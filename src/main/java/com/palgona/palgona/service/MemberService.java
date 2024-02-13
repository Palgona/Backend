package com.palgona.palgona.service;

import static com.palgona.palgona.common.error.ErrorCode.*;

import com.palgona.palgona.common.dto.CustomMemberDetails;
import com.palgona.palgona.common.error.NotFoundMemberException;
import com.palgona.palgona.domain.member.Member;
import com.palgona.palgona.dto.MemberDetailResponse;
import com.palgona.palgona.dto.MemberResponse;
import com.palgona.palgona.dto.MemberUpdateRequest;
import com.palgona.palgona.repository.MemberRepository;
import com.palgona.palgona.service.image.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final S3Service s3Service;

    public MemberDetailResponse findMyProfile(CustomMemberDetails loginMember) {
        Member member = loginMember.getMember();

        return MemberDetailResponse.from(member);
    }

    public MemberResponse findById(Long id) {
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new NotFoundMemberException(MEMBER_NOT_FOUND));

        return MemberResponse.from(member);
    }

    public Slice<MemberResponse> findAllMember(Pageable pageable) {
        return memberRepository.findAllByOrderById(pageable)
                .map(MemberResponse::from);
    }

    @Transactional
    public void update(
            CustomMemberDetails loginMember,
            MemberUpdateRequest memberUpdateRequest) {

        String socialId = loginMember.getUsername();
        Member member = memberRepository.findBySocialId(socialId)
                .orElseThrow(() -> new IllegalArgumentException("member not found"));

        s3Service.deleteFile(member.getProfileImage());
        String imageUrl = s3Service.upload(memberUpdateRequest.image());

        //TODO : nickName, profileImage 묶어서 embedded 타입으로 빼기
        member.updateNickName(memberUpdateRequest.nickName());
        member.updateProfileImage(imageUrl);
    }
}
