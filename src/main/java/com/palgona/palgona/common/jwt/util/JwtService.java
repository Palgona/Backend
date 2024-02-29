package com.palgona.palgona.common.jwt.util;

import static com.palgona.palgona.common.error.code.AuthErrorCode.ILLEGAL_TOKEN;

import com.palgona.palgona.common.error.exception.BusinessException;
import com.palgona.palgona.domain.RefreshToken;
import com.palgona.palgona.dto.AuthToken;
import com.palgona.palgona.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class JwtService {

    private final JwtUtils jwtUtils;
    private final RefreshTokenRepository refreshTokenRepository;

    public AuthToken issueToken(String socialId) {
        String accessToken = jwtUtils.createAccessToken(socialId);
        String refreshToken = jwtUtils.createRefreshToken(socialId);

        if (refreshTokenRepository.existsBySocialId(socialId)) {
            refreshTokenRepository.deleteBySocialId(socialId);
        }

        refreshTokenRepository.save(new RefreshToken(refreshToken, socialId));

        return new AuthToken(accessToken, refreshToken);
    }

    public AuthToken reissueToken(String accessToken, String refreshToken) {
        validateTokenReissue(accessToken, refreshToken);
        String socialId = findSocialIdByRefreshToken(refreshToken);

        return issueToken(socialId);
    }

    public void removeRefreshToken(String refreshToken) {
        validateTokenRemove(refreshToken);
        refreshTokenRepository.deleteById(refreshToken);
    }

    private String findSocialIdByRefreshToken(String token) {
        RefreshToken refreshToken = refreshTokenRepository.findById(token)
                .orElseThrow(() -> new BusinessException(ILLEGAL_TOKEN));
        return refreshToken.getSocialId();
    }

    private void validateTokenReissue(String accessToken, String refreshToken) {
        if (!jwtUtils.isExpired(refreshToken) && jwtUtils.isExpired(accessToken)) {
            return;
        }

        throw new BusinessException(ILLEGAL_TOKEN);
    }

    private void validateTokenRemove(String refreshToken) {
        if (jwtUtils.isExpired(refreshToken)) {
            throw new BusinessException(ILLEGAL_TOKEN);
        }
    }
}
