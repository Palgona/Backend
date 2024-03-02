package com.palgona.palgona.controller;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

import com.palgona.palgona.common.dto.CustomMemberDetails;
import com.palgona.palgona.common.jwt.util.JwtService;
import com.palgona.palgona.common.jwt.util.JwtUtils;
import com.palgona.palgona.common.jwt.util.TokenExtractor;
import com.palgona.palgona.dto.AuthToken;
import com.palgona.palgona.dto.LoginResponse;
import com.palgona.palgona.dto.MemberCreateRequest;
import com.palgona.palgona.dto.MemberCreateRequestWithoutImage;
import com.palgona.palgona.service.LoginService;
import io.swagger.v3.oas.annotations.Operation;
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
    private static final String BEARER = "Bearer ";
    private static final String REFRESH_HEADER = "refresh-token";

    private final LoginService loginService;
    private final JwtService jwtService;
    private final TokenExtractor tokenExtractor;

    @PostMapping(
            value = "/signup",
            consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @Operation(summary = "회원 가입 api", description = "닉네임, 프로필을 받아서 회원가입을 진행한다.")
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
    @Operation(summary = "로그인 api", description = "kakao AccessToken을 받아서 로그인을 진행한다.")
    public LoginResponse login(
            HttpServletRequest request,
            HttpServletResponse response
    ) {

        LoginResponse loginResponse = loginService.login(request);
        String socialId = loginResponse.socialId();
        AuthToken authToken = jwtService.issueToken(socialId);
        response.setHeader(AUTHORIZATION, BEARER + authToken.accessToken());
        response.setHeader(REFRESH_HEADER, BEARER + authToken.refreshToken());

        return loginResponse;
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request) {
        String refreshToken = tokenExtractor.extractRefreshToken(request);
        String accessToken = tokenExtractor.extractAccessToken(request);
        jwtService.removeRefreshToken(refreshToken);
        loginService.logout(accessToken);

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<Void> reissueToken(HttpServletRequest request, HttpServletResponse response) {
        String accessToken = tokenExtractor.extractAccessToken(request);
        String refreshToken = tokenExtractor.extractRefreshToken(request);
        AuthToken authToken = jwtService.reissueToken(accessToken, refreshToken);
        response.setHeader(AUTHORIZATION, BEARER + authToken.accessToken());
        response.setHeader(REFRESH_HEADER, BEARER + authToken.refreshToken());

        return ResponseEntity.noContent().build();
    }
}
