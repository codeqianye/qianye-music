package cn.qiany.basic.abtest.model;

import lombok.Builder;
import lombok.Getter;

/**
 * 单个流程节点的执行轨迹。
 *
 * <p>只保存调度和排障信息，不保存完整请求、密码、Token 或手机号。</p>
 */
@Getter
@Builder
public class ProcessorTrace {
    // JSON 中的节点名称
    private final String nodeName;
    // Processor 映射标识
    private final String fmap;
    // 节点执行阶段
    private final int order;
    // 节点最终状态
    private final ProcessorStatus status;
    // 必选节点失败时是否终止流程
    private final boolean required;
    // 实际执行节点的线程名称
    private final String threadName;
    // 单调时钟开始时间
    private final long startTimeNanos;
    // 单调时钟完成时间
    private final long completedAtNanos;
    // 节点执行耗时
    private final long costMs;
    // 异常类型
    private final String errorType;
    // 截断后的异常摘要
    private final String errorMessage;
}