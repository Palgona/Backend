package com.palgona.palgona.common.jwt.filter;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

import com.palgona.palgona.common.dto.CustomMemberDetails;
import com.palgona.palgona.common.jwt.util.JwtUtils;
import com.palgona.palgona.domain.member.Member;
import com.palgona.palgona.repository.MemberRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.core.authority.mapping.NullAuthoritiesMapper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private static final String BEARER = "bearer ";
    private static final String REFRESH_HEADER = "refresh-Authorization";
    private static final List<RequestMatcher> permittedRequestMatcher = Arrays.asList(
            new AntPathRequestMatcher("/api/v1/auth/login"),
            new AntPathRequestMatcher("/v3/**"),
            new AntPathRequestMatcher("/swagger-ui/**"));

    private final JwtUtils jwtUtils;

    private final MemberRepository memberRepository;

    private GrantedAuthoritiesMapper authoritiesMapper = new NullAuthoritiesMapper();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        log.info(request.getRequestURI());
        for (RequestMatcher requestMatcher : permittedRequestMatcher) {
            if (requestMatcher.matches(request)) {
                filterChain.doFilter(request, response);
                return;
            }
        }

        String refreshToken = extractRefreshToken(request)
                .filter(token -> !jwtUtils.isExpired(token))
                .orElse(null);
        if (refreshToken == null) {
            authenticate(request);
        } else {
            reIssueTokens(response, refreshToken);
        }
    }

    private void authenticate(HttpServletRequest request) {
        String accessToken = extractAccessToken(request).orElseThrow(
                () -> new IllegalArgumentException("access Token is not valid"));
        String email = jwtUtils.extractEmail(accessToken).orElseThrow(
                () -> new IllegalArgumentException("access Token is not valid"));
        Member member = memberRepository.findByEmail(email).orElseThrow(
                () -> new IllegalArgumentException("user is not exist"));

        saveAuthentication(member);
    }

    private void saveAuthentication(Member member) {
        CustomMemberDetails customMemberDetails = new CustomMemberDetails(member);
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                customMemberDetails, null,
                authoritiesMapper.mapAuthorities(customMemberDetails.getAuthorities()));
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private Optional<String> extractRefreshToken(HttpServletRequest request) {
        return Optional.ofNullable(request.getHeader(REFRESH_HEADER))
                .filter(token -> token.startsWith(BEARER))
                .map(token -> token.replace(BEARER, ""));
    }

    private Optional<String> extractAccessToken(HttpServletRequest request) {
        return Optional.ofNullable(request.getHeader(AUTHORIZATION))
                .filter(token -> token.startsWith(BEARER))
                .map(token -> token.replace(BEARER, ""));
    }

    private void reIssueTokens(HttpServletResponse response, String refreshToken) {
        String email = jwtUtils.extractEmail(refreshToken).orElseThrow(
                () -> new IllegalArgumentException("refresh Token is not valid"));

        response.addHeader(REFRESH_HEADER, BEARER + jwtUtils.createRefreshToken(email));
        response.addHeader(AUTHORIZATION, BEARER + jwtUtils.createAccessToken(email));
    }
}
