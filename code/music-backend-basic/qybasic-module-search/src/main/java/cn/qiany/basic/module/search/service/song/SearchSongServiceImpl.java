package cn.qiany.basic.module.search.service.song;

import cn.qiany.basic.module.search.common.AbstractGeneralSearchRequest;
import cn.qiany.basic.module.search.common.AbstractGeneralSearchResponse;
import cn.qiany.basic.module.search.config.SongESProperties;
import cn.qiany.basic.module.search.controller.admin.song.vo.es.SongEsSearchResult;
import cn.qiany.basic.module.search.dal.dataobject.song.IndexSongDO;
import cn.qiany.basic.module.search.dal.mysql.song.IndexSongMapper;
import cn.qiany.basic.module.search.enums.SearchEngineType;
import cn.qiany.basic.module.search.service.es.SearchSongEsService;
import cn.qiany.basic.module.search.templet.AbstractSearchTemplate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;

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
    private final SearchSongEsService searchSongEsService;
    private final SongESProperties properties;

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

        // ES 模式只查询 ES，失败时不自动切换 MySQL
        SongEsSearchResult esResult = searchSongEsService.search(request);
        AbstractGeneralSearchResponse response = afterHandle(
                request, new AbstractGeneralSearchResponse(),
                esResult.getRows(), esResult.getTotal());
        logSuccess(request, "ES", esResult.getTotal(),
                esResult.getRows().size(), start);
        return response;
    }

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

    private String keywordSummary(String keyword) {
        String value = StringUtils.defaultString(StringUtils.trim(keyword));
        return value.length() <= 20 ? value : value.substring(0, 20) + "...";
    }
}
