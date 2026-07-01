package cn.org.starpivot.mall.config;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import java.util.Arrays;

@Slf4j
@Configuration
@ConditionalOnProperty(prefix = "starpivot.mall.elasticsearch", name = "enabled", havingValue = "true")
public class MallElasticsearchConfiguration {

    @Bean(destroyMethod = "close")
    RestClient elasticsearchRestClient(MallElasticsearchProperties properties) {
        HttpHost[] hosts = Arrays.stream(properties.getUris().split(","))
                .map(String::trim)
                .filter(StringUtils::hasText)
                .map(HttpHost::create)
                .toArray(HttpHost[]::new);
        if (hosts.length == 0) {
            throw new IllegalStateException("starpivot.mall.elasticsearch.uris 未配置有效地址");
        }
        log.info("Elasticsearch client enabled, uris={}", properties.getUris());
        return RestClient.builder(hosts).build();
    }

    @Bean
    ElasticsearchClient elasticsearchClient(RestClient elasticsearchRestClient) {
        RestClientTransport transport = new RestClientTransport(elasticsearchRestClient, new JacksonJsonpMapper());
        return new ElasticsearchClient(transport);
    }
}
