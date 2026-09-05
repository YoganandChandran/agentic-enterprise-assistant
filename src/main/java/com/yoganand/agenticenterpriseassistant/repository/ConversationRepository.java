package com.yoganand.agenticenterpriseassistant.repository;

import com.yoganand.agenticenterpriseassistant.model.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ConversationRepository
        extends JpaRepository<Conversation, Long> {

    Optional<Conversation> findByUserId(String userId);

}