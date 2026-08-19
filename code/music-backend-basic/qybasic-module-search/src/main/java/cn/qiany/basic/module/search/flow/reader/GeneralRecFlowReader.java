package cn.qiany.basic.module.search.flow.reader;

import cn.qiany.basic.abtest.model.FlowDefinition;
import cn.qiany.basic.abtest.reader.JobFlowReader;
import cn.qiany.basic.module.search.common.AbstractGeneralSearchRequest;
import cn.qiany.basic.module.search.flow.context.GeneralRecFlowContext;
import cn.qiany.basic.module.search.flow.model.SongProcessorResult;
import org.springframework.stereotype.Component;

/**
 * 为每次歌曲搜索请求创建独立 Context。
 */
@Component
public class GeneralRecFlowReader implements JobFlowReader<
        AbstractGeneralSearchRequest,
        GeneralRecFlowContext,
        SongProcessorResult> {

    /**
     * 为每个请求创建 Context，禁止跨请求复用可变状态。
     */
    @Override
    public GeneralRecFlowContext read(
            AbstractGeneralSearchRequest input,
            FlowDefinition<GeneralRecFlowContext, SongProcessorResult> flow) {
        return new GeneralRecFlowContext(input, flow.getScene());
    }
}