package cn.org.starpivot.mall.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 商城 Elasticsearch 配置。
 */
@Data
@Component
@ConfigurationProperties(prefix = "starpivot.mall.elasticsearch")
public class MallElasticsearchProperties {

    /** 是否启用 ES 搜索（false 时 C 端仍走 MySQL LIKE） */
    private boolean enabled = false;

    /** 集群地址，多个用逗号分隔 */
    private String uris = "http://127.0.0.1:9200";

    /** 商品索引名 */
    private String indexName = "mall_product";

    /** 启动时全量重建索引（仅开发/运维场景） */
    private boolean reindexOnStartup = false;
}
