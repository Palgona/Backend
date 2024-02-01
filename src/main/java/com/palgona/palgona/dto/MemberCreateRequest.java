package com.palgona.palgona.dto;

import org.springframework.web.multipart.MultipartFile;

public record MemberCreateRequest(
        String nickName,
        MultipartFile image) {
    public static MemberCreateRequest of(
            MemberCreateRequestWithoutImage request,
            MultipartFile image) {
        return new MemberCreateRequest(request.nickName(), image);
    }
}
