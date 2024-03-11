package com.palgona.palgona.repository;

import com.palgona.palgona.domain.chat.ChatRoom;
import com.palgona.palgona.domain.member.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

    Optional<ChatRoom> findBySenderAndReceiver(Member sender, Member receiver);

    @Query("select c from ChatRoom c where c.sender = :member or c.receiver = :member")
    List<ChatRoom> findBySenderOrReceiver(Member member);
}
