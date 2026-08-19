package cn.qiany.basic.module.search.flow.context;

import cn.qiany.basic.abtest.context.RecommendJobFlowContext;
import cn.qiany.basic.module.search.common.AbstractGeneralSearchRequest;
import cn.qiany.basic.module.search.controller.admin.song.vo.es.SongSearchItem;
import cn.qiany.basic.module.search.flow.model.RecallType;
import cn.qiany.basic.module.search.flow.model.SongProcessorResult;
import lombok.Getter;

import java.util.Collections;
import java.util.EnumMap;
import java.util.List;

/**
 * 一次单曲搜索请求独享的流程 Context。
 */
@Getter
public class GeneralRecFlowContext
        extends RecommendJobFlowContext<SongProcessorResult> {
    private final AbstractGeneralSearchRequest request;
    private final String traceSeq;
    private String keyword;
    private final EnumMap<RecallType, SongProcessorResult> recallBuckets =
            new EnumMap<>(RecallType.class);
    private List<SongSearchItem> finalRows = Collections.emptyList();
    private long total;

    public GeneralRecFlowContext(
            AbstractGeneralSearchRequest request,
            String scene) {
        super(scene);
        this.request = request;
        this.traceSeq = request.getTraceSeq();
    }

    /**
     * 按节点声明顺序合并成功结果，仅由流程主线程调用。
     */
    @Override
    public void mergeSuccess(SongProcessorResult result) {
        if (result == null) {
            return;
        }
        if (result.getNormalizedKeyword() != null) {
            this.keyword = result.getNormalizedKeyword();
        }
        if (result.getRecallType() != null) {
            this.recallBuckets.put(result.getRecallType(), result);
        }
        if (result.isFinalResult()) {
            this.finalRows = result.getRows() == null
                    ? Collections.emptyList() : result.getRows();
            this.total = result.getTotal();
        }
    }
}