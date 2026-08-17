package cn.qiany.basic.abtest.processor;

import cn.qiany.basic.abtest.context.RecommendJobFlowContext;

import java.util.Map;

/**
 * 流程节点处理器基类。
 *
 * <p>Processor 是无请求状态的 Spring 单例，只读取 Context，
 * 返回独立结果，不直接修改共享 Context。</p>
 *
 * @param <C> 业务流程上下文
 * @param <R> 单节点业务结果
 */
public abstract class AbstractGeneralProcessor<
        C extends RecommendJobFlowContext<R>, R> {

    /**
     * 执行当前节点。
     *
     * @param context 当前阶段开始前的稳定上下文
     * @param params 当前节点的只读配置
     * @return 当前节点独立结果
     */
public abstract R processWithCustomParams(
        C context,
        Map<String, Object> params);
}