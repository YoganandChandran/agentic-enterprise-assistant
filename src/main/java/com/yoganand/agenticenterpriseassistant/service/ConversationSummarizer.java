package com.yoganand.agenticenterpriseassistant.service;

import com.yoganand.agenticenterpriseassistant.model.ConversationMessage;

import java.util.List;

public interface ConversationSummarizer {

    String summarize(
            String existingSummary,
            List<ConversationMessage> messages
    );
}