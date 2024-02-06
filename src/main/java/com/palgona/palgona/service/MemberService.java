package com.palgona.palgona.service;

import com.palgona.palgona.common.dto.CustomMemberDetails;
import com.palgona.palgona.domain.member.Member;
import com.palgona.palgona.dto.MemberDetailResponse;
import com.palgona.palgona.dto.MemberResponse;
import com.palgona.palgona.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;

    public MemberDetailResponse findMyProfile(CustomMemberDetails loginMember) {
        Member member = loginMember.getMember();

        return MemberDetailResponse.from(member);
    }

    public MemberDetailResponse findById(Long id) {
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("member not found"));

        return MemberDetailResponse.from(member);
    }

    public Slice<MemberResponse> findAllMember(Pageable pageable) {
        return memberRepository.findAllByOrderById(pageable)
                .map(MemberResponse::from);
    }
}
