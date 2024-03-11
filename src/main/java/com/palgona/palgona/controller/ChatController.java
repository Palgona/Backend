package com.palgona.palgona.controller;

import com.palgona.palgona.common.dto.CustomMemberDetails;
import com.palgona.palgona.domain.chat.ChatRoom;
import com.palgona.palgona.domain.member.Member;
import com.palgona.palgona.domain.member.Role;
import com.palgona.palgona.domain.member.Status;
import com.palgona.palgona.dto.ChatMessageRequest;
import com.palgona.palgona.dto.ChatRoomCreateRequest;
import com.palgona.palgona.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
@RequestMapping("/api/v1/chats")
public class ChatController {
    private final ChatService chatService;
    @MessageMapping("/sendMessage")
    public void sendMessage(@Payload ChatMessageRequest message) {
        chatService.sendMessage(message);
    }

    @PostMapping("/rooms")
    public ResponseEntity<ChatRoom> createChatRoom(@RequestBody ChatRoomCreateRequest request, @AuthenticationPrincipal CustomMemberDetails member) {
        ChatRoom room = chatService.createRoom(Member.of(1, Status.ACTIVE,"1", Role.USER), request);

        // 생성된 채팅방 정보를 클라이언트에게 응답
        return new ResponseEntity<>(room, HttpStatus.CREATED);
    }
}
