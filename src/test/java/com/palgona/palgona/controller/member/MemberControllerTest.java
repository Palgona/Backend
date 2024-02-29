package com.palgona.palgona.controller.member;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpMethod.PUT;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.IMAGE_PNG_VALUE;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.palgona.palgona.common.dto.response.SliceResponse;
import com.palgona.palgona.common.jwt.util.JwtUtils;
import com.palgona.palgona.domain.member.Member;
import com.palgona.palgona.domain.member.Role;
import com.palgona.palgona.domain.member.Status;
import com.palgona.palgona.dto.MemberDetailResponse;
import com.palgona.palgona.dto.MemberResponse;
import com.palgona.palgona.dto.MemberUpdateRequestWithoutImage;
import com.palgona.palgona.service.MemberService;
import java.util.List;
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

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class MemberControllerTest {

    private static final String BEARER = "Bearer ";
    private static final String ADMIN_SOCIAL_ID = "12345";
    private static final String USER_SOCIAL_ID = "1234";
    private static final String GUEST_SOCIAL_ID = "123";


    @MockBean
    MemberService memberService;

    @Autowired
    MockMvc mockMvc;

    @Autowired
    JwtUtils jwtUtils;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    void 나의_프로필_정보를_확인한다() throws Exception {
        MemberDetailResponse response = new MemberDetailResponse(
                2L,
                "palgona",
                100,
                "image.png"
        );

        given(memberService.findMyProfile(any())).willReturn(response);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/members/my")
                .header(AUTHORIZATION, BEARER + jwtUtils.createAccessToken(USER_SOCIAL_ID)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(jsonPath("$.id").value(2L));
    }

    @Test
    void 회원가입전에_프로필_조회하면_예외가_발생한다() throws Exception {
        MemberDetailResponse response = new MemberDetailResponse(
                2L,
                "palgona",
                100,
                "image.png"
        );

        given(memberService.findMyProfile(any())).willReturn(response);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/members/my")
                        .header(AUTHORIZATION, BEARER + jwtUtils.createAccessToken(GUEST_SOCIAL_ID)))
                .andExpect(MockMvcResultMatchers.status().isForbidden())
                .andExpect(jsonPath("$.code").value("A_006"));
    }

    @Test
    void 맴버_프로필_정보를_확인한다() throws Exception {
        MemberResponse response = new MemberResponse(
                2L,
                "palgona",
                "image.png"
        );

        given(memberService.findById(any())).willReturn(response);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/members/2")
                .header(AUTHORIZATION, BEARER + jwtUtils.createAccessToken(USER_SOCIAL_ID)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(jsonPath("$.id").value(2));
    }

    @Test
    void 회원가입전에_맴버_프로필_정보를_조회하면_예외가_발생한다() throws Exception {
        MemberResponse response = new MemberResponse(
                2L,
                "palgona",
                "image.png"
        );

        Member member = Member.of(100, Status.ACTIVE, "111", Role.GUEST);

        given(memberService.findById(any())).willReturn(response);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/members/2")
                        .header(AUTHORIZATION, BEARER + jwtUtils.createAccessToken(GUEST_SOCIAL_ID)))
                .andExpect(MockMvcResultMatchers.status().isForbidden())
                .andExpect(jsonPath("$.code").value("A_006"))
                .andDo(print());
    }

    @Test
    void 모든_유저를_조회한다() throws Exception {
        List<MemberResponse> memberResponses = List.of(
                new MemberResponse(
                        1L,
                        "palgona1",
                        "image1.png"
                ),
                new MemberResponse(
                        2L,
                        "palgona2",
                        "image2.png"
                )
        );

        SliceResponse<MemberResponse> response = new SliceResponse<>(memberResponses, true, "cursor");

        given(memberService.findAllMember(any())).willReturn(response);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/members")
                .header(AUTHORIZATION, BEARER + jwtUtils.createAccessToken(ADMIN_SOCIAL_ID)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(jsonPath("$.values[0].id").value(1))
                .andExpect(jsonPath("$.values[1].id").value(2));
    }

    @Test
    void 관리자가_아니면_모든_유저를_조회할_수_없다() throws Exception {
        List<MemberResponse> memberResponses = List.of(
                new MemberResponse(
                        1L,
                        "palgona1",
                        "image1.png"
                ),
                new MemberResponse(
                        2L,
                        "palgona2",
                        "image2.png"
                )
        );

        SliceResponse<MemberResponse> response = new SliceResponse<>(memberResponses, true, "cursor");

        given(memberService.findAllMember(any())).willReturn(response);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/members")
                        .header(AUTHORIZATION, BEARER + jwtUtils.createAccessToken(USER_SOCIAL_ID)))
                .andExpect(MockMvcResultMatchers.status().isForbidden())
                .andExpect(jsonPath("$.code").value("A_006"));
    }

    @Test
    void 유저_정보를_갱신한다() throws Exception {

        MemberUpdateRequestWithoutImage memberUpdateRequestWithoutImage
                = new MemberUpdateRequestWithoutImage("request");

        MockMultipartFile request = new MockMultipartFile(
                "request",
                "request",
                APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsBytes(memberUpdateRequestWithoutImage)
        );

        MockMultipartFile image = new MockMultipartFile(
                "image",
                "profile.png",
                IMAGE_PNG_VALUE,
                "imageDummy".getBytes());

        mockMvc.perform(MockMvcRequestBuilders.multipart(PUT, "/api/v1/members")
                        .file(request)
                        .file(image)
                .header(AUTHORIZATION, BEARER + jwtUtils.createAccessToken(USER_SOCIAL_ID))
                .contentType(MediaType.MULTIPART_FORM_DATA_VALUE))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    void 회원가입전에_유저_정보를_갱신하면_예외가_발생한다() throws Exception {

        MemberUpdateRequestWithoutImage memberUpdateRequestWithoutImage
                = new MemberUpdateRequestWithoutImage("request");

        MockMultipartFile request = new MockMultipartFile(
                "request",
                "request",
                APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsBytes(memberUpdateRequestWithoutImage)
        );

        MockMultipartFile image = new MockMultipartFile(
                "image",
                "profile.png",
                IMAGE_PNG_VALUE,
                "imageDummy".getBytes());

        mockMvc.perform(MockMvcRequestBuilders.multipart(PUT, "/api/v1/members")
                        .file(request)
                        .file(image)
                        .header(AUTHORIZATION, BEARER + jwtUtils.createAccessToken(GUEST_SOCIAL_ID))
                        .contentType(MediaType.MULTIPART_FORM_DATA_VALUE))
                .andExpect(MockMvcResultMatchers.status().isForbidden());
    }
}