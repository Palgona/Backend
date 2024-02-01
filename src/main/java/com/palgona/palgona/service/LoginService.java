package com.palgona.palgona.service;

import com.palgona.palgona.common.dto.CustomMemberDetails;
import com.palgona.palgona.domain.member.Member;
import com.palgona.palgona.dto.MemberCreateRequest;
import com.palgona.palgona.repository.MemberRepository;
import com.palgona.palgona.service.image.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class LoginService {

    private final MemberRepository memberRepository;
    private final S3Service s3Service;

    public Long signUp(CustomMemberDetails loginMember, MemberCreateRequest memberCreateRequest) {
        Member member = findMemberByEmail(loginMember);
        validateRoleOfMember(member);
        String nickName = memberCreateRequest.nickName();
        MultipartFile image = memberCreateRequest.image();

        validateNameDuplicated(nickName);
        String imageUrl = s3Service.upload(image);
        member.updateNickName(nickName);
        member.updateProfileImage(imageUrl);

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
