package com.palgona.palgona.repository;

import com.palgona.palgona.domain.chat.ChatMessage;
import com.palgona.palgona.domain.chat.ChatRoom;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    @Query("SELECT cm FROM ChatMessage cm WHERE cm.room.id = :roomId AND cm.id > :cursor")
    List<ChatMessage> findUnreadMessages(@Param("roomId") Long roomId, @Param("cursor") Long cursor);
    List<ChatMessage> findAllByRoom(ChatRoom room);
}
