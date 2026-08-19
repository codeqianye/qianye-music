package cn.qiany;

import cn.qiany.basic.module.search.common.AbstractGeneralSearchRequest;
import cn.qiany.basic.module.search.flow.context.GeneralRecFlowContext;
import cn.qiany.basic.module.search.flow.model.RecallType;
import cn.qiany.basic.module.search.flow.model.SongProcessorResult;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Context 合并只在流程主线程发生；本测试锁定第三期的合并语义。
 */
class GeneralRecFlowContextTest {

    @Test
    void shouldMergeKeywordRecallAndFinalResult() {
        AbstractGeneralSearchRequest request = new AbstractGeneralSearchRequest();
        request.setTraceSeq("trace-001");
        GeneralRecFlowContext context = new GeneralRecFlowContext(request, "s900");

        context.mergeSuccess(SongProcessorResult.builder()
                .normalizedKeyword("周杰伦").build());
        context.mergeSuccess(SongProcessorResult.builder()
                .recallType(RecallType.SONG).total(12L).build());
        context.mergeSuccess(SongProcessorResult.builder()
                .finalResult(true).total(12L).build());

        assertEquals("周杰伦", context.getKeyword());
        assertEquals(12L, context.getRecallBuckets()
                .get(RecallType.SONG).getTotal());
        assertEquals(12L, context.getTotal());
    }
}