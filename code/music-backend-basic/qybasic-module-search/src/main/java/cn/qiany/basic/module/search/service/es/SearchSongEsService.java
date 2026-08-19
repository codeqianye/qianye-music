package cn.qiany.basic.module.search.service.es;

import cn.qiany.basic.framework.common.exception.enums.GlobalErrorCodeConstants;
import cn.qiany.basic.module.search.common.AbstractGeneralSearchRequest;
import cn.qiany.basic.module.search.controller.admin.song.vo.es.SongEsSearchResult;
import cn.qiany.basic.module.search.controller.admin.song.vo.es.SongSearchItem;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static cn.qiany.basic.framework.common.exception.util.ServiceExceptionUtil.exception0;

/**
 * 执行单曲 ES 查询并映射当前页结果。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SearchSongEsService {

    private final SongEsQueryBuilder queryBuilder;
    private final IndexSongEsClient esClient;

    /**
     * 查询 ES 并返回总命中数和当前页数据。
     *
     * @param request 单曲搜索参数
     * @return ES 单曲搜索结果
     */
    public SongEsSearchResult search(AbstractGeneralSearchRequest request) {
        // 兼容第二期调用：仍以请求原词作为查询词。
        return search(request, request == null ? null : request.getText());
    }

    /**
     * 使用流程节点产出的指定关键词执行 ES 查询。
     *
     * @param request 单曲搜索参数
     * @param keyword 当前流程节点产出的查询词
     * @return ES 单曲搜索结果
     */
    public SongEsSearchResult search(
            AbstractGeneralSearchRequest request,
            String keyword) {
        SearchRequest searchRequest = queryBuilder.build(request, keyword);
        long start = System.currentTimeMillis();
        SearchResponse response = esClient.search(searchRequest);
        long queryCostMs = System.currentTimeMillis() - start;

        // total 来自 ES 全部命中，rows 只包含当前页
        SearchHits hits = response.getHits();
        long total = hits.getTotalHits() == null ? 0L : hits.getTotalHits().value;
        SearchHit[] searchHits = hits.getHits();
        List<SongSearchItem> rows = new ArrayList<>(searchHits.length);
        for (SearchHit hit : searchHits) {
            rows.add(mapHit(hit));
        }

        log.info("seq={}, engine=ES, index={}, total={}, returnSize={}, queryCostMs={}",
                request.getTraceSeq(), searchRequest.indices()[0],
                total, rows.size(), queryCostMs);
        return new SongEsSearchResult(total, rows);
    }

    /**
     * 将单条 ES 命中映射为搜索结果。
     */
    private SongSearchItem mapHit(SearchHit hit) {
        Map<String, Object> source = hit.getSourceAsMap();
        if (source == null) {
            throw exception0(GlobalErrorCodeConstants.INTERNAL_SERVER_ERROR.getCode(),"ES _source为空, _id={}",hit.getId());
        }

        // 同时保留原始字段和高亮字段
        SongSearchItem item = new SongSearchItem();
        item.setId(toLong(source.get("dbId")));
        item.setOrgId(asString(source.get("id")));
        item.setName(asString(source.get("name")));
        item.setSingerNames(asString(source.get("singerNames")));
        item.setAlbumNames(asString(source.get("albumNames")));
        item.setHot(toLong(source.get("hot")));
        item.setHighlightName(readHighlight(hit, "name"));
        item.setHighlightSingerNames(readHighlight(hit, "singerNames"));
        item.setHighlightAlbumNames(readHighlight(hit, "albumNames"));
        return item;
    }

    private Long toLong(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        try {
            return Long.valueOf(String.valueOf(value));
        } catch (NumberFormatException e) {
            throw exception0(GlobalErrorCodeConstants.INTERNAL_SERVER_ERROR.getCode(),"ES数值字段转Long失败: {}",value);
        }
    }

    private String asString(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private String readHighlight(SearchHit hit, String field) {
        HighlightField highlight = hit.getHighlightFields().get(field);
        if (highlight == null
                || highlight.fragments() == null
                || highlight.fragments().length == 0) {
            return null;
        }
        return Arrays.stream(highlight.fragments())
                .map(Text::string)
                .collect(Collectors.joining());
    }
}