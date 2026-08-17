package cn.qiany.basic.abtest.context;

import cn.qiany.basic.abtest.model.ProcessorTrace;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 一次请求独享的流程上下文基类。
 *
 * @param <R> 单节点业务结果类型
 */
public abstract class RecommendJobFlowContext<R> {
    private final String scene;
    private final String jobFlowId;
    private final long startTimeNanos;
    private final List<ProcessorTrace> traces = new ArrayList<>();

    protected RecommendJobFlowContext(String scene) {
        this.scene = scene;
        this.jobFlowId = scene;
        this.startTimeNanos = System.nanoTime();
    }

    /**
     * 由流程主线程合并成功节点的业务结果。
     *
     * @param result 节点业务结果
     */
    public abstract void mergeSuccess(R result);

    /**
     * 由流程主线程追加节点轨迹。
     *
     * @param trace 节点轨迹
     */
    public final void addTrace(ProcessorTrace trace) {
        traces.add(trace);
    }

    public final String getScene() {
        return scene;
    }

    public final String getJobFlowId() {
        return jobFlowId;
    }

    public final long getStartTimeNanos() {
        return startTimeNanos;
    }

    /**
     * 返回只读轨迹，防止业务代码绕过执行器修改顺序。
     */
    public final List<ProcessorTrace> getTraces() {
        return Collections.unmodifiableList(traces);
    }
}