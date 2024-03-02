package com.palgona.palgona.service.member;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.http.MediaType.IMAGE_PNG_VALUE;

import com.palgona.palgona.common.dto.CustomMemberDetails;
import com.palgona.palgona.domain.member.Member;
import com.palgona.palgona.domain.member.Role;
import com.palgona.palgona.domain.member.Status;
import com.palgona.palgona.dto.MemberUpdateRequest;
import com.palgona.palgona.repository.member.MemberRepository;
import com.palgona.palgona.service.MemberService;
import com.palgona.palgona.service.image.S3Service;
import java.util.Optional;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

@ExtendWith(MockitoExtension.class)
public class MemberServiceTest {

    @Mock
    MemberRepository memberRepository;

    @Mock
    S3Service s3Service;

    @InjectMocks
    MemberService memberService;

    @Test
    void 유저_정보_갱신_테스트() {
        MockMultipartFile image = new MockMultipartFile(
                "image",
                "profile.png",
                IMAGE_PNG_VALUE,
                "imageDummy".getBytes());

        MemberUpdateRequest request = new MemberUpdateRequest("palgona", image);

        Member loginMember = Member.of(100, Status.ACTIVE, "111", Role.USER);
        CustomMemberDetails customMemberDetails = new CustomMemberDetails(loginMember);
        Member target = Member.of(100, Status.ACTIVE, "111", Role.USER);

        given(memberRepository.findBySocialId(any())).willReturn(Optional.of(target));
        doNothing().when(s3Service).deleteFile(any());
        given(s3Service.upload(any())).willReturn("123.png");

        memberService.update(customMemberDetails, request);
        assertThat(target.getNickName()).isEqualTo("palgona");
        assertThat(target.getProfileImage()).isEqualTo("123.png");
    }
}
