package com.palgona.palgona.repository;

import com.palgona.palgona.domain.chat.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
}
