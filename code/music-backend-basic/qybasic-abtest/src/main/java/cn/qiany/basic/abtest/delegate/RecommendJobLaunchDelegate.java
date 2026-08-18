package cn.qiany.basic.abtest.delegate;

import cn.qiany.basic.abtest.context.RecommendJobFlowContext;
import cn.qiany.basic.abtest.executor.CompositeItemProcessor;
import cn.qiany.basic.abtest.model.FlowDefinition;
import cn.qiany.basic.abtest.parser.RecommendJobFlowParser;
import cn.qiany.basic.abtest.reader.JobFlowReader;
import cn.qiany.basic.abtest.source.JobFlowSource;
import cn.qiany.basic.abtest.writer.JobFlowWriter;
import org.slf4j.MDC;

import java.util.Map;

/**
 * 单次流程的通用启动入口。
 */
public class RecommendJobLaunchDelegate<
        I, C extends RecommendJobFlowContext<R>, R, O> {

    private final JobFlowSource flowSource;
    private final RecommendJobFlowParser<C, R> flowParser;
    private final JobFlowReader<I, C, R> reader;
    private final CompositeItemProcessor<C, R> processor;
    private final JobFlowWriter<C, O> writer;

    public RecommendJobLaunchDelegate(
            JobFlowSource flowSource,
            RecommendJobFlowParser<C, R> flowParser,
            JobFlowReader<I, C, R> reader,
            CompositeItemProcessor<C, R> processor,
            JobFlowWriter<C, O> writer) {
        this.flowSource = flowSource;
        this.flowParser = flowParser;
        this.reader = reader;
        this.processor = processor;
        this.writer = writer;
    }

    /**
     * 读取、解析并执行指定场景的流程。
     *
     * @param scene 当前请求选择的流程场景
     * @param input 业务模块传入的原始请求
     * @return Writer 转换后的业务输出
     */
    public O run(String scene, I input) {
        // Source 负责资源读取，Parser 只处理已反序列化的节点配置。
        Map<String, Map<String, Object>> nodes =
                flowSource.getRequired(scene);
        FlowDefinition<C, R> flow =
                flowParser.parseJobFlow(scene, nodes);
        C context = reader.read(input, flow);

        // 保存调用方上下文，避免流程结束后污染 Web 请求线程的 MDC。
        Map<String, String> previousMdc = MDC.getCopyOfContextMap();
        try {
            MDC.put("appId", scene);
            MDC.put("flowId", context.getJobFlowId());
            processor.processWithCustomParams(context, flow.getTasks());
            return writer.write(context);
        } finally {
            if (previousMdc == null) {
                MDC.clear();
            } else {
                MDC.setContextMap(previousMdc);
            }
        }
    }
}