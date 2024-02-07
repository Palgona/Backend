package com.palgona.palgona.dto;

import com.palgona.palgona.domain.member.Member;

public record MemberDetailResponse(
        Long id,
        String nickName,
        int mileage,
        String profileImage
) {
    public static MemberDetailResponse from(Member member) {
        return new MemberDetailResponse(
                member.getId(),
                member.getNickName(),
                member.getMileage(),
                member.getProfileImage()
        );
    }
}
