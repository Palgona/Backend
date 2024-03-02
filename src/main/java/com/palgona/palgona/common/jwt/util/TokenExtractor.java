package com.palgona.palgona.common.jwt.util;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class TokenExtractor {

    private static final String REFRESH_HEADER = "Refresh-token";
    private static final String BEARER = "Bearer ";

    public String extractRefreshToken(HttpServletRequest request) {
        String refreshToken = request.getHeader(REFRESH_HEADER);
        if (StringUtils.hasText(refreshToken) && isBearerToken(refreshToken)) {
            return refreshToken.substring(7);
        }

        return null;
    }

    public String extractAccessToken(HttpServletRequest request) {
        String accessToken = request.getHeader(AUTHORIZATION);
        if (StringUtils.hasText(accessToken) && isBearerToken(accessToken)) {
            return accessToken.substring(7);
        }

        return null;
    }

    private boolean isBearerToken(String refreshToken) {
        return refreshToken.toLowerCase().startsWith(BEARER.toLowerCase());
    }
}
