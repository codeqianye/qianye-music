package cn.qiany.basic.module.search.templet;

import cn.qiany.basic.module.search.common.AbstractGeneralSearchRequest;
import cn.qiany.basic.module.search.common.AbstractGeneralSearchResponse;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.UUID;

@Slf4j
public abstract class AbstractSearchTemplate {

    /**
     * 前置处理
     * 生成流水号
     * @param request 接口参数
     */
    protected void preHandle(AbstractGeneralSearchRequest request) {
        String traceId = UUID.randomUUID().toString().replace("-", "");
        request.setTraceSeq(traceId);
    }

    /**
     * 后置处理
     * @return
     */
    protected AbstractGeneralSearchResponse afterHandle(AbstractGeneralSearchRequest request,AbstractGeneralSearchResponse response, List recallData){
        response.setTotal(recallData.size());
        response.build(request, recallData);
        return response;
    }

    /**
     * 处理 ES 已分页的搜索结果。
     */
    protected AbstractGeneralSearchResponse afterHandlePaged(
            AbstractGeneralSearchRequest request,
            AbstractGeneralSearchResponse response,
            List<?> recallData,
            long total) {
        response.buildPaged(request, recallData, total);
        return response;
    }
}