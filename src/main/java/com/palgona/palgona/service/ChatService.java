package com.palgona.palgona.service;

import com.palgona.palgona.common.error.code.ChatErrorCode;
import com.palgona.palgona.common.error.code.MemberErrorCode;
import com.palgona.palgona.common.error.exception.BusinessException;
import com.palgona.palgona.domain.chat.ChatMessage;
import com.palgona.palgona.domain.chat.ChatReadStatus;
import com.palgona.palgona.domain.chat.ChatRoom;
import com.palgona.palgona.domain.chat.ChatType;
import com.palgona.palgona.domain.member.Member;
import com.palgona.palgona.dto.chat.ChatMessageRequest;
import com.palgona.palgona.dto.chat.ChatRoomCreateRequest;
import com.palgona.palgona.repository.ChatMessageRepository;
import com.palgona.palgona.repository.ChatReadStatusRepository;
import com.palgona.palgona.repository.ChatRoomRepository;
import com.palgona.palgona.repository.member.MemberRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ChatService {


    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final MemberRepository memberRepository;
    private final ChatReadStatusRepository chatReadStatusRepository;

    @Transactional
    public ChatMessage sendMessage(ChatMessageRequest messageDto) {
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

        // 가장 최근에 읽은 데이터를 표시해야함.
        // 현재 연결되어서 바로 읽었는지 확인이 필요함.

//        ChatReadStatus chatReadStatus = chatReadStatusRepository.findByMemberIdAndRoomId(sender.getId(), room.getId());
//        if (chatReadStatus == null) {
//            chatReadStatus = ChatReadStatus.builder().room(room).member(sender).build();
//        }
//        chatReadStatus.updateCursor(savedMessage.getId());
        return savedMessage;
    }

    public ChatRoom createRoom(Member sender, ChatRoomCreateRequest request) {
        Member receiver = memberRepository.findById(request.visitorId()).orElseThrow();
        Member sender2 = memberRepository.findById(1L).orElseThrow();
        Optional<ChatRoom> room = chatRoomRepository.findBySenderAndReceiver(receiver, sender2);
        if (room.isEmpty()){
            ChatRoom newRoom = ChatRoom.builder().sender(sender2).receiver(receiver).build();
            return chatRoomRepository.save(newRoom);
        } else {
            return room.get();
        }


    }

    public List<ChatRoom> getRoomList(Member member) {
        Member member1 = memberRepository.getReferenceById(1L);
        return chatRoomRepository.findBySenderOrReceiver(member1);
    }

    public List<ChatMessage> getMessageByRoom(Member member, Long roomId) {
        ChatRoom room = chatRoomRepository.findById(roomId).orElseThrow(() -> new BusinessException(ChatErrorCode.CHATROOM_NOT_FOUND));
        Member member1 = memberRepository.getReferenceById(1L);
        if (!room.hasMember(member1)) {
            throw new BusinessException(ChatErrorCode.INVALID_MEMBER);
        }
        return chatMessageRepository.findAllByRoom(room);
    }

    @Transactional
    public List<ChatMessage> getUnreadMessagesByRoom(Member member, Long roomId, Long cursor) {
        ChatRoom room = chatRoomRepository.findById(roomId).orElseThrow(() -> new BusinessException(ChatErrorCode.CHATROOM_NOT_FOUND));

        // chatReadStatus에 표시된 가장 최근에 읽은 messageId를 cursor로 접근해서 가져옴.
        ChatReadStatus chatReadStatus = chatReadStatusRepository.findByMemberIdAndRoomId(member.getId(), room.getId());
        if (chatReadStatus == null) {
            chatReadStatus = ChatReadStatus.builder().room(room).member(member).build();
        }

        // 값을 가져온 후 가장 최근 데이터로 다시 업데이트
        List<ChatMessage> chatMessages = chatMessageRepository.findMessagesAfterCursor(roomId, cursor);
        chatReadStatus.updateCursor(chatMessages.getLast().getId());

        return chatMessages;
    }
}
