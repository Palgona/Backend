package com.palgona.palgona.dto.chat;

public record ChatRoomResponse(
        Long id,
        Long senderId,
        Long receiverId
) {
}
