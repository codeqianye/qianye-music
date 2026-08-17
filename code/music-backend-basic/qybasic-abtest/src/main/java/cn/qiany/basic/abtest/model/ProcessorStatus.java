package cn.qiany.basic.abtest.model;

/**
 * Processor 节点执行状态。
 */
public enum ProcessorStatus {
    SUCCESS,
    SKIPPED,
    FAILED,
    TIMEOUT,
    REJECTED,
    CANCELLED
}