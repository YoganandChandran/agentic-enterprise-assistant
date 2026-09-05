package com.yoganand.agenticenterpriseassistant.service;

import com.yoganand.agenticenterpriseassistant.model.ConversationMessage;

import java.util.List;

public interface ConversationMemoryService {

    List<ConversationMessage> getRecentMessages(String userId);

    void saveUserMessage(
            String userId,
            String message
    );

    void saveAssistantMessage(
            String userId,
            String message
    );

    String getConversationSummary(String userId);
}