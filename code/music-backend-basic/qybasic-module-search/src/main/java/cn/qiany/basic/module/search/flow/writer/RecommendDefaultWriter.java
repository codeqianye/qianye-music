package cn.qiany.basic.module.search.flow.writer;

import cn.qiany.basic.abtest.model.RecommendResult;
import cn.qiany.basic.abtest.writer.JobFlowWriter;
import cn.qiany.basic.module.search.controller.admin.song.vo.es.SongSearchItem;
import cn.qiany.basic.module.search.flow.context.GeneralRecFlowContext;
import org.springframework.stereotype.Component;

/**
 * 将执行完成的歌曲 Context 转换为通用流程结果。
 */
@Component
public class RecommendDefaultWriter implements JobFlowWriter<
        GeneralRecFlowContext,
        RecommendResult<SongSearchItem, Void>> {

    /**
     * 将流程 Context 的最终桶转换为搜索 Service 使用的统一结果。
     */
    @Override
    public RecommendResult<SongSearchItem, Void> write(
            GeneralRecFlowContext context) {
        return RecommendResult.<SongSearchItem, Void>builder()
                .recData(context.getFinalRows())
                .total(context.getTotal())
                .flowId(context.getJobFlowId())
                .build();
    }
}