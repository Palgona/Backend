package com.palgona.palgona.controller.member;

import com.palgona.palgona.domain.member.Member;
import com.palgona.palgona.domain.member.Role;
import com.palgona.palgona.domain.member.Status;
import com.palgona.palgona.repository.member.MemberRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MemberInitializer {

    @Autowired
    private MemberRepository memberRepository;

    @PostConstruct
    public void init() {
        memberRepository.save(Member.of(0, Status.ACTIVE, "123", Role.GUEST));
        memberRepository.save(Member.of(0, Status.ACTIVE, "1234", Role.USER));
        memberRepository.save(Member.of(0, Status.ACTIVE, "12345", Role.ADMIN));
    }
}
