package com.yoganand.agenticenterpriseassistant.agent.node;

import com.yoganand.agenticenterpriseassistant.agent.RagAgentState;
import dev.langchain4j.model.chat.ChatModel;
import lombok.RequiredArgsConstructor;
import org.bsc.langgraph4j.action.NodeAction;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class QueryRewriteNode
        implements NodeAction<RagAgentState> {

    private final ChatModel chatModel;

    @Override
    public Map<String, Object> apply(RagAgentState state) {

        String originalQuestion =
                state.getQuestion();

        String retrievedContext =
                state.getRetrievedContext();

        String rewritePrompt = """
                You are a query rewriting agent.

                The previous retrieval attempt did not provide
                enough relevant information to answer the user's question.

                Your task is to rewrite the user's question into a
                better search query that can retrieve more relevant
                information from the knowledge base.

                Original User Question:
                %s

                Previous Retrieved Context:
                %s

                Create a concise and specific search query.

                Do not answer the question.
                Do not explain your reasoning.
                Return ONLY the rewritten search query.
                """.formatted(
                originalQuestion,
                retrievedContext
        );

        String rewrittenQuery =
                chatModel.chat(rewritePrompt).trim();

        System.out.println(
                "\n====== QUERY REWRITE ======"
        );

        System.out.println(
                "Original Query: " + originalQuestion
        );

        System.out.println(
                "Rewritten Query: " + rewrittenQuery
        );

        System.out.println(
                "===========================\n"
        );

        return Map.of(
                "rewrittenQuery",
                rewrittenQuery
        );
    }
}