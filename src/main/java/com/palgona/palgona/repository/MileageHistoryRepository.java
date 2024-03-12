package com.palgona.palgona.repository;

import com.palgona.palgona.domain.mileage.MileageHistory;
import com.palgona.palgona.domain.member.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface MileageHistoryRepository extends JpaRepository<MileageHistory, Long> {
    @Query("SELECT mh FROM MileageHistory mh WHERE mh.member = :member ORDER BY mh.createdAt DESC")
    Optional<MileageHistory> findTopByMember(Member member);
}
