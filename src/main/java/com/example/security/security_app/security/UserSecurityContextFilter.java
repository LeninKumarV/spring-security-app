package com.example.security.security_app.security;

import com.example.security.security_app.models.UserContext;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.util.*;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserSecurityContextFilter extends OncePerRequestFilter {

    @Value("${app.jwt.secret}")
    private String secretKey;

    private final CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain)
            throws ServletException, IOException {
        try {
            String token = extractToken(request);

            // Only process if token exists and is valid
            if (token != null && isTokenValid(token)) {

                Claims claims = parseClaims(token); // parse once, reuse

                // Build UserContext
                UserContext ctx = UserContext.builder()
                        .userName(extractUsername(claims))
                        .token(token)
                        .requestId(Optional
                                .ofNullable(request.getHeader("X-Request-ID"))
                                .orElse(UUID.randomUUID().toString()))
                        .rolesList(extractRoles(claims))
                        .build();

                // Store in ThreadLocal
                UserContext.set(ctx);

                //  MDC for log tracing
                MDC.put("userName", ctx.getUserName());
                MDC.put("requestId", ctx.getRequestId());

                // Set Spring Security context
                setSpringSecurityContext(ctx, request);

                log.info("User authenticated successfully");
            }

            chain.doFilter(request, response);

        } catch (JwtException e) {
            // Invalid token — reject request cleanly
            log.warn("Invalid JWT token: {}", e.getMessage());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Invalid or expired token");

        } finally {
            UserContext.clear();
            MDC.clear();
            SecurityContextHolder.clearContext();
        }
    }

    // Set Spring Security Authentication
    private void setSpringSecurityContext(UserContext ctx,
                                          HttpServletRequest request) {
        UserDetails userDetails = userDetailsService
                .loadUserByUsername(ctx.getUserName());

        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()); //roles from UserDetails

        authToken.setDetails(
                new WebAuthenticationDetailsSource().buildDetails(request));

        SecurityContextHolder.getContext().setAuthentication(authToken);
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    private boolean isTokenValid(String token) {
        try {
            Claims claims = parseClaims(token);
            return !claims.getExpiration()
                    .before(new java.util.Date()); // not expired
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    private String extractToken(HttpServletRequest request) {
        String bearer = request.getHeader("Authorization");
        if (StringUtils.hasText(bearer) && bearer.startsWith("Bearer ")) {
            return bearer.substring(7); // strip "Bearer " prefix
        }
        return null;
    }

    private String extractUsername(Claims claims) {
        return claims.getSubject(); // "sub" claim
    }

    @SuppressWarnings("unchecked")
    private Set<String> extractRoles(Claims claims) {
        List<String> roles = claims.get("roles", List.class);
        if (roles == null) return new HashSet<>(); // null safe
        return new HashSet<>(roles);               // returns actual roles
    }
}