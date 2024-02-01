package com.palgona.palgona.service;

import com.palgona.palgona.common.dto.CustomMemberDetails;
import com.palgona.palgona.domain.member.Member;
import com.palgona.palgona.dto.MemberCreateRequest;
import com.palgona.palgona.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LoginService {

    private final MemberRepository memberRepository;

    public Long signUp(CustomMemberDetails loginMember, MemberCreateRequest memberCreateRequest) {
        String nickName = memberCreateRequest.nickName();
        //MultipartFile image = memberCreateRequest.image();
        Member member = findMemberByEmail(loginMember);
        validateRoleOfMember(member);
        validateNameDuplicated(nickName);
        member.updateNickName(nickName);

        return member.getId();
    }

    private void validateRoleOfMember(Member member) {
        if (!member.isGuest()) {
            throw new IllegalStateException("already signUp");
        }
    }

    private void validateNameDuplicated(String nickName) {
        if (memberRepository.existsByNickName(nickName)) {
            throw new IllegalArgumentException("nickName is duplicated");
        }
    }

    private Member findMemberByEmail(CustomMemberDetails loginMember) {
        String email = loginMember.getUsername();
        return memberRepository.findByEmail(email).orElseThrow(
                () -> new IllegalArgumentException("user is not exist"));
    }
}
