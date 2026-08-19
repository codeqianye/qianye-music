package cn.qiany.basic.module.search.flow.processor;

import cn.qiany.basic.abtest.processor.AbstractGeneralProcessor;
import cn.qiany.basic.module.search.flow.context.GeneralRecFlowContext;
import cn.qiany.basic.module.search.flow.model.RecallType;
import cn.qiany.basic.module.search.flow.model.SongProcessorResult;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Map;

/**
 * 将歌曲召回桶转换为流程最终结果。
 */
@Component
public class GeneralDecorateProcessor extends AbstractGeneralProcessor<
        GeneralRecFlowContext, SongProcessorResult> {

    /**
     * 将 SONG 召回桶定格为 HTTP 响应需要的最终列表。
     */
    @Override
    public SongProcessorResult processWithCustomParams(
            GeneralRecFlowContext context,
            Map<String, Object> params) {
        SongProcessorResult recall =
                context.getRecallBuckets().get(RecallType.SONG);
        if (recall == null) {
            // 可选召回失败时返回空结果，避免装饰节点再抛出空指针异常。
            return SongProcessorResult.builder()
                    .rows(Collections.emptyList())
                    .total(0L)
                    .finalResult(true)
                    .build();
        }
        return SongProcessorResult.builder()
                .rows(recall.getRows())
                .total(recall.getTotal())
                .finalResult(true)
                .build();
    }
}