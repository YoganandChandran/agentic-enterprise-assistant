package com.yoganand.agenticenterpriseassistant.agent.node;

import com.yoganand.agenticenterpriseassistant.agent.RagAgentState;
import dev.langchain4j.model.chat.ChatModel;
import lombok.RequiredArgsConstructor;
import org.bsc.langgraph4j.action.NodeAction;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class LlmAnswerNode implements NodeAction<RagAgentState> {

    private final ChatModel chatModel;

    @Override
    public Map<String, Object> apply(RagAgentState state) {

        String question =
                state.getQuestion();

        String context =
                state.getRetrievedContext();

        String conversationSummary =
                state.getConversationSummary();

        List<String> recentMessages =
                state.getRecentMessages();

        String recentConversation =
                String.join(
                        "\n",
                        recentMessages
                );

        String toolResult =
                state.getToolResult();

        String prompt = """
                You are an enterprise assistant.

                You have access to:
                1. Previous conversation memory
                2. Retrieved information from enterprise documents
                3. Results from external enterprise tools
                4. The user's current question

                Use the conversation memory to understand
                references to previous discussion.

                Use the retrieved document context to answer
                questions about enterprise information.

                Use external tool results when they contain
                information required to answer the question.

                IMPORTANT:
                - Do not invent information.
                - If the user asks about the previous conversation,
                  use the conversation memory.
                - If the question requires information from documents,
                  use the retrieved context.
                - If external tool results are available,
                  use them when relevant.
                - If the required information is unavailable,
                  clearly say that you don't have enough information.

                Previous Conversation Summary:
                %s

                Recent Conversation:
                %s

                Retrieved Document Context:
                %s

                External Tool Result:
                %s

                Current User Question:
                %s
                """.formatted(
                conversationSummary,
                recentConversation,
                context,
                toolResult,
                question
        );

        String answer =
                chatModel.chat(prompt);

        return Map.of(
                "answer",
                answer
        );
    }
}