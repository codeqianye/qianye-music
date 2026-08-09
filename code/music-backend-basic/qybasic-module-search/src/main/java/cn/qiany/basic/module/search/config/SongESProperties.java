package cn.qiany.basic.module.search.config;

import cn.qiany.basic.module.search.enums.SearchEngineType;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Data
@Component
@ConfigurationProperties(prefix = "search.song")
public class SongESProperties {

    private SearchEngineType engine;

    private Elasticsearch elasticsearch = new Elasticsearch();

    @Data
    public static class Elasticsearch {

        private List<String> uris = new ArrayList<>();

        private String username;

        private String password;

        private String indexName;

        private Integer connectTimeoutMs;

        private Integer socketTimeoutMs;

        private Integer queryTimeoutMs;

        private Integer syncBatchSize;

        private Integer maxResultWindow;
    }
}