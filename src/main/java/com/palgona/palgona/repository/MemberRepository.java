package com.palgona.palgona.repository;

import com.palgona.palgona.domain.member.Member;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Long> {

    boolean existsByNickName(String nickName);

    Optional<Member> findBySocialId(String socialId);
}