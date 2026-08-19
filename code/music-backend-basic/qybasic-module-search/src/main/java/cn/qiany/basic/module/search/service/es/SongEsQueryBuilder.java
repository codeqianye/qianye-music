package cn.qiany.basic.module.search.service.es;

import cn.qiany.basic.framework.common.exception.enums.GlobalErrorCodeConstants;
import cn.qiany.basic.module.search.common.AbstractGeneralSearchRequest;
import cn.qiany.basic.module.search.config.SongESProperties;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MultiMatchQueryBuilder;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneId;

import static cn.qiany.basic.framework.common.exception.util.ServiceExceptionUtil.exception0;

/**
 * 构建单曲 ES 查询、过滤、排序和高亮 DSL。
 */
@Component
@RequiredArgsConstructor
public class SongEsQueryBuilder {

    private static final ZoneId BUSINESS_ZONE = ZoneId.of("Asia/Shanghai");
    private static final int MAX_PAGE_SIZE = 50;

    private final SongESProperties properties;

    /**
     * 根据单曲搜索参数构建 ES 请求。
     *
     * @param request 单曲搜索参数
     * @return ES 搜索请求
     */
    public SearchRequest build(AbstractGeneralSearchRequest request) {
        // 兼容第二期调用：仍以请求原词作为查询词。
        return build(request, request == null ? null : request.getText());
    }

    /**
     * 根据指定关键词构建 ES 请求。
     *
     * <p>第三期的 Processor 将 TextRule 处理后的关键词传入此方法；除查询词来源外，
     * 查询字段、过滤、排序、高亮、分页和超时逻辑均与第二期保持一致。</p>
     *
     * @param request 单曲搜索参数
     * @param keyword 当前流程节点产出的查询词
     * @return ES 查询请求
     */
    public SearchRequest build(AbstractGeneralSearchRequest request, String keyword) {
        if (request == null) {
            throw exception0(GlobalErrorCodeConstants.INTERNAL_SERVER_ERROR.getCode(),"搜索请求不能为空");
        }
        String normalizedKeyword = StringUtils.trim(keyword);
        if (StringUtils.isBlank(normalizedKeyword)) {
            throw exception0(GlobalErrorCodeConstants.INTERNAL_SERVER_ERROR.getCode(),"搜索关键词不能为空");
        }

        int pageNo = request.getPageNo();
        int pageSize = request.getPageSize();
        if (pageNo < 1) {
            throw exception0(GlobalErrorCodeConstants.INTERNAL_SERVER_ERROR.getCode(),"pageNo不能小于1");
        }
        if (pageSize < 1 || pageSize > MAX_PAGE_SIZE) {
            throw exception0(GlobalErrorCodeConstants.INTERNAL_SERVER_ERROR.getCode(),"pageSize必须在1至50之间");
        }
        long from = (long) (pageNo - 1) * pageSize;
        if (from + pageSize > properties.getElasticsearch().getMaxResultWindow()) {
            throw exception0(GlobalErrorCodeConstants.INTERNAL_SERVER_ERROR.getCode(),"分页窗口超过上限: {}",properties.getElasticsearch().getMaxResultWindow());
        }

        //type: 以匹配效果最好的那个字段为主来计算相关性分数
        MultiMatchQueryBuilder textQuery = QueryBuilders
                .multiMatchQuery(normalizedKeyword, "name", "singerNames", "albumNames")
                .type(MultiMatchQueryBuilder.Type.BEST_FIELDS)
                .operator(Operator.OR);

        // 以业务时区判断版权有效期，避免受 ES 容器时区影响
        String today = LocalDate.now(BUSINESS_ZONE).toString();
        BoolQueryBuilder validCopyright = QueryBuilders.boolQuery()
                .should(QueryBuilders.boolQuery().mustNot(QueryBuilders.existsQuery("invalidate")))
                .should(QueryBuilders.rangeQuery("invalidate").gte(today))
                .minimumShouldMatch(1);

        BoolQueryBuilder query = QueryBuilders.boolQuery()
                .must(textQuery)
                .filter(QueryBuilders.termQuery("isEnabled", 1))
                .filter(QueryBuilders.termQuery("firstStartState", 1))
                .filter(QueryBuilders.termQuery("isCopyright", 1))
                .filter(validCopyright);

        // 按相关性、热度和主键保证结果稳定
        SearchSourceBuilder source = new SearchSourceBuilder()
                .query(query)
                .from(Math.toIntExact(from))
                .size(pageSize)
                .trackTotalHits(true) //精确统计总数
                //第一个参数：限制返回这些字段,第二个参数 null：没有要排除的字段
                //举例 .fetchSource(null, new String[]{"hot", "dbId"}):null不限制返回字段，表示其他字段都返回，但不要 hot 和 dbId
                .fetchSource(new String[]{"id", "dbId", "name", "singerNames", "albumNames", "hot"}, null)
                .sort(SortBuilders.scoreSort().order(SortOrder.DESC))
                .sort("hot", SortOrder.DESC)
                .sort("dbId", SortOrder.DESC)
                .highlighter(buildHighlighter())
                .timeout(TimeValue.timeValueMillis(properties.getElasticsearch().getQueryTimeoutMs()));

        return new SearchRequest(properties.getElasticsearch().getIndexName()).source(source);
    }

    private HighlightBuilder buildHighlighter() {
        HighlightBuilder builder = new HighlightBuilder()
                .preTags("<em>")
                .postTags("</em>")
                .requireFieldMatch(false);
        builder.field(new HighlightBuilder.Field("name").numOfFragments(0));
        builder.field(new HighlightBuilder.Field("singerNames").numOfFragments(0));
        builder.field(new HighlightBuilder.Field("albumNames").numOfFragments(0));
        return builder;
    }
}