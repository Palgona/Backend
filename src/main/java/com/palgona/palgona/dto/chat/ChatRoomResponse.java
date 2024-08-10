package com.palgona.palgona.dto.chat;

import com.palgona.palgona.domain.chat.ChatRoom;

public record ChatRoomResponse(Long id,
                               Long senderId,
                               Long receiverId,
                               boolean isLeaveSender,
                               boolean isLeaveReceiver,
                               Long productId) {
    public static ChatRoomResponse from(ChatRoom chatRoom) {
        return new ChatRoomResponse(chatRoom.getId(),
                chatRoom.getSender().getId(),
                chatRoom.getReceiver().getId(),
                chatRoom.isLeaveSender(),
                chatRoom.isLeaveReceiver(),
                chatRoom.getProduct().getId());
    }
}
