package cn.qiany.basic.module.search.service.song;

import cn.qiany.basic.abtest.delegate.RecommendJobLaunchDelegate;
import cn.qiany.basic.abtest.exception.AbFlowException;
import cn.qiany.basic.abtest.model.RecommendResult;
import cn.qiany.basic.framework.common.exception.enums.GlobalErrorCodeConstants;
import cn.qiany.basic.module.search.common.AbstractGeneralSearchRequest;
import cn.qiany.basic.module.search.common.AbstractGeneralSearchResponse;
import cn.qiany.basic.module.search.config.SongESProperties;
import cn.qiany.basic.module.search.controller.admin.song.vo.es.SongSearchItem;
import cn.qiany.basic.module.search.flow.config.SongSearchFlowProperties;
import cn.qiany.basic.module.search.flow.context.GeneralRecFlowContext;
import cn.qiany.basic.module.search.flow.model.SongProcessorResult;
import cn.qiany.basic.module.search.dal.dataobject.song.IndexSongDO;
import cn.qiany.basic.module.search.dal.mysql.song.IndexSongMapper;
import cn.qiany.basic.module.search.enums.SearchEngineType;
import cn.qiany.basic.module.search.templet.AbstractSearchTemplate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;

import static cn.qiany.basic.framework.common.exception.util.ServiceExceptionUtil.exception0;

/**
 * 歌曲 Service 实现类
 *
 * @author fengpeng
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SearchSongServiceImpl extends AbstractSearchTemplate implements SearchSongService {
    private final IndexSongMapper indexSongMapper;
    private final SongESProperties properties;
    private final SongSearchFlowProperties flowProperties;
    private final RecommendJobLaunchDelegate<
            AbstractGeneralSearchRequest,
            GeneralRecFlowContext,
            SongProcessorResult,
            RecommendResult<SongSearchItem, Void>> songJobLaunchDelegate;

    /**
     * 执行单曲搜索。
     *
     * @param request 单曲搜索参数
     * @return 通用搜索响应
     */
    @Override
    public AbstractGeneralSearchResponse search(AbstractGeneralSearchRequest request) {
        long start = System.currentTimeMillis();
        preHandle(request);

        // MYSQL 模式继续使用第一期查询和内存分页
        if (properties.getEngine() == SearchEngineType.MYSQL) {
            return searchByMysql(request, start);
        }

        // 未传 app_id 时固定使用第三期默认场景 s900。
        String scene = StringUtils.defaultIfBlank(
                request.getApp_id(), flowProperties.getDefaultAppId());
        try {
            // Delegate 内部依次完成：读配置、解析、构造 Context、执行节点、写结果。
            RecommendResult<SongSearchItem, Void> result =
                    songJobLaunchDelegate.run(scene, request);
            // afterHandle 使用该值拼接既有响应 seq：traceSeq@s900。
            request.getExtra().setFlowId(result.getFlowId());
            AbstractGeneralSearchResponse response = afterHandle(
                    request, new AbstractGeneralSearchResponse(),
                    result.getRecData(), result.getTotal());
            logSuccess(request, "ES", result.getTotal(),
                    result.getRecData().size(), start);
            return response;
        } catch (AbFlowException ex) {
            log.error("seq={}, scene={}, 单曲流程执行失败",
                    request.getTraceSeq(), scene, ex);
            // 维持搜索模块原有的业务异常出口，Controller 无需改动。
            throw exception0(
                    GlobalErrorCodeConstants.INTERNAL_SERVER_ERROR.getCode(),
                    "单曲流程执行失败");
        }
    }

    /**
     * 保留第一期 MySQL 查询链路，不创建流程 Context，也不读取流程配置。
     */
    private AbstractGeneralSearchResponse searchByMysql(
            AbstractGeneralSearchRequest request,
            long start) {
        List<IndexSongDO> rows = indexSongMapper.selectList(request);
        AbstractGeneralSearchResponse response = afterHandleDB(
                request, new AbstractGeneralSearchResponse(), rows);
        logSuccess(request, "MYSQL", rows.size(),
                response.getC() == null ? 0 : ((List<?>) response.getC()).size(), start);
        return response;
    }

    /**
     * 输出统一的成功摘要；关键词已截断，避免日志记录过长查询文本。
     */
    private void logSuccess(AbstractGeneralSearchRequest request,
                            String actualEngine,
                            long total,
                            int returnSize,
                            long start) {
        log.info("seq={}, keyword={}, pageNo={}, pageSize={}, engine={}, "
                        + "total={}, returnSize={}, totalCostMs={}",
                request.getTraceSeq(), keywordSummary(request.getText()),
                request.getPageNo(), request.getPageSize(), actualEngine,
                total, returnSize, System.currentTimeMillis() - start);
    }

    /**
     * 生成用于日志的关键词摘要，避免完整长文本进入日志。
     */
    private String keywordSummary(String keyword) {
        String value = StringUtils.defaultString(StringUtils.trim(keyword));
        return value.length() <= 20 ? value : value.substring(0, 20) + "...";
    }
}