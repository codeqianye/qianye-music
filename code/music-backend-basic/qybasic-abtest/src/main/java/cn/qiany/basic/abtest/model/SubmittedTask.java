package cn.qiany.basic.abtest.model;

import cn.qiany.basic.abtest.context.RecommendJobFlowContext;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.concurrent.Future;

/**
 * 已提交节点及其可取消任务句柄。
 *
 * <p>TaskDefinition 说明“执行什么”，Future 表示“当前执行到哪里”，
 * submittedAtNanos 用于计算节点绝对截止时间。执行器通过 Future.get 等待，
 * 超时后通过 Future.cancel(true) 请求中断工作线程。</p>
 */
@Getter
@RequiredArgsConstructor
public class SubmittedTask<
        C extends RecommendJobFlowContext<R>, R> {
    // 本次提交对应的节点定义
    private final TaskDefinition<C, R> task;
    // 提交时的单调时钟时间，不能用系统时间代替
    private final long submittedAtNanos;
    // 可等待、可取消的异步任务句柄
    private final Future<NodeOutcome<R>> future;
}