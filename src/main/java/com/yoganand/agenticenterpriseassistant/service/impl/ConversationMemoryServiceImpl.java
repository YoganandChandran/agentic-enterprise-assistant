package com.yoganand.agenticenterpriseassistant.service.impl;

import com.yoganand.agenticenterpriseassistant.model.Conversation;
import com.yoganand.agenticenterpriseassistant.model.ConversationMessage;
import com.yoganand.agenticenterpriseassistant.model.MessageRole;
import com.yoganand.agenticenterpriseassistant.repository.ConversationMessageRepository;
import com.yoganand.agenticenterpriseassistant.repository.ConversationRepository;
import com.yoganand.agenticenterpriseassistant.service.ConversationMemoryService;
import com.yoganand.agenticenterpriseassistant.service.ConversationSummarizer;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class ConversationMemoryServiceImpl
        implements ConversationMemoryService {

    private final ConversationRepository conversationRepository;
    private final ConversationMessageRepository messageRepository;
    private final ConversationSummarizer conversationSummarizer;

    @Value("${conversation.memory.summarization-threshold:20}")
    private int summarizationThreshold;

    @Override
    @Transactional(readOnly = true)
    public List<ConversationMessage> getRecentMessages(
            String userId
    ) {

        return conversationRepository
                .findByUserId(userId)
                .map(conversation -> {

                    List<ConversationMessage> messages =
                            messageRepository
                                    .findTop10ByConversationIdOrderByCreatedAtDesc(
                                            conversation.getId()
                                    );

                    Collections.reverse(messages);

                    return messages;
                })
                .orElse(Collections.emptyList());
    }

    @Override
    public void saveUserMessage(
            String userId,
            String message
    ) {

        Conversation conversation =
                getOrCreateConversation(userId);

        saveMessage(
                conversation,
                MessageRole.USER,
                message
        );
    }

    @Override
    public void saveAssistantMessage(
            String userId,
            String message
    ) {

        Conversation conversation =
                getOrCreateConversation(userId);

        saveMessage(
                conversation,
                MessageRole.ASSISTANT,
                message
        );
    }

    @Override
    @Transactional(readOnly = true)
    public String getConversationSummary(
            String userId
    ) {

        return conversationRepository
                .findByUserId(userId)
                .map(Conversation::getSummary)
                .orElse(null);
    }

    private Conversation getOrCreateConversation(
            String userId
    ) {

        return conversationRepository
                .findByUserId(userId)
                .orElseGet(() -> {

                    Conversation conversation =
                            new Conversation();

                    conversation.setUserId(userId);

                    return conversationRepository.save(
                            conversation
                    );
                });
    }

    private void saveMessage(
            Conversation conversation,
            MessageRole role,
            String content
    ) {

        ConversationMessage message =
                new ConversationMessage();

        message.setConversation(conversation);
        message.setRole(role);
        message.setContent(content);

        messageRepository.save(message);

        conversation.touch();

        conversationRepository.save(conversation);

        summarizeIfRequired(conversation);
    }

    private void summarizeIfRequired(
            Conversation conversation
    ) {

        long messageCount =
                messageRepository.countByConversationId(
                        conversation.getId()
                );

        if (messageCount <= summarizationThreshold) {
            return;
        }

        List<ConversationMessage> messages =
                messageRepository
                        .findByConversationIdOrderByCreatedAtAsc(
                                conversation.getId()
                        );

        int keepRecentMessages = 10;

        int messagesToSummarize =
                messages.size() - keepRecentMessages;

        if (messagesToSummarize <= 0) {
            return;
        }

        List<ConversationMessage> oldMessages =
                messages.subList(
                        0,
                        messagesToSummarize
                );

        String newSummary =
                conversationSummarizer.summarize(
                        conversation.getSummary(),
                        oldMessages
                );

        conversation.setSummary(newSummary);

        conversation.touch();

        conversationRepository.save(conversation);

        List<Long> idsToDelete =
                oldMessages.stream()
                        .map(ConversationMessage::getId)
                        .toList();

        messageRepository
                .deleteByConversationIdAndIdIn(
                        conversation.getId(),
                        idsToDelete
                );
    }
}