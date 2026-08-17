package cn.qiany.basic.abtest.reader;

import cn.qiany.basic.abtest.context.RecommendJobFlowContext;
import cn.qiany.basic.abtest.model.FlowDefinition;

/**
 * 将业务输入转换为一次请求独享的流程 Context。
 */
public interface JobFlowReader<
        I, C extends RecommendJobFlowContext<R>, R> {
    C read(I input, FlowDefinition<C, R> flow);
}