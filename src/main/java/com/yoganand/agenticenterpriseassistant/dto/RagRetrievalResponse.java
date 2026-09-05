package com.yoganand.agenticenterpriseassistant.dto;

import java.util.List;

public record RagRetrievalResponse(

        String query,

        List<String> relevantChunks,

        boolean hasRelevantContext

) {
}