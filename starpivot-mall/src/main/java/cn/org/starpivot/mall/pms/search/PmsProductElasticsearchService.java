package cn.org.starpivot.mall.pms.search;

import cn.org.starpivot.common.entity.PageResponse;
import cn.org.starpivot.mall.config.MallElasticsearchProperties;
import cn.org.starpivot.mall.portal.domain.bo.PortalProductSearchBo;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.BulkRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.bulk.BulkOperation;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch.indices.CreateIndexRequest;
import co.elastic.clients.elasticsearch.indices.ExistsRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@ConditionalOnBean(ElasticsearchClient.class)
@RequiredArgsConstructor
public class PmsProductElasticsearchService {

    private final ElasticsearchClient elasticsearchClient;
    private final MallElasticsearchProperties properties;

    public void ensureIndex() throws IOException {
        String indexName = properties.getIndexName();
        boolean exists = elasticsearchClient.indices().exists(ExistsRequest.of(e -> e.index(indexName))).value();
        if (exists) {
            return;
        }
        elasticsearchClient.indices().create(CreateIndexRequest.of(c -> c.index(indexName)
                .mappings(m -> m.properties("spuName", p -> p.text(t -> t))
                        .properties("spuDescription", p -> p.text(t -> t))
                        .properties("brandName", p -> p.text(t -> t))
                        .properties("catalogId", p -> p.long_(l -> l))
                        .properties("brandId", p -> p.long_(l -> l))
                        .properties("price", p -> p.double_(d -> d))
                        .properties("coverImg", p -> p.keyword(k -> k))
                        .properties("publishStatus", p -> p.integer(i -> i))
                        .properties("createTime", p -> p.long_(l -> l)))));
        log.info("Created elasticsearch index: {}", indexName);
    }

    public void indexDocument(PmsProductDocument document) {
        if (document == null || document.getId() == null) {
            return;
        }
        try {
            ensureIndex();
            elasticsearchClient.index(i -> i.index(properties.getIndexName())
                    .id(String.valueOf(document.getId()))
                    .document(document));
        } catch (Exception ex) {
            log.warn("Failed to index product document id={}", document.getId(), ex);
            throw new IllegalStateException("Elasticsearch 索引写入失败", ex);
        }
    }

    public void bulkIndex(List<PmsProductDocument> documents) {
        if (documents == null || documents.isEmpty()) {
            return;
        }
        try {
            ensureIndex();
            List<BulkOperation> operations = new ArrayList<>();
            for (PmsProductDocument document : documents) {
                if (document == null || document.getId() == null) {
                    continue;
                }
                operations.add(BulkOperation.of(op -> op.index(idx -> idx.index(properties.getIndexName())
                        .id(String.valueOf(document.getId()))
                        .document(document))));
            }
            if (operations.isEmpty()) {
                return;
            }
            elasticsearchClient.bulk(BulkRequest.of(b -> b.operations(operations)));
        } catch (Exception ex) {
            log.warn("Failed to bulk index product documents", ex);
            throw new IllegalStateException("Elasticsearch 批量索引失败", ex);
        }
    }

    public void deleteById(Long spuId) {
        if (spuId == null) {
            return;
        }
        try {
            elasticsearchClient.delete(d -> d.index(properties.getIndexName()).id(String.valueOf(spuId)));
        } catch (Exception ex) {
            log.warn("Failed to delete product document id={}", spuId, ex);
        }
    }

    public PageResponse<Long> searchSpuIds(PortalProductSearchBo bo) throws IOException {
        int pageNum = bo.getPageNum() != null ? bo.getPageNum().intValue() : 1;
        int pageSize = bo.getPageSize() != null ? bo.getPageSize().intValue() : 10;
        int from = Math.max(0, (pageNum - 1) * pageSize);

        BoolQuery.Builder bool = new BoolQuery.Builder();
        bool.filter(f -> f.term(t -> t.field("publishStatus").value(1)));
        if (StringUtils.hasText(bo.getKeyword())) {
            bool.must(m -> m.multiMatch(mm -> mm.query(bo.getKeyword().trim())
                    .fields("spuName^3", "spuDescription", "brandName")));
        }
        if (bo.getCatalogId() != null) {
            bool.filter(f -> f.term(t -> t.field("catalogId").value(bo.getCatalogId())));
        }
        if (bo.getBrandId() != null) {
            bool.filter(f -> f.term(t -> t.field("brandId").value(bo.getBrandId())));
        }
        Query query = Query.of(q -> q.bool(bool.build()));

        SearchResponse<PmsProductDocument> response = elasticsearchClient.search(s -> {
            s.index(properties.getIndexName()).from(from).size(pageSize).query(query);
            applySort(s, bo.getSort());
            return s;
        }, PmsProductDocument.class);

        List<Long> ids = response.hits().hits().stream()
                .map(Hit::source)
                .filter(Objects::nonNull)
                .map(PmsProductDocument::getId)
                .filter(Objects::nonNull)
                .toList();
        long total = response.hits().total() != null ? response.hits().total().value() : ids.size();

        PageResponse<Long> page = new PageResponse<>();
        page.setTotal(total);
        page.setRows(ids);
        page.setPageNum((long) pageNum);
        page.setPageSize((long) pageSize);
        page.setPageCount(pageSize > 0 ? (long) Math.ceil((double) total / pageSize) : 0L);
        return page;
    }

    private void applySort(co.elastic.clients.elasticsearch.core.SearchRequest.Builder builder, String sort) {
        if (!StringUtils.hasText(sort)) {
            builder.sort(so -> so.field(f -> f.field("_score").order(SortOrder.Desc)));
            builder.sort(so -> so.field(f -> f.field("createTime").order(SortOrder.Desc)));
            return;
        }
        switch (sort) {
            case "priceAsc" -> builder.sort(so -> so.field(f -> f.field("price").order(SortOrder.Asc)));
            case "priceDesc" -> builder.sort(so -> so.field(f -> f.field("price").order(SortOrder.Desc)));
            case "newest" -> builder.sort(so -> so.field(f -> f.field("createTime").order(SortOrder.Desc)));
            default -> {
                builder.sort(so -> so.field(f -> f.field("_score").order(SortOrder.Desc)));
                builder.sort(so -> so.field(f -> f.field("createTime").order(SortOrder.Desc)));
            }
        }
    }
}
