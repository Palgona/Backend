package com.palgona.palgona.repository;

import com.palgona.palgona.domain.RefreshToken;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, String> {

    boolean existsBySocialId(String socialId);

    void deleteBySocialId(String socialId);
}
