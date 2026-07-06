package cn.org.starpivot.ai.service;

import cn.org.starpivot.ai.domain.vo.RagRetrievalResult;

public interface AiKnowledgeRetrievalService {

    RagRetrievalResult retrieve(String query, int topK);

    /** @deprecated 使用 {@link #retrieve(String, int)} */
    default String retrieveContext(String query, int topK) {
        return retrieve(query, topK).getContext();
    }
}
