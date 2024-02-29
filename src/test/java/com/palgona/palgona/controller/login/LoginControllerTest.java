package com.palgona.palgona.controller.login;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.palgona.palgona.common.jwt.util.JwtService;
import com.palgona.palgona.common.jwt.util.JwtUtils;
import com.palgona.palgona.dto.AuthToken;
import com.palgona.palgona.dto.LoginResponse;
import com.palgona.palgona.dto.MemberCreateRequestWithoutImage;
import com.palgona.palgona.service.LoginService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import static org.mockito.BDDMockito.given;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.IMAGE_PNG_VALUE;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class LoginControllerTest {

    private static final String BEARER = "Bearer ";
    private static final String USER_SOCIAL_ID = "1234";
    private static final String GUEST_SOCIAL_ID = "123";
    private static final String REFRESH_HEADER = "refresh-token";

    @MockBean
    LoginService loginService;

    @MockBean
    JwtService jwtService;

    @Autowired
    JwtUtils jwtUtils;

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    void 로그인_테스트() throws Exception {

        LoginResponse loginResponse = new LoginResponse(1L, GUEST_SOCIAL_ID);

        AuthToken authToken = new AuthToken(
                jwtUtils.createAccessToken(GUEST_SOCIAL_ID),
                jwtUtils.createRefreshToken(GUEST_SOCIAL_ID)
        );

        given(loginService.login(any())).willReturn(loginResponse);
        given(jwtService.issueToken(GUEST_SOCIAL_ID)).willReturn(authToken);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/auth/login"))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    void 회원가입_테스트() throws Exception {

        MemberCreateRequestWithoutImage createRequest = new MemberCreateRequestWithoutImage("닉네임");

        given(loginService.signUp(any(), any())).willReturn(1L);

        MockMultipartFile request = new MockMultipartFile(
                "request",
                "request",
                APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsBytes(createRequest)
        );

        MockMultipartFile image = new MockMultipartFile(
                "image",
                "profile.png",
                IMAGE_PNG_VALUE,
                "imageDummy".getBytes());

        mockMvc.perform(MockMvcRequestBuilders.multipart(POST, "/api/v1/auth/signup")
                        .file(request)
                        .file(image)
                .header(AUTHORIZATION, BEARER + jwtUtils.createAccessToken(USER_SOCIAL_ID))
                .contentType(MediaType.MULTIPART_FORM_DATA_VALUE))
                .andExpect(MockMvcResultMatchers.status().isCreated());
    }

    @Test
    void 로그아웃_테스트() throws Exception {

        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/auth/logout")
                        .header(AUTHORIZATION, BEARER + jwtUtils.createAccessToken(USER_SOCIAL_ID))
                        .header(REFRESH_HEADER, BEARER + jwtUtils.createRefreshToken(USER_SOCIAL_ID)))
                .andExpect(MockMvcResultMatchers.status().isNoContent());
    }

    @Test
    void 리프레쉬_토큰_재발급_테스트() throws Exception {

        AuthToken authToken = new AuthToken(
                jwtUtils.createAccessToken(USER_SOCIAL_ID),
                jwtUtils.createRefreshToken(USER_SOCIAL_ID)
        );

        given(jwtService.reissueToken(anyString(), anyString())).willReturn(authToken);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/auth/refresh-token")
                .header(AUTHORIZATION, BEARER + jwtUtils.createRefreshToken(USER_SOCIAL_ID))
                .header(REFRESH_HEADER, BEARER + jwtUtils.createRefreshToken(USER_SOCIAL_ID)))
                .andExpect(MockMvcResultMatchers.status().isNoContent());
    }
}