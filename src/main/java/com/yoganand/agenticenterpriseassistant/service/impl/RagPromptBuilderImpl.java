package com.yoganand.agenticenterpriseassistant.service.impl;

import com.yoganand.agenticenterpriseassistant.model.ConversationMessage;
import com.yoganand.agenticenterpriseassistant.service.RagPromptBuilder;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class RagPromptBuilderImpl
        implements RagPromptBuilder {

    @Override
    public String buildPrompt(
            String question,
            List<String> relevantChunks,
            String conversationSummary,
            List<ConversationMessage> recentMessages
    ) {

        String documentContext = String.join(
                "\n\n",
                relevantChunks
        );

        String memoryContext = buildMemoryContext(
                conversationSummary,
                recentMessages
        );

        return """
                You are an Enterprise AI Assistant.

                Answer the user's question using the provided
                conversation memory and document context.

                IMPORTANT RULES:

                1. Use conversation memory to understand follow-up questions.
                2. Use document context for factual answers.
                3. Do not invent information.
                4. If the answer is not available in the document context,
                   say:
                   "I don't have enough information in the uploaded documents."
                5. Keep answers concise and clear.

                CONVERSATION MEMORY:
                %s

                DOCUMENT CONTEXT:
                %s

                CURRENT USER QUESTION:
                %s

                ANSWER:
                """.formatted(
                memoryContext,
                documentContext,
                question
        );
    }

    private String buildMemoryContext(
            String summary,
            List<ConversationMessage> recentMessages
    ) {

        StringBuilder memory = new StringBuilder();

        if (summary != null && !summary.isBlank()) {

            memory.append("CONVERSATION SUMMARY:\n")
                    .append(summary)
                    .append("\n\n");
        }

        if (!recentMessages.isEmpty()) {

            memory.append("RECENT MESSAGES:\n");

            for (ConversationMessage message : recentMessages) {

                memory.append(
                                message.getRole()
                        )
                        .append(": ")
                        .append(
                                message.getContent()
                        )
                        .append("\n");
            }
        }

        if (memory.isEmpty()) {

            return "No previous conversation.";
        }

        return memory.toString();
    }
}