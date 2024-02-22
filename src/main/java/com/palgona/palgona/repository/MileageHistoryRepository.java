package com.palgona.palgona.repository;

import com.palgona.palgona.domain.mailage.MileageHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MileageHistoryRepository extends JpaRepository<MileageHistory, Long> {
}
