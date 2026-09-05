package com.yoganand.agenticenterpriseassistant.service;


import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingMatch;

import java.util.List;

public interface EmbeddingService {

    public float[] embed(String text);
    public void storeSegments(List<TextSegment> segments);
    public List<String> search(String query);
    public List<EmbeddingMatch<TextSegment>> searchWithScore(
            String query
    );
}