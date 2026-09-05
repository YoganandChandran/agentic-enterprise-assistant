package com.yoganand.agenticenterpriseassistant.service.impl;

import com.yoganand.agenticenterpriseassistant.service.EmbeddingService;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingSearchResult;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EmbeddingServiceImpl implements EmbeddingService {

    private final EmbeddingModel embeddingModel;
    private final EmbeddingStore<TextSegment> embeddingStore;

    public EmbeddingServiceImpl (
            EmbeddingModel embeddingModel,
            EmbeddingStore<TextSegment> embeddingStore
    ) {
        this.embeddingModel = embeddingModel;
        this.embeddingStore = embeddingStore;
    }

    public float[] embed(String text) {

        Embedding embedding = embeddingModel
                .embed(text)
                .content();

        return embedding.vector();
    }

    public void storeSegments(List<TextSegment> segments) {

        for (TextSegment segment : segments) {

            Embedding embedding = embeddingModel
                    .embed(segment)
                    .content();

            embeddingStore.add(
                    embedding,
                    segment
            );
        }
    }

    public List<String> search(String query) {

        Embedding queryEmbedding = embeddingModel
                .embed(query)
                .content();

        EmbeddingSearchRequest searchRequest =
                EmbeddingSearchRequest.builder()
                        .queryEmbedding(queryEmbedding)
                        .maxResults(5)
                        .build();

        EmbeddingSearchResult<TextSegment> searchResult =
                embeddingStore.search(searchRequest);

        return searchResult.matches()
                .stream()
                .map(match -> match.embedded().text())
                .toList();
    }

    public List<EmbeddingMatch<TextSegment>> searchWithScore(
            String query
    ) {

        Embedding queryEmbedding =
                embeddingModel
                        .embed(query)
                        .content();

        EmbeddingSearchRequest searchRequest =
                EmbeddingSearchRequest.builder()
                        .queryEmbedding(queryEmbedding)
                        .maxResults(5)
                        .build();

        EmbeddingSearchResult<TextSegment> searchResult =
                embeddingStore.search(searchRequest);

        return searchResult.matches();
    }
}
