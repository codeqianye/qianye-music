package cn.qiany.basic.module.search.config;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 建立es客户端连接
 */
@Configuration
public class ElasticsearchConfig {

    @Bean(destroyMethod = "close")
    public RestHighLevelClient restHighLevelClient(
            SongElasticsearchProperties properties) {
        SongElasticsearchProperties.Elasticsearch config =
                properties.getElasticsearch();

        List<HttpHost> httpHosts = config.getUris()
                .stream()
                .map(HttpHost::create)
                .collect(Collectors.toList());

        RestClientBuilder builder = RestClient.builder(
                httpHosts.toArray(new HttpHost[0])
        );

        builder.setRequestConfigCallback(requestConfigBuilder ->
                requestConfigBuilder
                        .setConnectTimeout(config.getConnectTimeoutMs())
                        .setSocketTimeout(config.getSocketTimeoutMs())
        );

        if (StringUtils.isNotBlank(config.getUsername())) {
            CredentialsProvider credentialsProvider =
                    new BasicCredentialsProvider();

            credentialsProvider.setCredentials(
                    AuthScope.ANY,
                    new UsernamePasswordCredentials(
                            config.getUsername(),
                            config.getPassword()
                    )
            );

            builder.setHttpClientConfigCallback(httpClientBuilder ->
                    httpClientBuilder
                            .setDefaultCredentialsProvider(credentialsProvider)
            );
        }

        return new RestHighLevelClient(builder);
    }
}