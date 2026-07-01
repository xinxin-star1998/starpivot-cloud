package cn.org.starpivot.mall.pms.search;

import cn.org.starpivot.mall.config.MallElasticsearchProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "starpivot.mall.elasticsearch", name = "reindex-on-startup", havingValue = "true")
public class PmsProductSearchIndexRunner implements ApplicationRunner {

    private final PmsProductSearchSyncService productSearchSyncService;
    private final MallElasticsearchProperties elasticsearchProperties;

    @Override
    public void run(ApplicationArguments args) {
        if (!elasticsearchProperties.isEnabled()) {
            return;
        }
        int count = productSearchSyncService.reindexAllPublished();
        log.info("Elasticsearch startup reindex finished, count={}", count);
    }
}
