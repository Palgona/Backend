package com.palgona.palgona.repository;

import com.palgona.palgona.domain.chat.ChatRoom;
import com.palgona.palgona.domain.member.Member;
import com.palgona.palgona.dto.chat.ChatRoomCountResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

    Optional<ChatRoom> findBySenderAndReceiver(Member sender, Member receiver);

    @Query("select c from ChatRoom c where c.sender = :member or c.receiver = :member")
    List<ChatRoom> findBySenderOrReceiver(Member member);

    @Query("SELECT new com.palgona.palgona.dto.chat.ChatRoomCountResponse(cr.id, cr.sender.id, cr.receiver.id, COUNT(cm)) " +
            "FROM ChatRoom cr " +
            "LEFT JOIN cr.chatMessages cm " +
            "WHERE (cr.sender = :member OR cr.receiver = :member) " +
            "AND cm.id > (SELECT crs.messageCursor FROM ChatReadStatus crs WHERE crs.room = cr AND crs.member = :member) " +
            "GROUP BY cr.id, cr.sender.id, cr.receiver.id")
    List<ChatRoomCountResponse> countUnreadMessagesInRooms(Member member);
}
