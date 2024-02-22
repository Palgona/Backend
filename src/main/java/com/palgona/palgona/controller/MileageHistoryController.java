package com.palgona.palgona.controller;

import com.palgona.palgona.common.dto.CustomMemberDetails;
import com.palgona.palgona.dto.MileageChargeRequest;
import com.palgona.palgona.service.MileageHistoryService;
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
    public ResponseEntity<Void> chargeMileage(@RequestBody MileageChargeRequest request,@AuthenticationPrincipal CustomMemberDetails memberDetails){

        mileageHistoryService.chargeMileage(request, memberDetails);

        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<Integer> readMileage(@AuthenticationPrincipal CustomMemberDetails memberDetails){

        int mileage = mileageHistoryService.readMileage(memberDetails);

        return ResponseEntity.ok().body(mileage);
    }
}
