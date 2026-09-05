package com.yoganand.agenticenterpriseassistant.agent.node;

import com.yoganand.agenticenterpriseassistant.agent.RagAgentState;
import com.yoganand.agenticenterpriseassistant.dto.RagRetrievalResponse;
import com.yoganand.agenticenterpriseassistant.service.RagRetrievalService;
import lombok.RequiredArgsConstructor;
import org.bsc.langgraph4j.action.NodeAction;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class RagRetrievalNode implements NodeAction<RagAgentState> {

    private final RagRetrievalService ragRetrievalService;

    @Override
    public Map<String, Object> apply(RagAgentState state) {

        String originalQuestion =
                state.getQuestion();

        String rewrittenQuery =
                state.getRewrittenQuery();

        String retrievalQuery =
                rewrittenQuery.isBlank()
                        ? originalQuestion
                        : rewrittenQuery;

        int retrievalAttempt =
                state.getRetrievalAttempt() + 1;

        System.out.println(
                "\n====== RAG RETRIEVAL ======"
        );

        System.out.println(
                "Attempt: " + retrievalAttempt
        );

        System.out.println(
                "Retrieval Query: " + retrievalQuery
        );

        RagRetrievalResponse response =
                ragRetrievalService.retrieve(
                        retrievalQuery
                );

        String context =
                response.hasRelevantContext()
                        ? String.join(
                        "\n\n",
                        response.relevantChunks()
                )
                        : "";

        System.out.println(
                "Retrieved Chunks: "
                        + response.relevantChunks().size()
        );

        System.out.println(
                "Has Relevant Context: "
                        + response.hasRelevantContext()
        );

        System.out.println(
                "===========================\n"
        );

        return Map.of(
                "retrievedContext",
                context,
                "retrievalAttempt",
                retrievalAttempt
        );
    }
}