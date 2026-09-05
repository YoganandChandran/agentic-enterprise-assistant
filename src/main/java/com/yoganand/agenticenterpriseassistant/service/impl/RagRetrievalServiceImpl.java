package com.yoganand.agenticenterpriseassistant.service.impl;

import com.yoganand.agenticenterpriseassistant.dto.RagRetrievalResponse;
import com.yoganand.agenticenterpriseassistant.service.EmbeddingService;
import com.yoganand.agenticenterpriseassistant.service.RagRetrievalService;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RagRetrievalServiceImpl
        implements RagRetrievalService {

    private static final double MIN_SCORE = 0.50;

    private final EmbeddingService embeddingService;

    public RagRetrievalServiceImpl(
            EmbeddingService embeddingService
    ) {
        this.embeddingService = embeddingService;
    }

    @Override
    public RagRetrievalResponse retrieve(String query) {

        List<EmbeddingMatch<TextSegment>> matches =
                embeddingService.searchWithScore(query);

        List<String> relevantChunks =
                matches.stream()
                        .filter(match ->
                                match.score() >= MIN_SCORE
                        )
                        .map(match ->
                                match.embedded().text()
                        )
                        .toList();

        return new RagRetrievalResponse(
                query,
                relevantChunks,
                !relevantChunks.isEmpty()
        );
    }
}