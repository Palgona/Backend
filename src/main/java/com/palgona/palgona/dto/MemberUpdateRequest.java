package com.palgona.palgona.dto;

import org.springframework.web.multipart.MultipartFile;

public record MemberUpdateRequest(
        String nickName,
        MultipartFile image
) {

    public static MemberUpdateRequest of(
            MemberUpdateRequestWithoutImage request,
            MultipartFile image
    ) {

        return new MemberUpdateRequest(
                request.nickName(),
                image);
    }
}
