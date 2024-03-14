package com.palgona.palgona.service;

import com.palgona.palgona.common.dto.response.SliceResponse;
import com.palgona.palgona.common.error.code.ChatErrorCode;
import com.palgona.palgona.common.error.code.MemberErrorCode;
import com.palgona.palgona.common.error.exception.BusinessException;
import com.palgona.palgona.domain.chat.ChatMessage;
import com.palgona.palgona.domain.chat.ChatRoom;
import com.palgona.palgona.domain.chat.ChatType;
import com.palgona.palgona.domain.member.Member;
import com.palgona.palgona.dto.chat.ChatMessageRequest;
import com.palgona.palgona.dto.chat.ChatMessageResponse;
import com.palgona.palgona.dto.chat.ChatRoomCreateRequest;
import com.palgona.palgona.repository.ChatMessageRepository;
import com.palgona.palgona.repository.ChatReadStatusRepository;
import com.palgona.palgona.repository.ChatRoomRepository;
import com.palgona.palgona.repository.member.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final SimpMessageSendingOperations messagingTemplate;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final MemberRepository memberRepository;
    private final ChatReadStatusRepository chatReadStatusRepository;

    public void sendMessage(ChatMessageRequest messageDto) {

        // 있는 멤버인지 확인
        Member sender = memberRepository.findById(messageDto.senderId()).orElseThrow(() -> new BusinessException(MemberErrorCode.MEMBER_NOT_EXIST));
        Member receiver = memberRepository.findById(messageDto.receiverId()).orElseThrow(() -> new BusinessException(MemberErrorCode.MEMBER_NOT_EXIST));

//        ChatRoom room = chatRoomRepository.findBySenderAndReceiver(sender, receiver).orElseThrow(()-> new BusinessException(ChatErrorCode.CHATROOM_NOT_FOUND));
        ChatRoom room = chatRoomRepository.findById(messageDto.roomId()).orElseThrow(() -> new BusinessException(ChatErrorCode.CHATROOM_NOT_FOUND));

        // 송신자, 수신자 모두 채팅방에 존재하는지 확인
        if (room.hasMember(sender) && room.hasMember(receiver)) {
            throw new BusinessException(ChatErrorCode.INVALID_MEMBER);
        }

        ChatMessage message = ChatMessage.builder().sender(sender).receiver(receiver).message(messageDto.message()).room(room).type(ChatType.TEXT).build();
        ChatMessage savedMessage = chatMessageRepository.save(message);
        ChatMessageResponse messageResponse = ChatMessageResponse.of(message);
        messagingTemplate.convertAndSend("/sub/topic/chat/" + savedMessage.getRoom().getId(), messageResponse);
    }

    public ChatRoom createRoom(Member sender2, ChatRoomCreateRequest request) {
        Member receiver = memberRepository.findById(request.visitorId()).orElseThrow();
        Member sender = memberRepository.findById(1L).orElseThrow();
        ChatRoom room = ChatRoom.builder().sender(sender).receiver(receiver).build();

        return chatRoomRepository.save(room);
    }

    public List<ChatRoom> getRoomList(Member member) {
        Member member1 = memberRepository.getReferenceById(1L);
        return chatRoomRepository.findBySenderOrReceiver(member1);
    }

    public boolean doesChatRoomExist(Long chatRoomId) {
        return chatRoomRepository.existsById(chatRoomId);
    }

    public void sendMessageToChatRoom(ChatMessageRequest chatMessageDto) {
        // 있는 멤버인지 확인
        Member sender = memberRepository.findById(chatMessageDto.senderId()).orElseThrow(() -> new BusinessException(MemberErrorCode.MEMBER_NOT_EXIST));
        Member receiver = memberRepository.findById(chatMessageDto.receiverId()).orElseThrow(() -> new BusinessException(MemberErrorCode.MEMBER_NOT_EXIST));

        ChatRoom chatRoom = chatRoomRepository.findBySenderAndReceiver(sender, receiver).orElseThrow(() -> new BusinessException(ChatErrorCode.CHATROOM_NOT_FOUND));
        // 송신자, 수신자 모두 채팅방에 존재하는지 확인
        if (chatRoom.hasMember(sender) && chatRoom.hasMember(receiver)) {
            throw new BusinessException(ChatErrorCode.INVALID_MEMBER);
        }
        ChatMessage message = ChatMessage.builder().message(chatMessageDto.message()).sender(sender).receiver(receiver).room(chatRoom).build();
//        ChatMessage savedMessage = chatMessageRepository.save(message);

        messagingTemplate.convertAndSend("/topic/chat/" + message.getRoom(), message);
    }

    public List<ChatMessage> getMessageByRoom(Member member, Long roomId) {
        ChatRoom room = chatRoomRepository.findById(roomId).orElseThrow(() -> new BusinessException(ChatErrorCode.CHATROOM_NOT_FOUND));
        Member member1 = memberRepository.getReferenceById(1L);
        if (!room.hasMember(member1)) {
            throw new BusinessException(ChatErrorCode.INVALID_MEMBER);
        }
        return chatMessageRepository.findAllByRoom(room);
    }

    public SliceResponse<ChatMessageResponse> getUnreadMessageByRoom(Member of, Long roomId, String cursor) {
        // TODO
        return null;
    }
}
