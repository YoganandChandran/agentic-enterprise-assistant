package com.yoganand.agenticenterpriseassistant.agent.node;

import com.yoganand.agenticenterpriseassistant.agent.RagAgentState;
import dev.langchain4j.model.chat.ChatModel;
import lombok.RequiredArgsConstructor;
import org.bsc.langgraph4j.action.NodeAction;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class RetrievalEvaluatorNode
        implements NodeAction<RagAgentState> {

    private final ChatModel chatModel;

    @Override
    public Map<String, Object> apply(
            RagAgentState state
    ) {

        String question = state.getQuestion();

        String retrievedContext =
                state.getRetrievedContext();

        String evaluationPrompt = """
                You are a retrieval evaluation agent.

                Your task is to determine whether the retrieved
                context contains enough relevant information to
                answer the user's question.

                User Question:
                %s

                Retrieved Context:
                %s

                Respond with ONLY one of these exact values:

                SUFFICIENT
                INSUFFICIENT

                Choose SUFFICIENT when the retrieved context contains
                enough relevant information to answer the question.

                Choose INSUFFICIENT when the retrieved context is empty,
                irrelevant, or does not contain enough information.
                """.formatted(
                question,
                retrievedContext
        );

        String decision =
                chatModel.chat(evaluationPrompt)
                        .trim()
                        .toUpperCase(Locale.ROOT);

        if (!decision.equals("SUFFICIENT")
                && !decision.equals("INSUFFICIENT")) {

            decision = "INSUFFICIENT";
        }

        System.out.println(
                "\n====== RETRIEVAL EVALUATION ======"
        );

        System.out.println(
                "Question: " + question
        );

        System.out.println(
                "Decision: " + decision
        );

        System.out.println(
                "==================================\n"
        );

        return Map.of(
                "retrievalDecision",
                decision
        );
    }
}