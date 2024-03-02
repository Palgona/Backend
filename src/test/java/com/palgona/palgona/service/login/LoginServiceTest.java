package com.palgona.palgona.service.login;

import static com.palgona.palgona.common.error.code.MemberErrorCode.ALREADY_SIGNED_UP;
import static com.palgona.palgona.common.error.code.MemberErrorCode.DUPLICATE_NAME;
import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.http.MediaType.IMAGE_PNG_VALUE;
import static org.mockito.BDDMockito.given;

import com.palgona.palgona.common.dto.CustomMemberDetails;
import com.palgona.palgona.common.error.exception.BusinessException;
import com.palgona.palgona.domain.member.Member;
import com.palgona.palgona.domain.member.Role;
import com.palgona.palgona.domain.member.Status;
import com.palgona.palgona.dto.MemberCreateRequest;
import com.palgona.palgona.repository.member.MemberRepository;
import com.palgona.palgona.service.LoginService;
import com.palgona.palgona.service.image.S3Service;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

@ExtendWith(MockitoExtension.class)
public class LoginServiceTest {

    @Mock
    MemberRepository memberRepository;
    @Mock
    S3Service s3Service;

    @InjectMocks
    LoginService loginService;


    @Test
    void 회원가입_성공_테스트() {
        Member member = Member.of(100, Status.ACTIVE, "111", Role.GUEST);
        CustomMemberDetails customMemberDetails = new CustomMemberDetails(member);
        MockMultipartFile image = new MockMultipartFile(
                "image",
                "profile.png",
                IMAGE_PNG_VALUE,
                "imageDummy".getBytes());

        MemberCreateRequest request = new MemberCreateRequest("name", image);

        given(memberRepository.findBySocialId("111")).willReturn(Optional.of(member));
        given(memberRepository.existsByNickName("name")).willReturn(false);
        given(s3Service.upload(image)).willReturn("image.png");

        loginService.signUp(customMemberDetails, request);

        assertThat(member.getNickName()).isEqualTo("name");
        assertThat(member.getProfileImage()).isEqualTo("image.png");
    }

    @Test
    void 동일한_닉네임이_존재하면_에러가_발생한다() {
        Member member = Member.of(100, Status.ACTIVE, "111", Role.GUEST);
        CustomMemberDetails customMemberDetails = new CustomMemberDetails(member);
        MockMultipartFile image = new MockMultipartFile(
                "image",
                "profile.png",
                IMAGE_PNG_VALUE,
                "imageDummy".getBytes());

        MemberCreateRequest request = new MemberCreateRequest("name", image);

        given(memberRepository.findBySocialId("111")).willReturn(Optional.of(member));
        given(memberRepository.existsByNickName("name")).willReturn(true);

        assertThatThrownBy(() -> loginService.signUp(customMemberDetails, request))
                .isInstanceOf(BusinessException.class)
                .hasMessage(DUPLICATE_NAME.getMessage());
    }


    @Test
    void 유저_ROLE이_USER이면_회원가입에_실패한다() {
        Member member = Member.of(100, Status.ACTIVE, "111", Role.USER);
        CustomMemberDetails customMemberDetails = new CustomMemberDetails(member);
        MockMultipartFile image = new MockMultipartFile(
                "image",
                "profile.png",
                IMAGE_PNG_VALUE,
                "imageDummy".getBytes());

        MemberCreateRequest request = new MemberCreateRequest("name", image);

        given(memberRepository.findBySocialId("111")).willReturn(Optional.of(member));

        assertThatThrownBy(() -> loginService.signUp(customMemberDetails, request))
                .isInstanceOf(BusinessException.class)
                .hasMessage(ALREADY_SIGNED_UP.getMessage());
    }

    @Test
    void 유저_ROLE이_ADMIN이면_회원가입에_실패한다() {
        Member member = Member.of(100, Status.ACTIVE, "111", Role.ADMIN);
        CustomMemberDetails customMemberDetails = new CustomMemberDetails(member);
        MockMultipartFile image = new MockMultipartFile(
                "image",
                "profile.png",
                IMAGE_PNG_VALUE,
                "imageDummy".getBytes());

        MemberCreateRequest request = new MemberCreateRequest("name", image);

        given(memberRepository.findBySocialId("111")).willReturn(Optional.of(member));

        assertThatThrownBy(() -> loginService.signUp(customMemberDetails, request))
                .isInstanceOf(BusinessException.class)
                .hasMessage(ALREADY_SIGNED_UP.getMessage());
    }
}
