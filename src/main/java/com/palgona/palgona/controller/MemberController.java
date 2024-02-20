package com.palgona.palgona.controller;

import com.palgona.palgona.common.dto.CustomMemberDetails;
import com.palgona.palgona.common.dto.response.SliceResponse;
import com.palgona.palgona.dto.MemberDetailResponse;
import com.palgona.palgona.dto.MemberResponse;
import com.palgona.palgona.dto.MemberUpdateRequest;
import com.palgona.palgona.dto.MemberUpdateRequestWithoutImage;
import com.palgona.palgona.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

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
    public ResponseEntity<MemberResponse> findById(
            @PathVariable Long memberId
    ) {

        MemberResponse response = memberService.findById(memberId);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<SliceResponse<MemberResponse>> findAll(
            @AuthenticationPrincipal CustomMemberDetails member,
            @RequestParam(required = false) String cursor) {

        SliceResponse<MemberResponse> response = memberService.findAllMember(member, cursor);
        return ResponseEntity.ok(response);
    }

    @PutMapping(consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<Void> update(
            @AuthenticationPrincipal CustomMemberDetails member,
            @RequestPart MemberUpdateRequestWithoutImage request,
            @RequestPart(required = false) MultipartFile image
    ) {

        MemberUpdateRequest memberUpdateRequest = MemberUpdateRequest.of(request, image);
        memberService.update(member, memberUpdateRequest);

        return ResponseEntity.ok()
                .header("Location", "/api/v1/members/"
                + member.getMember().getId())
                .build();
    }
}
