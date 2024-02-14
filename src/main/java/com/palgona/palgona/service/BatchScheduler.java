package com.palgona.palgona.service;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BatchScheduler {

    private final BiddingService biddingService;

    @Scheduled(cron = "0/30 * * * * ?") // 5분 주기로 실행
    public void checkBiddingExpiration() {
        biddingService.checkBiddingExpiration();
    }
}
