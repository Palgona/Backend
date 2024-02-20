package com.palgona.palgona.repository.member;

import com.palgona.palgona.domain.member.Member;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Long>, MemberRepositoryCustom {

    boolean existsByNickName(String nickName);

    Optional<Member> findBySocialId(String socialId);
}
