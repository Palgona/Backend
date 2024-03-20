package com.palgona.palgona.dto.chat;

import com.palgona.palgona.domain.chat.ChatMessage;

public record ChatMessageResponse(Long id, Long senderId, Long receiverId, String message, Long roomId) {
    public static ChatMessageResponse from(ChatMessage message) {
        return new ChatMessageResponse(message.getId(), message.getSender().getId(), message.getReceiver().getId(), message.getMessage(), message.getRoom().getId());
    }
}
