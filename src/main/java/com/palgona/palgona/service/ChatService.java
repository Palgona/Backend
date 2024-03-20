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
import com.palgona.palgona.dto.chat.ChatRoomCountResponse;
import com.palgona.palgona.dto.chat.ChatRoomCreateRequest;
import com.palgona.palgona.dto.chat.ReadMessageRequest;
import com.palgona.palgona.repository.ChatMessageRepository;
import com.palgona.palgona.repository.ChatReadStatusRepository;
import com.palgona.palgona.repository.ChatRoomRepository;
import com.palgona.palgona.repository.member.MemberRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatService {
    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final MemberRepository memberRepository;
    private final ChatReadStatusRepository chatReadStatusRepository;

    @Transactional
    public ChatMessage sendMessage(ChatMessageRequest messageDto) {
        // 있는 멤버와 채팅방인지 확인
        Member sender = findMember(messageDto.senderId());
        Member receiver = findMember(messageDto.receiverId());
        ChatRoom room = findChatRoom(messageDto.roomId());

        // 송신자, 수신자 모두 채팅방에 존재하는지 확인
        if (!(room.hasMember(sender) && room.hasMember(receiver))) {
            throw new BusinessException(ChatErrorCode.INVALID_MEMBER);
        }

        ChatMessage message = ChatMessage.builder().sender(sender).receiver(receiver).message(messageDto.message()).room(room).type(ChatType.TEXT).build();
        return chatMessageRepository.save(message);
    }

    public ChatRoom createRoom(Member sender, ChatRoomCreateRequest request) {
        Member receiver = findMember(request.visitorId());
        ChatRoom room = findOrCreateChatRoom(sender, receiver);
        ChatReadStatus receiverStatus = ChatReadStatus.builder().room(room).member(receiver).build();
        ChatReadStatus senderStatus = ChatReadStatus.builder().room(room).member(sender).build();

        chatReadStatusRepository.saveAll(Arrays.asList(receiverStatus, senderStatus));
        return room;
    }

    public void readMessage(Member member, ReadMessageRequest request) {
        // 가장 최근에 읽은 데이터를 표시해야함.
        // 현재 연결되어서 바로 읽었는지 확인이 필요함.
        ChatMessage message = chatMessageRepository.findById(request.messageId()).orElseThrow(() -> new BusinessException(ChatErrorCode.MESSAGE_NOT_FOUND));
        ChatReadStatus chatReadStatus = chatReadStatusRepository.findByMemberAndRoom(member, message.getRoom());
        chatReadStatus.updateCursor(message.getId());
        chatReadStatusRepository.save(chatReadStatus);
    }

    public List<ChatRoomCountResponse> getRoomList(Member member) {
        return chatRoomRepository.countUnreadMessagesInRooms(member);
    }

    public List<ChatMessage> getMessageByRoom(Member member, Long roomId) {
        ChatRoom room = findChatRoom(roomId);
        if (!room.hasMember(member)) {
            throw new BusinessException(ChatErrorCode.INVALID_MEMBER);
        }
        return chatMessageRepository.findAllByRoom(room);
    }

    @Transactional
    public List<ChatMessage> getUnreadMessagesByRoom(Member member, Long roomId) {
        ChatRoom room = findChatRoom(roomId);

        // chatReadStatus에 표시된 가장 최근에 읽은 messageId를 cursor로 접근해서 가져옴.
        ChatReadStatus chatReadStatus = chatReadStatusRepository.findByMemberAndRoom(member, room);

        // 값을 가져온 후 가장 최근 데이터로 다시 업데이트
        List<ChatMessage> chatMessages = chatMessageRepository.findMessagesAfterCursor(roomId, chatReadStatus.getMessageCursor());
        chatReadStatus.updateCursor(chatMessages.getLast().getId());
        chatReadStatusRepository.save(chatReadStatus);

        return chatMessages;
    }

    private Member findMember(Long visitorId) {
        return memberRepository.findById(visitorId)
                .orElseThrow(() -> new BusinessException(MemberErrorCode.MEMBER_NOT_EXIST));
    }

    private ChatRoom findChatRoom(Long roomId) {
        return chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new BusinessException(ChatErrorCode.CHATROOM_NOT_FOUND));
    }

    private ChatRoom findOrCreateChatRoom(Member sender, Member receiver) {
        return chatRoomRepository.findBySenderAndReceiver(sender, receiver)
                .orElseGet(() -> chatRoomRepository.save(ChatRoom.builder().sender(sender).receiver(receiver).build()));
    }
}
