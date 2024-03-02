package com.palgona.palgona.controller;

import com.palgona.palgona.common.dto.CustomMemberDetails;
import com.palgona.palgona.dto.MileageChargeRequest;
import com.palgona.palgona.service.MileageHistoryService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/mileages")
public class MileageHistoryController {
    private final MileageHistoryService mileageHistoryService;

    @PostMapping
    @Operation(summary = "마일리지 충전 api", description = "마일리지 충전값을 받아서 충전을 진행한다.")
    public ResponseEntity<Void> chargeMileage(@RequestBody MileageChargeRequest request,@AuthenticationPrincipal CustomMemberDetails memberDetails){

        mileageHistoryService.chargeMileage(request, memberDetails);

        return ResponseEntity.ok().build();
    }

    @GetMapping
    @Operation(summary = "마일리지 조회 api", description = "유저의 현재 마일리지 조회를 진행한다.")
    public ResponseEntity<Integer> readMileage(@AuthenticationPrincipal CustomMemberDetails memberDetails){

        int mileage = mileageHistoryService.readMileage(memberDetails);

        return ResponseEntity.ok().body(mileage);
    }
}
