package com.yoganand.agenticenterpriseassistant.service.impl;

import com.yoganand.agenticenterpriseassistant.agent.RagAgentState;
import com.yoganand.agenticenterpriseassistant.agent.graph.AgentGraph;
import com.yoganand.agenticenterpriseassistant.model.ConversationMessage;
import com.yoganand.agenticenterpriseassistant.service.ConversationMemoryService;
import com.yoganand.agenticenterpriseassistant.service.RagAnswerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class RagAnswerServiceImpl
        implements RagAnswerService {

    private final AgentGraph agentGraph;

    private final ConversationMemoryService conversationMemoryService;

    @Override
    public String getAnswer(
            String userId,
            String question
    ) {

        // 1. Load persistent conversation memory
        List<ConversationMessage> recentMessages =
                conversationMemoryService
                        .getRecentMessages(userId);

        String conversationSummary =
                conversationMemoryService
                        .getConversationSummary(userId);

        // 2. Save current user question
        conversationMemoryService
                .saveUserMessage(userId, question);

        // 3. Convert recent messages into simple strings
        List<String> recentMessageTexts =
                recentMessages.stream()
                        .map(message ->
                                message.getRole()
                                        + ": "
                                        + message.getContent()
                        )
                        .toList();

        // 4. Create initial LangGraph state
        Map<String, Object> input =
                new HashMap<>();

        input.put("userId", userId);
        input.put("question", question);
        input.put(
                "conversationSummary",
                conversationSummary
        );
        input.put(
                "recentMessages",
                recentMessageTexts
        );

        // 5. Execute LangGraph
        RagAgentState finalState =
                agentGraph.getGraph()
                        .invoke(input)
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "LangGraph execution returned no state"
                                )
                        );

        // 6. Get answer from final graph state
        String answer =
                finalState.getAnswer();

        // 7. Save assistant response
        conversationMemoryService
                .saveAssistantMessage(
                        userId,
                        answer
                );

        return answer;
    }
}