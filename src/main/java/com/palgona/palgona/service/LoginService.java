package com.palgona.palgona.service;


import static com.palgona.palgona.common.error.code.AuthErrorCode.*;
import static com.palgona.palgona.common.error.code.MemberErrorCode.*;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.palgona.palgona.common.dto.CustomMemberDetails;
import com.palgona.palgona.common.error.exception.BusinessException;
import com.palgona.palgona.domain.member.Member;
import com.palgona.palgona.domain.member.Role;
import com.palgona.palgona.domain.member.Status;
import com.palgona.palgona.dto.KakaoUserInfoResponse;
import com.palgona.palgona.dto.LoginResponse;
import com.palgona.palgona.dto.MemberCreateRequest;
import com.palgona.palgona.repository.member.MemberRepository;
import com.palgona.palgona.service.image.S3Service;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class LoginService {

    private static final String BEARER = "Bearer ";

    private final MemberRepository memberRepository;
    private final S3Service s3Service;
    private final RestTemplate restTemplate;

    public Long signUp(CustomMemberDetails loginMember, MemberCreateRequest memberCreateRequest) {
        Member member = findMemberBySocialId(loginMember);
        validateRoleOfMember(member);
        String nickName = memberCreateRequest.nickName();
        MultipartFile image = memberCreateRequest.image();

        validateNameDuplicated(nickName);
        String imageUrl = s3Service.upload(image);
        member.updateNickName(nickName);
        member.updateProfileImage(imageUrl);
        member.signUp();
        return member.getId();
    }

    public LoginResponse login(HttpServletRequest request) {
        String accessToken = extractToken(request);
        KakaoUserInfoResponse kakaoUserInfo = getKakaoUserInfo(accessToken);

        Member findMember = memberRepository.findBySocialId(kakaoUserInfo.id())
                .orElseGet(() -> {
                    Member member = Member.of(0,
                            Status.ACTIVE,
                            kakaoUserInfo.id(),
                            Role.GUEST);
                    memberRepository.save(member);
                    return member;
                });

        return LoginResponse.from(findMember);
    }

    private KakaoUserInfoResponse getKakaoUserInfo(String accessToken) {
        HttpEntity<MultiValueMap<String, String>> kakaoUserInfoRequest = getKakaoRequest(accessToken);
        ResponseEntity<String> kakaoResponse = null;
        try {
            kakaoResponse = restTemplate.exchange(
                    "https://kapi.kakao.com/v2/user/me",
                    HttpMethod.GET,
                    kakaoUserInfoRequest,
                    String.class
            );
        } catch (Exception e) {
            throw new BusinessException(ILLEGAL_KAKAO_TOKEN);
        }

        KakaoUserInfoResponse kakaoUserInfoResponse = parseKakaoInfo(kakaoResponse);

        return kakaoUserInfoResponse;
    }

    private KakaoUserInfoResponse parseKakaoInfo(ResponseEntity<String> kakaoResponse) {
        ObjectMapper objectMapper = new ObjectMapper();
        KakaoUserInfoResponse kakaoUserInfoResponse;
        try {
            kakaoUserInfoResponse = objectMapper.readValue(kakaoResponse.getBody(), KakaoUserInfoResponse.class);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("json parsing error");
        }
        return kakaoUserInfoResponse;
    }

    private HttpEntity<MultiValueMap<String, String>> getKakaoRequest(String accessToken) {
        HttpHeaders header = new HttpHeaders();
        header.add(AUTHORIZATION, accessToken);
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
            throw new BusinessException(ILLEGAL_KAKAO_TOKEN);
        }
    }

    private void validateRoleOfMember(Member member) {
        if (!member.isGuest()) {
            throw new BusinessException(ALREADY_SIGNED_UP);
        }
    }

    private void validateNameDuplicated(String nickName) {
        if (memberRepository.existsByNickName(nickName)) {
            throw new BusinessException(DUPLICATE_NAME);
        }
    }

    private Member findMemberBySocialId(CustomMemberDetails loginMember) {
        String socialId = loginMember.getUsername();
        return memberRepository.findBySocialId(socialId).orElseThrow(
                () -> new BusinessException(MEMBER_NOT_FOUND));
    }
}
