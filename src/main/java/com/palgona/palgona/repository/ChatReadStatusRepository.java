package com.palgona.palgona.repository;

import com.palgona.palgona.domain.chat.ChatReadStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatReadStatusRepository extends JpaRepository<ChatReadStatus, Long> {
    ChatReadStatus findByMemberIdAndRoomId(Long id, Long roomId);
}
