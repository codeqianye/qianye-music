package cn.qiany.basic.abtest.executor;

import cn.qiany.basic.abtest.context.RecommendJobFlowContext;
import cn.qiany.basic.abtest.model.NodeOutcome;
import cn.qiany.basic.abtest.model.ProcessorStatus;
import cn.qiany.basic.abtest.model.ProcessorTrace;
import cn.qiany.basic.abtest.model.SubmittedTask;
import cn.qiany.basic.abtest.model.TaskDefinition;
import org.slf4j.MDC;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * 向专用线程池提交并执行单个节点。
 */
public class AsyncTaskManager<
        C extends RecommendJobFlowContext<R>, R> {

    private static final int MAX_ERROR_MESSAGE_LENGTH = 300;
    private final ThreadPoolTaskExecutor executor;

    public AsyncTaskManager(ThreadPoolTaskExecutor executor) {
        this.executor = executor;
    }

    /**
     * 提交一个节点，并把提交失败也转换成可被阶段执行器处理的结果。
     *
     * @param context 本次请求独享的流程上下文
     * @param task 已解析的节点定义
     * @return 包含 Future 与提交时间的任务句柄
     */
    public SubmittedTask<C, R> process(
            C context,
            TaskDefinition<C, R> task) {
        long submittedAtNanos = System.nanoTime();
        // MDC 属于线程本地变量，提交前必须提取，不能直接让工作线程读取。
        Map<String, String> callerMdc = MDC.getCopyOfContextMap();
        try {
            Future<NodeOutcome<R>> future = executor.submit(() ->
                    executeWithMdc(context, task, callerMdc));
            return new SubmittedTask<>(task, submittedAtNanos, future);
        } catch (RejectedExecutionException ex) {
            // 队列满不是抛给调用方，而是作为节点失败参与 required/optional 判断。
            NodeOutcome<R> outcome = buildOutcome(
                    task, ProcessorStatus.REJECTED, null,
                    submittedAtNanos, System.nanoTime(), ex);
            return new SubmittedTask<>(task, submittedAtNanos,
                    completedFuture(outcome));
        }
    }

    /**
     * 在工作线程内安装调用线程的 MDC，并在结束后恢复线程池原有上下文。
     */
    private NodeOutcome<R> executeWithMdc(
            C context,
            TaskDefinition<C, R> task,
            Map<String, String> callerMdc) {
        Map<String, String> workerMdc = MDC.getCopyOfContextMap();
        try {
            if (callerMdc == null) {
                MDC.clear();
            } else {
                MDC.setContextMap(callerMdc);
            }
            return executeNode(context, task);
        } finally {
            if (workerMdc == null) {
                MDC.clear();
            } else {
                MDC.setContextMap(workerMdc);
            }
        }
    }

    /**
     * 调用 Processor 并将所有异常标准化为节点执行结果。
     */
    private NodeOutcome<R> executeNode(
            C context,
            TaskDefinition<C, R> task) {
        long start = System.nanoTime();
        try {
            // 超时取消可能发生在真正执行前，先检查可避免无意义的 ES 调用。
            if (Thread.currentThread().isInterrupted()) {
                return buildOutcome(task, ProcessorStatus.CANCELLED,
                        null, start, System.nanoTime(), null);
            }
            R result = task.getProcessor().processWithCustomParams(
                    context, task.getParams());
            return buildOutcome(task, ProcessorStatus.SUCCESS,
                    result, start, System.nanoTime(), null);
        } catch (Throwable ex) {
            ProcessorStatus status = Thread.currentThread().isInterrupted()
                    ? ProcessorStatus.CANCELLED : ProcessorStatus.FAILED;
            return buildOutcome(task, status, null,
                    start, System.nanoTime(), ex);
        }
    }

    /**
     * 同时构造机器可判断的 Outcome 与面向排障的 Trace。
     */
    private NodeOutcome<R> buildOutcome(
            TaskDefinition<C, R> task,
            ProcessorStatus status,
            R result,
            long start,
            long completed,
            Throwable error) {
        ProcessorTrace trace = ProcessorTrace.builder()
                .nodeName(task.getType())
                .fmap(task.getFmap())
                .order(task.getExecOrder())
                .status(status)
                .required(task.isRequired())
                .threadName(Thread.currentThread().getName())
                .startTimeNanos(start)
                .completedAtNanos(completed)
                .costMs(TimeUnit.NANOSECONDS.toMillis(completed - start))
                .errorType(error == null ? null
                        : error.getClass().getName())
                .errorMessage(errorMessage(error))
                .build();
        return NodeOutcome.<R>builder()
                .nodeName(task.getType())
                .fmap(task.getFmap())
                .status(status)
                .result(result)
                .trace(trace)
                .completedAtNanos(completed)
                .error(error)
                .build();
    }

    /**
     * 限制异常文本长度，防止流程日志被下游异常内容撑大。
     */
    private String errorMessage(Throwable error) {
        if (error == null || error.getMessage() == null) {
            return null;
        }
        String message = error.getMessage();
        return message.length() <= MAX_ERROR_MESSAGE_LENGTH
                ? message : message.substring(0, MAX_ERROR_MESSAGE_LENGTH);
    }

    /**
     * 为提交被拒绝的节点创建一个已完成 Future，统一后续等待逻辑。
     */
    private Future<NodeOutcome<R>> completedFuture(
            NodeOutcome<R> outcome) {
        FutureTask<NodeOutcome<R>> future =
                new FutureTask<>(() -> outcome);
        future.run();
        return future;
    }
}