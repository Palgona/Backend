package com.palgona.palgona.service;


import static org.springframework.http.HttpHeaders.AUTHORIZATION;

import com.palgona.palgona.common.dto.CustomMemberDetails;
import com.palgona.palgona.domain.member.Member;
import com.palgona.palgona.domain.member.Role;
import com.palgona.palgona.domain.member.Status;
import com.palgona.palgona.dto.KakaoUserInfoResponse;
import com.palgona.palgona.dto.LoginResponse;
import com.palgona.palgona.dto.MemberCreateRequest;
import com.palgona.palgona.repository.MemberRepository;
import com.palgona.palgona.service.image.S3Service;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Slf4j
public class LoginService {

    private static final String BEARER = "bearer ";

    private final MemberRepository memberRepository;
    private final S3Service s3Service;
    private final RestTemplate restTemplate;

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

    public LoginResponse login(HttpServletRequest request) {
        String accessToken = extractToken(request);
        KakaoUserInfoResponse kakaoUserInfo = getKakaoUserInfo(accessToken);
        String email = kakaoUserInfo.extractEmail();

        Member findMember = memberRepository.findByEmail(email)
                .orElseGet(() -> {
                    Member member = Member.of(0, Status.ACTIVE, email, Role.GUEST);
                    memberRepository.save(member);
                    return member;
                });

        return LoginResponse.from(findMember);
    }

    private KakaoUserInfoResponse getKakaoUserInfo(String accessToken) {
        HttpEntity<MultiValueMap<String, String>> kakaoUserInfoRequest = getKakaoRequest(accessToken);
        ResponseEntity<String> kakaoUserInfoResponse = restTemplate.exchange(
                "https://kapi.kakao.com/v2/user/me",
                HttpMethod.POST,
                kakaoUserInfoRequest,
                String.class
        );

        // 테스트 요구 -> kakao 유저 정보 들어오는 거 보고 구현
        log.info(kakaoUserInfoResponse.toString());

        //return kakaoUserInfoResponse;
        return null;
    }

    private HttpEntity<MultiValueMap<String, String>> getKakaoRequest(String accessToken) {
        HttpHeaders header = new HttpHeaders();
        header.add(AUTHORIZATION, BEARER + accessToken);
        header.add(
                "Content-type",
                "application/x-www-form-urlencoded;charset=utf-8"
        );

        HttpEntity<MultiValueMap<String, String>> kakaoUserInfoRequest = new HttpEntity<>(header);
        return kakaoUserInfoRequest;
    }

    private String extractToken(HttpServletRequest request) {
        String token = request.getHeader(AUTHORIZATION);
        validateToken(token);
        return token;
    }

    private void validateToken(String token) {
        if (token == null || !token.toLowerCase().startsWith(BEARER.toLowerCase())) {
            throw new IllegalArgumentException("invalid kakao Access Token");
        }
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
