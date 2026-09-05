package com.yoganand.agenticenterpriseassistant.repository;

import com.yoganand.agenticenterpriseassistant.model.ConversationMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ConversationMessageRepository
        extends JpaRepository<ConversationMessage, Long> {

    List<ConversationMessage>
    findByConversationIdOrderByCreatedAtAsc(
            Long conversationId
    );

    List<ConversationMessage>
    findTop10ByConversationIdOrderByCreatedAtDesc(
            Long conversationId
    );

    long countByConversationId(Long conversationId);

    void deleteByConversationIdAndIdIn(
            Long conversationId,
            List<Long> ids
    );

}