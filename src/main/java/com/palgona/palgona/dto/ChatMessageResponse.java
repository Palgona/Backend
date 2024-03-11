package com.palgona.palgona.dto;

import com.palgona.palgona.domain.chat.ChatMessage;
import com.palgona.palgona.domain.chat.ChatRoom;
import com.palgona.palgona.domain.member.Member;

public record ChatMessageResponse(Long senderId, Long receiverId, String message, Long roomId) {
    public static ChatMessageResponse of(ChatMessage message) {
        return new ChatMessageResponse(message.getSender().getId(), message.getReceiver().getId(), message.getMessage()+"message", message.getRoom().getId());
    }
}
