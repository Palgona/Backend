package com.palgona.palgona.dto.chat;

public record ChatMessageRequest(Long senderId, Long receiverId, Long roomId, String message, String imgData) {
}
