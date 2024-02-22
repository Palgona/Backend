package com.palgona.palgona.service;

import com.palgona.palgona.common.dto.CustomMemberDetails;
import com.palgona.palgona.common.error.exception.BusinessException;
import com.palgona.palgona.domain.mailage.MileageHistory;
import com.palgona.palgona.domain.mailage.MileageState;
import com.palgona.palgona.domain.member.Member;
import com.palgona.palgona.dto.MileageChargeRequest;
import com.palgona.palgona.repository.MileageHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.palgona.palgona.common.error.code.MileageErrorCode.INVALID_CHARGE_AMOUNT;

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
                .before(before)
                .amount(amount)
                .after(after)
                .member(member)
                .state(MileageState.CHARGE)
                .build();

        mileageHistoryRepository.save(mileageHistory);
    }
}
