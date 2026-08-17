package cn.qiany.basic.abtest.model;

import lombok.Builder;
import lombok.Getter;

/**
 * 单节点完整执行结果，由异步任务返回给主线程。
 *
 * <p>业务数据 result 和调度信息 status、trace 放在同一个不可变对象中。
 * 工作线程只创建 NodeOutcome，不直接写 Context；主线程收到它后再判断是否按时、
 * 是否成功以及是否允许合并。</p>
 *
 * @param <R> 单节点业务结果类型
 */
@Getter
@Builder
public class NodeOutcome<R> {
    // JSON 中的节点名称，用于日志和异常定位
    private final String nodeName;
    // 实际执行的 Processor 映射标识
    private final String fmap;
    // 节点最终调度状态，决定 result 是否可以合并
    private final ProcessorStatus status;
    // Processor 成功时返回的业务结果，非成功状态通常为空
    private final R result;
    // 节点线程、耗时、输入输出数量等观测信息
    private final ProcessorTrace trace;
    // 工作线程真正完成的单调时钟时间，用于识别迟到结果
    private final long completedAtNanos;
    // Processor 原始异常，仅供执行器判断和主日志记录
    private final Throwable error;
}