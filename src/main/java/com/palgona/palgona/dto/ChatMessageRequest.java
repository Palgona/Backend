package com.palgona.palgona.dto;

public record ChatMessageRequest(Long senderId, Long receiverId, String message, Long roomId) {
}
