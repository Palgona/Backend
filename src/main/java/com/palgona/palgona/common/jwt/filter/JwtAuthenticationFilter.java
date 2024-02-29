package com.palgona.palgona.common.jwt.filter;

import com.palgona.palgona.common.dto.CustomMemberDetails;
import com.palgona.palgona.common.error.code.AuthErrorCode;
import com.palgona.palgona.common.jwt.util.JwtUtils;
import com.palgona.palgona.common.jwt.util.TokenExtractor;
import com.palgona.palgona.common.redis.RedisUtils;
import com.palgona.palgona.domain.member.Member;
import com.palgona.palgona.repository.member.MemberRepository;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.SecurityException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.core.authority.mapping.NullAuthoritiesMapper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String EXCEPTION = "exception";

    private final JwtUtils jwtUtils;
    private final MemberRepository memberRepository;
    private final TokenExtractor tokenExtractor;
    private final RedisUtils redisUtils;

    private GrantedAuthoritiesMapper authoritiesMapper = new NullAuthoritiesMapper();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        log.info(request.getRequestURI());
        authenticate(request);
        filterChain.doFilter(request, response);
    }

    private void authenticate(HttpServletRequest request) {
        try {
            String accessToken = tokenExtractor.extractAccessToken(request);
            if (!jwtUtils.isExpired(accessToken) || !redisUtils.isBlacklist(accessToken)) {
                String socialId = jwtUtils.extractSocialId(accessToken)
                        .orElseThrow(() -> new IllegalArgumentException());
                Member member = memberRepository.findBySocialId(socialId)
                        .orElseThrow(() -> new IllegalArgumentException());

                saveAuthentication(member);
            }
        } catch (SecurityException | MalformedJwtException e) {
            request.setAttribute(EXCEPTION, AuthErrorCode.INVALID_SIGNATURE);
        } catch (ExpiredJwtException e) {
            request.setAttribute(EXCEPTION, AuthErrorCode.EXPIRED_TOKEN);
        } catch (UnsupportedJwtException e) {
            request.setAttribute(EXCEPTION, AuthErrorCode.NOT_SUPPORTED_TOKEN);
        } catch (IllegalArgumentException e) {
            request.setAttribute(EXCEPTION, AuthErrorCode.ILLEGAL_TOKEN);
        }
    }

    private void saveAuthentication(Member member) {
        CustomMemberDetails customMemberDetails = new CustomMemberDetails(member);
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                customMemberDetails, null,
                authoritiesMapper.mapAuthorities(customMemberDetails.getAuthorities()));
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
