package com.palgona.palgona.dto.chat;

public record ChatRoomCountResponse(
        Long id,
        Long senderId,
        Long receiverId,
        Long unreadMessageCount
) {
}
