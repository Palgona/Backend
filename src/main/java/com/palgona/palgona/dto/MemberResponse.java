package com.palgona.palgona.dto;

import com.palgona.palgona.domain.member.Member;

public record MemberResponse(
        Long id,
        String nickName,
        String profileImage
) {

    public static MemberResponse from(Member member) {
        return new MemberResponse(
                member.getId(),
                member.getNickName(),
                member.getProfileImage()
        );
    }
}
