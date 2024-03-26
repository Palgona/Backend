package com.palgona.palgona.service.chat;

import com.palgona.palgona.common.error.code.ChatErrorCode;
import com.palgona.palgona.common.error.exception.BusinessException;
import com.palgona.palgona.domain.chat.ChatMessage;
import com.palgona.palgona.domain.chat.ChatReadStatus;
import com.palgona.palgona.domain.chat.ChatRoom;
import com.palgona.palgona.domain.chat.ChatType;
import com.palgona.palgona.domain.member.Member;
import com.palgona.palgona.domain.member.Role;
import com.palgona.palgona.domain.member.Status;
import com.palgona.palgona.dto.chat.ChatMessageRequest;
import com.palgona.palgona.dto.chat.ChatRoomCreateRequest;
import com.palgona.palgona.dto.chat.ReadMessageRequest;
import com.palgona.palgona.repository.ChatMessageRepository;
import com.palgona.palgona.repository.ChatReadStatusRepository;
import com.palgona.palgona.repository.ChatRoomRepository;
import com.palgona.palgona.repository.member.MemberRepository;
import com.palgona.palgona.service.ChatService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

class ChatServiceTest {
    @Mock
    private ChatRoomRepository chatRoomRepository;

    @Mock
    private ChatMessageRepository chatMessageRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private ChatReadStatusRepository chatReadStatusRepository;

    @InjectMocks
    private ChatService chatService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("채팅방에 없는 유저로 채팅방에 메시지를 보내 실패한다.")
    public void testSendMessageWithInvalidMember() {
        // given
        int mileage = 1000;
        Status status = Status.ACTIVE;
        String socialId = "1111";
        Role role = Role.USER;

        ChatMessageRequest messageDto = new ChatMessageRequest(1L, 2L, 3L, "Hello");
        Member sender = Member.of(mileage, status, socialId, role);
        Member receiver = Member.of(mileage, status, socialId, role);
        Member another = Member.of(mileage, status, socialId, role);
        ChatRoom room = ChatRoom.builder().sender(sender).receiver(receiver).build();

        given(memberRepository.findById(1L)).willReturn(Optional.of(sender));
        given(memberRepository.findById(2L)).willReturn(Optional.of(another));
        given(chatRoomRepository.findById(3L)).willReturn(Optional.of(room));

        // when
        BusinessException exception = assertThrows(BusinessException.class, () -> chatService.sendMessage(messageDto));

        // then
        assertEquals(ChatErrorCode.INVALID_MEMBER, exception.getErrorCode());
    }


    @Test
    @DisplayName("메시지를 보내는데 성공한다.")
    public void testSendMessageSuccess() {
        // given
        int mileage = 1000;
        Status status = Status.ACTIVE;
        String socialId = "1111";
        Role role = Role.USER;

        Member sender = Member.of(mileage, status, socialId, role);
        Member receiver = Member.of(mileage, status, socialId, role);
        ChatRoom room = ChatRoom.builder().sender(sender).receiver(receiver).build();
        ChatMessageRequest messageDto = new ChatMessageRequest(1L, 2L, 3L, "Hello");
        ChatMessage message = ChatMessage.builder().sender(sender).receiver(receiver).message(messageDto.message()).room(room).type(ChatType.TEXT).build();

        given(memberRepository.findById(1L)).willReturn(Optional.of(sender));
        given(memberRepository.findById(2L)).willReturn(Optional.of(receiver));
        given(chatRoomRepository.findById(3L)).willReturn(Optional.of(room));
        given(chatMessageRepository.save(any())).willReturn(message);

        // when
        ChatMessage result = chatService.sendMessage(messageDto);

        // then
        assertEquals("Hello", result.getMessage());
    }

    @Test
    @DisplayName("채팅방을 만드는데 성공한다.")
    public void testCreateRoomSuccess() {
        // given
        int mileage = 1000;
        Status status = Status.ACTIVE;
        String socialId = "1111";
        Role role = Role.USER;

        Member sender = Member.of(mileage, status, socialId, role);
        Member receiver = Member.of(mileage, status, socialId, role);
        ChatRoom room = ChatRoom.builder().sender(sender).receiver(receiver).build();
        ChatRoomCreateRequest request = new ChatRoomCreateRequest(1L);

        given(memberRepository.findById(1L)).willReturn(Optional.of(receiver));
        given(chatRoomRepository.findBySenderAndReceiver(sender, receiver)).willReturn(Optional.empty());
        given(chatRoomRepository.save(any())).willReturn(room);

        // when
        ChatRoom createdRoom = chatService.createRoom(sender, request);

        // then
        assertNotNull(createdRoom);
        assertEquals(sender, createdRoom.getSender());
        assertEquals(receiver, createdRoom.getReceiver());
        verify(chatReadStatusRepository, times(1)).saveAll(any());
    }

    @Test
    @DisplayName("현재 채팅방의 읽은 메시지 커서를 최신화한다.")
    public void testReadMessageSuccess() {
        // given
        int mileage = 1000;
        Status status = Status.ACTIVE;
        String socialId = "1111";
        Role role = Role.USER;
        Member sender = Member.of(mileage, status, socialId, role);
        Member receiver = Member.of(mileage, status, socialId, role);
        ChatRoom room = ChatRoom.builder().sender(sender).receiver(receiver).build();
        ChatReadStatus chatReadStatus = ChatReadStatus.builder().room(room).member(sender).build();

        Long messageId = 100L;
        ReadMessageRequest request = new ReadMessageRequest(messageId);
        ChatMessage message = mock(ChatMessage.class);

        given(chatMessageRepository.findById(messageId)).willReturn(Optional.of(message));
        given(chatReadStatusRepository.findByMemberAndRoom(sender, room)).willReturn(chatReadStatus);
        given(message.getId()).willReturn(messageId);
        given(message.getRoom()).willReturn(room);

        // when
        chatService.readMessage(sender, request);

        // then
        verify(chatReadStatusRepository, times(1)).save(chatReadStatus);
        assertEquals(messageId, chatReadStatus.getMessageCursor());
    }

    @Test
    @DisplayName("방별로 메시지를 불러온다.")
    void getMessageByRoom() {
        // given
        int mileage = 1000;
        Status status = Status.ACTIVE;
        String socialId = "1111";
        Role role = Role.USER;
        Member sender = Member.of(mileage, status, socialId, role);
        Member receiver = Member.of(mileage, status, socialId, role);
        ChatRoom room = ChatRoom.builder().sender(sender).receiver(receiver).build();
        Long roomId = 1L;

        ChatMessage message = mock(ChatMessage.class);

        given(chatRoomRepository.findById(roomId)).willReturn(Optional.ofNullable(room));
        given(chatMessageRepository.findAllByRoom(room)).willReturn(Collections.singletonList(message));

        // when
        List<ChatMessage> chatMessages = chatService.getMessageByRoom(sender, roomId);

        // then
        assertEquals(chatMessages, Collections.singletonList(message));
    }

    @Test
    @DisplayName("읽은 상태가 없는 메시지가 올 경우 안읽은 메시지 조회에 실패한다.")
    public void testGetUnreadMessagesByRoom_UnValidChatReadStatus() {
        // given
        int mileage = 1000;
        Status status = Status.ACTIVE;
        String socialId = "1111";
        Role role = Role.USER;
        Member sender = Member.of(mileage, status, socialId, role);
        Member receiver = Member.of(mileage, status, socialId, role);
        ChatRoom room = ChatRoom.builder().sender(sender).receiver(receiver).build();
        Long roomId = 1L;
        ChatReadStatus chatReadStatus = null;

        when(chatRoomRepository.findById(roomId)).thenReturn(Optional.of(room));
        when(chatReadStatusRepository.findByMemberAndRoom(sender, room)).thenReturn(chatReadStatus);

        // when
        BusinessException exception = assertThrows(BusinessException.class, ()-> chatService.getUnreadMessagesByRoom(sender, roomId));

        // then
        assertEquals(ChatErrorCode.READ_STATUS_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("정상적인 메시지일 경우 안읽은 메시지 조회에 성공한다.")
    public void testGetUnreadMessagesByRoom() {
        // Arrange
        int mileage = 1000;
        Status status = Status.ACTIVE;
        String socialId = "1111";
        Role role = Role.USER;
        Member sender = Member.of(mileage, status, socialId, role);
        Member receiver = Member.of(mileage, status, socialId, role);
        ChatRoom room = ChatRoom.builder().sender(sender).receiver(receiver).build();
        Long roomId = 1L;
        Long messageCursor = 50L;
        ChatReadStatus chatReadStatus = ChatReadStatus.builder().build();
        chatReadStatus.updateCursor(messageCursor);
        List<ChatMessage> unreadMessages = new ArrayList<>();
        unreadMessages.add(mock(ChatMessage.class));
        unreadMessages.add(mock(ChatMessage.class));

        // 가짜 읽지 않은 메시지 생성
        when(chatRoomRepository.findById(roomId)).thenReturn(Optional.of(room));
        when(chatReadStatusRepository.findByMemberAndRoom(sender, room)).thenReturn(chatReadStatus);
        when(chatMessageRepository.findMessagesAfterCursor(roomId, messageCursor)).thenReturn(unreadMessages);

        // Act
        List<ChatMessage> chatMessages = chatService.getUnreadMessagesByRoom(sender, roomId);

        // Assert
        assertNotNull(chatMessages);
        assertEquals(unreadMessages, chatMessages);
        verify(chatReadStatusRepository, times(1)).save(chatReadStatus);
    }
}