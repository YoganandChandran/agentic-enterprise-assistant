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
public class RouterNode implements NodeAction<RagAgentState> {

    private final ChatModel chatModel;

    @Override
    public Map<String, Object> apply(RagAgentState state) {

        String question = state.getQuestion();

        String routingPrompt = """
                You are an intelligent routing agent for an enterprise assistant.

                Analyze the user's question and choose exactly ONE route.

                Available routes:

                RAG
                Use this when the answer requires information from
                enterprise documents, policies, knowledge base, or
                internal documentation.

                MCP
                Use this when the answer requires data or an action
                from an external enterprise tool or system.

                DIRECT
                Use this for general conversation, greetings, or
                questions that can be answered using conversation
                memory without retrieving documents or calling tools.

                IMPORTANT:
                Respond with ONLY one of these exact values:

                RAG
                MCP
                DIRECT

                User Question:
                %s
                """.formatted(question);

        String route =
                chatModel.chat(routingPrompt)
                        .trim()
                        .toUpperCase(Locale.ROOT);

        // Safety fallback
        if (!route.equals("RAG")
                && !route.equals("MCP")
                && !route.equals("DIRECT")) {

            route = "DIRECT";
        }

        System.out.println(
                "\n========== AGENTIC ROUTER DECISION =========="
        );

        System.out.println(
                "Question: " + question
        );

        System.out.println(
                "Selected Route: " + route
        );

        System.out.println(
                "==============================================\n"
        );

        return Map.of(
                "route",
                route
        );
    }
}