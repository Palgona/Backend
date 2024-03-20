package com.palgona.palgona.repository;

import com.palgona.palgona.domain.chat.ChatMessage;
import com.palgona.palgona.domain.chat.ChatRoom;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> findAllByRoom(ChatRoom room);

    @Query("SELECT cm FROM ChatMessage cm WHERE cm.room.id = :roomId AND cm.id > :cursor ORDER BY cm.id ASC")
    List<ChatMessage> findMessagesAfterCursor(@Param("roomId") Long roomId, @Param("cursor") Long cursor);
}
