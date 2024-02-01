package com.palgona.palgona.controller;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

import com.palgona.palgona.common.dto.CustomMemberDetails;
import com.palgona.palgona.common.jwt.util.JwtUtils;
import com.palgona.palgona.dto.LoginResponse;
import com.palgona.palgona.dto.MemberCreateRequest;
import com.palgona.palgona.dto.MemberCreateRequestWithoutImage;
import com.palgona.palgona.service.LoginService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class LoginController {
    private static final String BEARER = "bearer ";
    private static final String REFRESH_HEADER = "refresh-Authorization";

    private final LoginService loginService;
    private final JwtUtils jwtUtils;

    @PostMapping(
            value = "/signup",
            consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<Void> create(
            @AuthenticationPrincipal CustomMemberDetails member,
            @RequestPart MemberCreateRequestWithoutImage request,
            @RequestPart(required = false) MultipartFile image
    ) {

        MemberCreateRequest memberCreateRequest = MemberCreateRequest.of(request, image);
        Long memberId = loginService.signUp(member, memberCreateRequest);

        return ResponseEntity.created(URI.create("/members/" + memberId))
                .build();
    }

    @GetMapping("/login")
    public LoginResponse login(
            HttpServletRequest request,
            HttpServletResponse response
    ) {

        LoginResponse loginResponse = loginService.login(request);
        String email = loginResponse.email();

        String accessToken = jwtUtils.createAccessToken(email);
        String refreshToken = jwtUtils.createRefreshToken(email);
        response.setHeader(AUTHORIZATION, BEARER + accessToken);
        response.setHeader(REFRESH_HEADER, BEARER + refreshToken);

        return loginResponse;
    }
}
