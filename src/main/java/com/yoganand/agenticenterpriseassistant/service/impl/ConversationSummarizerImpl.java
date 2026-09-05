package com.yoganand.agenticenterpriseassistant.service.impl;

import com.yoganand.agenticenterpriseassistant.model.ConversationMessage;
import com.yoganand.agenticenterpriseassistant.service.ConversationSummarizer;
import dev.langchain4j.model.chat.ChatModel;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ConversationSummarizerImpl
        implements ConversationSummarizer {

    private final ChatModel chatModel;

    public ConversationSummarizerImpl(
            ChatModel chatModel
    ) {
        this.chatModel = chatModel;
    }

    @Override
    public String summarize(
            String existingSummary,
            List<ConversationMessage> messages
    ) {

        StringBuilder conversation =
                new StringBuilder();

        if (existingSummary != null
                && !existingSummary.isBlank()) {

            conversation.append(
                    "Existing conversation summary:\n"
            );

            conversation.append(existingSummary)
                    .append("\n\n");
        }

        conversation.append(
                "Conversation messages:\n"
        );

        for (ConversationMessage message : messages) {

            conversation.append(
                            message.getRole()
                    )
                    .append(": ")
                    .append(message.getContent())
                    .append("\n");
        }

        String prompt = """
                You are a conversation summarization assistant.

                Summarize the conversation below into a concise
                factual summary.

                Preserve:
                - Important user preferences
                - Questions asked
                - Important facts
                - Decisions
                - Important context required for future questions

                Do not invent information.

                %s

                Summary:
                """.formatted(conversation);

        return chatModel.chat(prompt);
    }
}