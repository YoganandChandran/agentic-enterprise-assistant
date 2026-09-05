package com.yoganand.agenticenterpriseassistant.service;

import com.yoganand.agenticenterpriseassistant.model.ConversationMessage;

import java.util.List;

public interface RagPromptBuilder {

    String buildPrompt(
            String question,
            List<String> relevantChunks,
            String conversationSummary,
            List<ConversationMessage> recentMessages
    );
}