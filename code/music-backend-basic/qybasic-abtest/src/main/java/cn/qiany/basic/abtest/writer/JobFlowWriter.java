package cn.qiany.basic.abtest.writer;

/**
 * 将执行完成的 Context 转换为业务输出。
 */
public interface JobFlowWriter<C, O> {
    O write(C context);
}