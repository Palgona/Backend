package com.palgona.palgona.service;

import com.palgona.palgona.common.dto.CustomMemberDetails;
import com.palgona.palgona.common.error.exception.BusinessException;
import com.palgona.palgona.domain.mileage.MileageHistory;
import com.palgona.palgona.domain.mileage.MileageState;
import com.palgona.palgona.domain.member.Member;
import com.palgona.palgona.dto.MileageChargeRequest;
import com.palgona.palgona.repository.MileageHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.palgona.palgona.common.error.code.MileageErrorCode.INVALID_CHARGE_AMOUNT;
import static com.palgona.palgona.common.error.code.MileageErrorCode.INVALID_MILEAGE_TRANSACTION;

@Service
@RequiredArgsConstructor
public class MileageHistoryService {
    private final MileageHistoryRepository mileageHistoryRepository;

    @Transactional
    public void chargeMileage(MileageChargeRequest request, CustomMemberDetails memberDetails){
        Member member = memberDetails.getMember();

        //1.유효한 결제 내역인지 확인
        int before = member.getMileage();
        int amount = request.amount();
        int after = before + amount;

        if(amount < 0){
            throw new BusinessException(INVALID_CHARGE_AMOUNT);
        }

        //2. 멤버 마일리지 변경
        member.updateMileage(after);

        //3. 마일리지 변경이력 생성
        MileageHistory mileageHistory = MileageHistory.builder()
                .beforeMileage(before)
                .amount(amount)
                .afterMileage(after)
                .member(member)
                .state(MileageState.CHARGE)
                .build();

        mileageHistoryRepository.save(mileageHistory);
    }

    public int readMileage(CustomMemberDetails memberDetails){
        Member member = memberDetails.getMember();

        //1. 해당 멤버의 최신 마일리지 기록을 확인
        MileageHistory mileageHistory = mileageHistoryRepository.findTopByMember(member).orElse(null);

        //2. 예외처리) 마일리지 거래 내역이 없는데, 마일리지 값이 0이 아닌 경우
        if (mileageHistory == null && member.getMileage() != 0) {
            member.updateMileage(0);
            throw new BusinessException(INVALID_MILEAGE_TRANSACTION);
        }

        //3. 예외처리) 마일리지 최근 내역과 일치하지 않는 경우
        if(!mileageHistory.getAfterMileage().equals(member.getMileage())){
            member.updateMileage(mileageHistory.getAfterMileage());
            throw new BusinessException(INVALID_MILEAGE_TRANSACTION);
        }

        return member.getMileage();
    }
}
