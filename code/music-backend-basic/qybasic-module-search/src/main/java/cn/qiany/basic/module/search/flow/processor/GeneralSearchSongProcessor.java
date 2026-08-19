package cn.qiany.basic.module.search.flow.processor;

import cn.qiany.basic.abtest.exception.AbFlowException;
import cn.qiany.basic.abtest.processor.AbstractGeneralProcessor;
import cn.qiany.basic.module.search.controller.admin.song.vo.es.SongEsSearchResult;
import cn.qiany.basic.module.search.flow.context.GeneralRecFlowContext;
import cn.qiany.basic.module.search.flow.model.RecallType;
import cn.qiany.basic.module.search.flow.model.SongProcessorResult;
import cn.qiany.basic.module.search.service.es.SearchSongEsService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 复用第二期 ES 查询执行单路歌曲召回。
 */
@Component
@RequiredArgsConstructor
public class GeneralSearchSongProcessor extends AbstractGeneralProcessor<
        GeneralRecFlowContext, SongProcessorResult> {

    private final SearchSongEsService searchSongEsService;

    /**
     * 用文本节点产出的关键词执行歌曲 ES 召回。
     */
    @Override
    public SongProcessorResult processWithCustomParams(
            GeneralRecFlowContext context,
            Map<String, Object> params) {
        // 未配置 recall_type 时保持现网单曲搜索的默认 SONG 语义。
        String configuredType = params.get("recall_type") == null
                ? null : String.valueOf(params.get("recall_type"));
        String recallType = StringUtils.defaultIfBlank(
                StringUtils.trim(configuredType), RecallType.SONG.name());
        if (!RecallType.SONG.name().equalsIgnoreCase(recallType)) {
            throw new AbFlowException(
                    "第三期只支持SONG召回: " + recallType);
        }
        SongEsSearchResult result = searchSongEsService.search(
                context.getRequest(), context.getKeyword());
        return SongProcessorResult.builder()
                .recallType(RecallType.SONG)
                .rows(result.getRows())
                .total(result.getTotal())
                .build();
    }
}