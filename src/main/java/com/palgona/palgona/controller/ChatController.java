package com.palgona.palgona.controller;

import com.palgona.palgona.common.dto.CustomMemberDetails;
import com.palgona.palgona.domain.chat.ChatMessage;
import com.palgona.palgona.domain.chat.ChatRoom;
import com.palgona.palgona.dto.chat.*;
import com.palgona.palgona.service.ChatService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
@RequestMapping("/api/v1/chats")
public class ChatController {
    private final ChatService chatService;
    private final SimpMessageSendingOperations messagingTemplate;

    // TODO
    // 1번 멤버로 강제로 바꾸는 코드 없애야함.

    private static ChatRoomResponse mapToResponse(ChatRoom chatRoom) {
        return new ChatRoomResponse(chatRoom.getId(), chatRoom.getSender().getId(), chatRoom.getReceiver().getId());
    }

    private static List<ChatRoomResponse> mapListToResponse(List<ChatRoom> chatRooms) {
        return chatRooms.stream().map(ChatController::mapToResponse).collect(Collectors.toList());
    }

    @MessageMapping("/sendMessage")
    @Operation(summary = "채팅 발송 api", description = "socket통신으로 체팅을 받아 발송한다.")
    public void sendMessage(@Payload ChatMessageRequest message) {
        ChatMessage savedMessage = chatService.sendMessage(message);
        ChatMessageResponse messageResponse = ChatMessageResponse.from(savedMessage);
        messagingTemplate.convertAndSend("/sub/topic/chat/" + savedMessage.getRoom().getId(), messageResponse);
    }

    @PostMapping
    @Operation(summary = "채팅방 생성 api", description = "사용자와 상대방의 방이 있는지 확인하고 방을 리턴한다.")
    public ResponseEntity<ChatRoomResponse> createChatRoom(@RequestBody ChatRoomCreateRequest request, @AuthenticationPrincipal CustomMemberDetails member) {
        ChatRoom room = chatService.createRoom(member.getMember(), request);

        return ResponseEntity.ok(mapToResponse(room));
    }

    @GetMapping
    @Operation(summary = "채팅방 목록 조회 api", description = "현재 유저의 모든 채팅방 목록과 안읽은 채팅수를 불러온다.")
    public ResponseEntity<List<ChatRoomCountResponse>> readChatRooms(@AuthenticationPrincipal CustomMemberDetails member) {
        List<ChatRoomCountResponse> rooms = chatService.getRoomList(member.getMember());

        return ResponseEntity.ok(rooms);
    }

    @GetMapping("/{roomId}")
    @Operation(summary = "채팅방의 채팅 목록 조회 api", description = "현재 채팅방의 채팅 목록을 불러온다.")
    public ResponseEntity<List<ChatMessageResponse>> readRoomChat(@AuthenticationPrincipal CustomMemberDetails member, @PathVariable Long roomId) {
        List<ChatMessage> chats = chatService.getMessageByRoom(member.getMember(), roomId);
        List<ChatMessageResponse> responses = chats.stream().map(ChatMessageResponse::from).toList();

        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{roomId}/unread")
    @Operation(summary = "채팅방의 안읽은 채팅 목록 조회 api", description = "현재 채팅방의 안읽은 채팅 목록을 불러온다.")
    public ResponseEntity<List<ChatMessageResponse>> readUnreadRoomChat(@AuthenticationPrincipal CustomMemberDetails member, @PathVariable Long roomId) {
        List<ChatMessage> chats = chatService.getUnreadMessagesByRoom(member.getMember(), roomId);
        List<ChatMessageResponse> responses = chats.stream().map(ChatMessageResponse::from).toList();

        return ResponseEntity.ok(responses);
    }

    @PostMapping("/{roomId}/exit")
    @Operation(summary = "채팅방 나가기 api", description = "현재 채팅방을 나간다.")
    public ResponseEntity<String> exitChatRoom(@AuthenticationPrincipal CustomMemberDetails member, @PathVariable Long roomId) {
        // TODO

        return ResponseEntity.ok(null);
    }

    @PostMapping("/{roomId}/image")
    @Operation(summary = "채팅방 이미지 업로드 api", description = "채팅방의 image를 업로드한다.")
    public ResponseEntity<String> uploadChatImage(@RequestPart(value = "files") List<MultipartFile> files, @AuthenticationPrincipal CustomMemberDetails member) {
        // TODO

        return ResponseEntity.ok(null);
    }

    @PostMapping("/read")
    @Operation(summary = "읽은 채팅 최신화 api", description = "가장 마지막에 읽은 채팅을 표시한다.")
    public ResponseEntity<String> readMessage(@AuthenticationPrincipal CustomMemberDetails member, @RequestBody ReadMessageRequest request) {
        chatService.readMessage(member.getMember(), request);

        return ResponseEntity.ok(null);
    }
}
