package cn.qiany.basic.abtest.executor;

import cn.qiany.basic.abtest.context.RecommendJobFlowContext;
import cn.qiany.basic.abtest.exception.AbFlowException;
import cn.qiany.basic.abtest.model.NodeOutcome;
import cn.qiany.basic.abtest.model.ProcessorStatus;
import cn.qiany.basic.abtest.model.ProcessorTrace;
import cn.qiany.basic.abtest.model.SubmittedTask;
import cn.qiany.basic.abtest.model.TaskDefinition;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * 按 order 分阶段执行节点，并由当前线程稳定合并结果。
 */
public class CompositeItemProcessor<
        C extends RecommendJobFlowContext<R>, R> {

    private final AsyncTaskManager<C, R> asyncTaskManager;
    private final long stageTimeoutMs;

    public CompositeItemProcessor(
            AsyncTaskManager<C, R> asyncTaskManager,
            long stageTimeoutMs) {
        if (stageTimeoutMs <= 0) {
            throw new IllegalArgumentException("stageTimeoutMs必须大于0");
        }
        this.asyncTaskManager = asyncTaskManager;
        this.stageTimeoutMs = stageTimeoutMs;
    }

    /**
     * 按稳定排序后的任务列表执行全部阶段。
     */
    public C processWithCustomParams(
            C context,
            List<TaskDefinition<C, R>> tasks) {
        int from = 0;
        while (from < tasks.size()) {
            int order = tasks.get(from).getExecOrder();
            int to = from + 1;
            while (to < tasks.size()
                    && tasks.get(to).getExecOrder() == order) {
                to++;
            }
            executeStage(context, new ArrayList<>(tasks.subList(from, to)));
            from = to;
        }
        return context;
    }

    /**
     * 执行一个 order 阶段：先并发提交，再统一等待并由主线程稳定合并。
     */
    private void executeStage(
            C context,
            List<TaskDefinition<C, R>> stageTasks) {
        long stageStart = System.nanoTime();
        long stageDeadline = stageStart
                + TimeUnit.MILLISECONDS.toNanos(stageTimeoutMs);
        Map<TaskDefinition<C, R>, NodeOutcome<R>> outcomes =
                new LinkedHashMap<>();
        Map<TaskDefinition<C, R>, SubmittedTask<C, R>> submitted =
                new LinkedHashMap<>();

        // 本阶段的全部可用节点先提交，保证同一 order 具备并发机会。
        for (TaskDefinition<C, R> task : stageTasks) {
            if (!task.isToggle()) {
                outcomes.put(task, localOutcome(
                        task, ProcessorStatus.SKIPPED, null));
            } else {
                submitted.put(task, asyncTaskManager.process(context, task));
            }
        }

        NodeOutcome<R> requiredFailure = null;
        // 仍按配置顺序取结果，避免并发完成先后影响轨迹和合并顺序。
        for (TaskDefinition<C, R> task : stageTasks) {
            if (outcomes.containsKey(task)) {
                continue;
            }
            SubmittedTask<C, R> submittedTask = submitted.get(task);
            NodeOutcome<R> outcome;
            if (requiredFailure != null) {
                // 同阶段后续节点不再等待；尽力中断，最终仍由状态轨迹说明结果。
                submittedTask.getFuture().cancel(true);
                outcome = localOutcome(
                        task, ProcessorStatus.CANCELLED, null);
            } else {
                outcome = await(submittedTask, stageDeadline);
            }
            outcomes.put(task, outcome);
            if (task.isRequired()
                    && outcome.getStatus() != ProcessorStatus.SUCCESS) {
                requiredFailure = outcome;
            }
        }

        // 无论成功还是失败，都按照配置声明顺序记录轨迹
        for (TaskDefinition<C, R> task : stageTasks) {
            context.addTrace(outcomes.get(task).getTrace());
        }
        if (requiredFailure != null) {
            throw new AbFlowException(
                    "必选流程节点执行失败: "
                            + requiredFailure.getNodeName(),
                    requiredFailure.getError());
        }
        // 当前阶段全部等待完成后，才允许主线程稳定合并
        for (TaskDefinition<C, R> task : stageTasks) {
            NodeOutcome<R> outcome = outcomes.get(task);
            if (outcome.getStatus() == ProcessorStatus.SUCCESS) {
                context.mergeSuccess(outcome.getResult());
            }
        }
    }

    /**
     * 在节点截止时间与阶段截止时间中取更早者，避免单节点拖住整个阶段。
     */
    private NodeOutcome<R> await(
            SubmittedTask<C, R> submitted,
            long stageDeadline) {
        TaskDefinition<C, R> task = submitted.getTask();
        long nodeDeadline = submitted.getSubmittedAtNanos()
                + TimeUnit.MILLISECONDS.toNanos(task.getTimeoutMs());
        long deadline = Math.min(stageDeadline, nodeDeadline);
        try {
            NodeOutcome<R> outcome;
            if (submitted.getFuture().isDone()) {
                outcome = submitted.getFuture().get();
            } else {
                long remaining = deadline - System.nanoTime();
                if (remaining <= 0) {
                    submitted.getFuture().cancel(true);
                    return localOutcome(task, ProcessorStatus.TIMEOUT, null);
                }
                outcome = submitted.getFuture().get(
                        remaining, TimeUnit.NANOSECONDS);
            }
            // 即使 Future 已返回，超过截止时间的结果也不能参与后续合并。
            if (outcome.getCompletedAtNanos() > deadline) {
                return localOutcome(task, ProcessorStatus.TIMEOUT, null);
            }
            return outcome;
        } catch (TimeoutException ex) {
            submitted.getFuture().cancel(true);
            return localOutcome(task, ProcessorStatus.TIMEOUT, ex);
        } catch (CancellationException ex) {
            return localOutcome(task, ProcessorStatus.CANCELLED, ex);
        } catch (InterruptedException ex) {
            submitted.getFuture().cancel(true);
            Thread.currentThread().interrupt();
            return localOutcome(task, ProcessorStatus.CANCELLED, ex);
        } catch (ExecutionException ex) {
            return localOutcome(task, ProcessorStatus.FAILED, ex.getCause());
        }
    }

    private NodeOutcome<R> localOutcome(
            TaskDefinition<C, R> task,
            ProcessorStatus status,
            Throwable error) {
        long now = System.nanoTime();
        ProcessorTrace trace = ProcessorTrace.builder()
                .nodeName(task.getType())
                .fmap(task.getFmap())
                .order(task.getExecOrder())
                .status(status)
                .required(task.isRequired())
                .threadName(Thread.currentThread().getName())
                .startTimeNanos(now)
                .completedAtNanos(now)
                .costMs(0L)
                .errorType(error == null ? null
                        : error.getClass().getName())
                .errorMessage(error == null ? null : error.getMessage())
                .build();
        return NodeOutcome.<R>builder()
                .nodeName(task.getType())
                .fmap(task.getFmap())
                .status(status)
                .trace(trace)
                .completedAtNanos(now)
                .error(error)
                .build();
    }
}