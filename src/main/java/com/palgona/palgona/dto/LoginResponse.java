package com.palgona.palgona.dto;

import com.palgona.palgona.domain.member.Member;

public record LoginResponse(
        Long id,
        String socialId
) {

    public static LoginResponse from(Member member) {
        return new LoginResponse(
                member.getId(),
                member.getSocialId()
        );
    }
}
