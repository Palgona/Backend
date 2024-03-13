package com.palgona.palgona.dto.chat;

public record ChatMessageRequest(Long senderId, Long receiverId, String message, Long roomId) {
}
