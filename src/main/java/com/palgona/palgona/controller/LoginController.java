package com.palgona.palgona.controller;

import com.palgona.palgona.common.dto.CustomMemberDetails;
import com.palgona.palgona.dto.MemberCreateRequest;
import com.palgona.palgona.dto.MemberCreateRequestWithoutImage;
import com.palgona.palgona.service.LoginService;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class LoginController {
    private final LoginService loginService;

    @PostMapping(
            value = "/signup",
            consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<Void> create(
            @AuthenticationPrincipal CustomMemberDetails member,
            @RequestPart MemberCreateRequestWithoutImage request,
            @RequestPart(required = false) MultipartFile image) {

        MemberCreateRequest memberCreateRequest = MemberCreateRequest.of(request, image);
        Long memberId = loginService.signUp(member, memberCreateRequest);

        return ResponseEntity.created(URI.create("/members/" + memberId))
                .build();
    }
}
