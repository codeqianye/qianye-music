package cn.qiany.basic.module.search.service.es;

import cn.qiany.basic.framework.common.exception.enums.GlobalErrorCodeConstants;
import cn.qiany.basic.module.search.config.SongESProperties;
import cn.qiany.basic.module.search.controller.admin.song.vo.sync.BulkWriteResult;
import cn.qiany.basic.module.search.dal.es.song.IndexSongEsDocument;
import cn.qiany.basic.module.search.service.sync.IndexSongEsConverter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.ElasticsearchStatusException;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.admin.indices.refresh.RefreshRequest;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.core.CountRequest;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.rest.RestStatus;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

import static cn.qiany.basic.framework.common.exception.util.ServiceExceptionUtil.exception0;

/**
 * 封装歌曲索引管理、Bulk 写入和查询操作。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class IndexSongEsClient {

    private static final String MAPPING_PATH = "es/index_song_mapping.json";
    private static final int MAX_FAILURE_IDS = 20;

    private final RestHighLevelClient client;
    private final SongESProperties properties;
    private final IndexSongEsConverter converter;

    /**
     * 检查目标索引是否存在。
     *
     * @return true 表示索引存在
     */
    public boolean indexExists() {
        try {
            GetIndexRequest request = new GetIndexRequest(indexName());
            return client.indices().exists(request, RequestOptions.DEFAULT);
        } catch (IOException e) {
            throw exception0(GlobalErrorCodeConstants.INTERNAL_SERVER_ERROR.getCode(),"检查ES索引失败: {}",indexName());

        }
    }

    /**
     * 删除目标索引，索引不存在时直接返回。
     */
    public void deleteIndex() {
        if (!indexExists()) {
            return;
        }
        try {
            DeleteIndexRequest request = new DeleteIndexRequest(indexName());
            AcknowledgedResponse response = client.indices().delete(request, RequestOptions.DEFAULT);
            if (!response.isAcknowledged()) {
                throw exception0(GlobalErrorCodeConstants.INTERNAL_SERVER_ERROR.getCode(),"删除ES索引未被集群确认: {}",indexName());
            }
        } catch (IOException e) {
            throw exception0(GlobalErrorCodeConstants.INTERNAL_SERVER_ERROR.getCode(),"删除ES索引失败: {}",indexName());
        }
    }

    /**
     * 根据 classpath Mapping 创建目标索引。
     */
    public void createIndex() {
        if (indexExists()) {
            throw exception0(GlobalErrorCodeConstants.INTERNAL_SERVER_ERROR.getCode(),"目标ES索引已存在: {}",indexName());

        }
        String mapping = readClasspath(MAPPING_PATH);
        try {
            CreateIndexRequest request = new CreateIndexRequest(indexName());
            request.source(mapping, XContentType.JSON);
            CreateIndexResponse response = client.indices().create(request, RequestOptions.DEFAULT);
            if (!response.isAcknowledged()) {
                throw exception0(GlobalErrorCodeConstants.INTERNAL_SERVER_ERROR.getCode(),"创建ES索引未被集群确认: {}",indexName());
            }
        } catch (IOException e) {
            throw exception0(GlobalErrorCodeConstants.INTERNAL_SERVER_ERROR.getCode(),"创建ES索引失败: {}",indexName());

        }
    }

    /**
     * 批量写入歌曲文档。
     *
     * @param documents ES 歌曲文档
     * @return Bulk 写入结果
     */
    public BulkWriteResult bulkIndex(List<IndexSongEsDocument> documents) {
        if (CollectionUtils.isEmpty(documents)) {
            return BulkWriteResult.empty();
        }

        // 使用业务 ID 作为 _id，重复同步时覆盖旧文档
        BulkRequest request = new BulkRequest();
        for (IndexSongEsDocument document : documents) {
            if (document == null || StringUtils.isBlank(document.getId())) {
                throw exception0(GlobalErrorCodeConstants.INTERNAL_SERVER_ERROR.getCode(),"Bulk文档或业务ID为空");
            }
            Map<String, Object> source = converter.toSource(document);
            request.add(new IndexRequest(indexName())
                    .id(document.getId())
                    .source(source));
        }

        try {
            BulkResponse response = client.bulk(request, RequestOptions.DEFAULT);
            return parseBulkResponse(response);
        } catch (IOException e) {
            throw exception0(GlobalErrorCodeConstants.INTERNAL_SERVER_ERROR.getCode(),"ES Bulk请求失败");

        }
    }

    /**
     * 刷新目标索引，使同步数据立即可查。
     */
    public void refresh() {
        try {
            RefreshRequest request = new RefreshRequest(indexName());
            client.indices().refresh(request, RequestOptions.DEFAULT);
        } catch (IOException e) {
            throw exception0(GlobalErrorCodeConstants.INTERNAL_SERVER_ERROR.getCode(),"刷新ES索引失败: {}",indexName());

        }
    }

    /**
     * 统计目标索引文档数。
     *
     * @return ES 文档数
     */
    public long count() {
        try {
            CountRequest request = new CountRequest(indexName());
            return client.count(request, RequestOptions.DEFAULT).getCount();
        } catch (IOException e) {
            throw exception0(GlobalErrorCodeConstants.INTERNAL_SERVER_ERROR.getCode(),"统计ES文档数失败: {}",indexName());

        }
    }

    /**
     * 执行 ES 搜索请求。
     *
     * @param request ES 搜索请求
     * @return ES 搜索响应
     */
    public SearchResponse search(SearchRequest request) {
        try {
            return client.search(request, RequestOptions.DEFAULT);
        } catch (ElasticsearchStatusException e) {
            RestStatus status = e.status();
            if (status == RestStatus.NOT_FOUND
                    || status == RestStatus.TOO_MANY_REQUESTS
                    || status == RestStatus.SERVICE_UNAVAILABLE) {
                throw exception0(GlobalErrorCodeConstants.INTERNAL_SERVER_ERROR.getCode(),"ES查询不可用, status= {}",status);
            }
            throw e;
        } catch (IOException e) {
            throw exception0(GlobalErrorCodeConstants.INTERNAL_SERVER_ERROR.getCode(),"ES连接失败或查询超时");
        }
    }

    private BulkWriteResult parseBulkResponse(BulkResponse response) {
        BulkWriteResult result = new BulkWriteResult();
        StringJoiner failureMessages = new StringJoiner("; ");

        // Bulk 可能部分成功，需要逐条统计失败项
        for (BulkItemResponse item : response.getItems()) {
            if (!item.isFailed()) {
                result.setSuccessCount(result.getSuccessCount() + 1);
                continue;
            }

            result.setFailureCount(result.getFailureCount() + 1);
            if (result.getFailureIds().size() < MAX_FAILURE_IDS) {
                result.getFailureIds().add(item.getId());
                failureMessages.add("id=" + item.getId()
                        + ", reason=" + item.getFailureMessage());
            }
        }

        result.setFailureMessage(failureMessages.length() == 0
                ? null : failureMessages.toString());
        return result;
    }

    private String readClasspath(String path) {
        ClassPathResource resource = new ClassPathResource(path);
        try (InputStream input = resource.getInputStream()) {
            return StreamUtils.copyToString(input, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw exception0(GlobalErrorCodeConstants.INTERNAL_SERVER_ERROR.getCode(),"读取ES Mapping失败: {}",path);
        }
    }

    private String indexName() {
        String indexName = properties.getElasticsearch().getIndexName();
        if (StringUtils.isBlank(indexName)
                || !indexName.matches("^[a-z0-9][a-z0-9_-]{0,254}$")) {
            throw exception0(GlobalErrorCodeConstants.INTERNAL_SERVER_ERROR.getCode(),"非法ES索引名: {}",indexName);
        }
        return indexName;
    }
}