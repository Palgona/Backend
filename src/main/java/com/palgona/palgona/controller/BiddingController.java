package com.palgona.palgona.controller;

import com.palgona.palgona.common.dto.CustomMemberDetails;
import com.palgona.palgona.domain.bidding.Bidding;
import com.palgona.palgona.dto.BiddingAttemptRequest;
import com.palgona.palgona.dto.BiddingPageResponse;
import com.palgona.palgona.service.BiddingService;
import io.swagger.v3.oas.annotations.Operation;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("¬")
public class BiddingController {
    private final BiddingService biddingService;

    @PostMapping(value = "/attempt", consumes = {MediaType.APPLICATION_JSON_VALUE})
    @Operation(summary = "입찰 진행 api", description = "물건 id와 가격을 받아서 입찰을 진행한다.")
    public ResponseEntity<Void> attemptBidding(@AuthenticationPrincipal CustomMemberDetails member,
                                               @RequestBody BiddingAttemptRequest request) {
        biddingService.attemptBidding(member.getMember(), request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{productId}")
    @Operation(summary = "입찰 목록 api", description = "물건 id를 받아서 입찰 목록을 보여준다.")
    public ResponseEntity<BiddingPageResponse> findAllByProductId(@RequestParam long productId,
                                                            @PageableDefault(size = 20) Pageable pageable) {
        Page<Bidding> biddingPage =  biddingService.findAllByProductId(productId, pageable);
        List<Bidding> biddingList = biddingPage.toList();
        BiddingPageResponse response = BiddingPageResponse.of(biddingPage, biddingList);

        return ResponseEntity.ok(response);
    }
}
