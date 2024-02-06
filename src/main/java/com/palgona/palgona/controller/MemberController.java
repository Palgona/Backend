package com.palgona.palgona.controller;

import com.palgona.palgona.common.dto.CustomMemberDetails;
import com.palgona.palgona.dto.MemberDetailResponse;
import com.palgona.palgona.dto.MemberResponse;
import com.palgona.palgona.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/members")
public class MemberController {

    private final MemberService memberService;

    @GetMapping("/my")
    public ResponseEntity<MemberDetailResponse> findMyProfile(
            @AuthenticationPrincipal CustomMemberDetails member
    ) {
        MemberDetailResponse response = memberService.findMyProfile(member);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{memberId}")
    public ResponseEntity<MemberDetailResponse> findById(
            @PathVariable Long memberId
    ) {

        MemberDetailResponse response = memberService.findById(memberId);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<Slice<MemberResponse>> findAll(
            @PageableDefault(size = 20) Pageable pageable) {

        Slice<MemberResponse> response = memberService.findAllMember(pageable);
        return ResponseEntity.ok(response);
    }
}