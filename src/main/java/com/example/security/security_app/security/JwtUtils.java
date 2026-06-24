package com.example.security.security_app.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
@Slf4j
public class JwtUtils {

    @Value("${app.jwt.secret}")
    private String secretKey;

    @Value("${app.jwt.expiration}")
    private long jwtExpiration;

    @Value("${app.jwt.refresh-expiration}")
    private long refreshExpiration;

    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateAccessToken(String username,
                                      Set<String> roles) {
        return Jwts.builder()
                .subject(username)
                .claim("roles", roles)
                .claim("type", "ACCESS")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(getSigningKey())
                .compact();
    }

    public Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String extractUsername(String token) {
        return parseClaims(token).getSubject();
    }

    public String extractTenantId(String token) {
        return parseClaims(token).get("tenantId", String.class);
    }

    public String extractSubdomain(String token) {
        return parseClaims(token).get("subdomain", String.class);
    }

    @SuppressWarnings("unchecked")
    public Set<String> extractRoles(String token) {
        List<String> roles = parseClaims(token).get("roles", List.class);
        return new HashSet<>(roles);
    }

     public boolean isTokenValid(String token) {
        try {
            Claims claims = parseClaims(token);
            return !claims.getExpiration().before(new Date());
        } catch (JwtException | IllegalArgumentException e) {
            log.info("Invalid JWT Token", e);
            return false;
        }
    }

    public boolean isAccessToken(String token) {
        return "ACCESS".equals(parseClaims(token).get("type", String.class));
    }

    public String generateRefreshToken(String username) {
        return Jwts.builder()
                .subject(username)
                .claim("type", "REFRESH")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + refreshExpiration))
                .signWith(getSigningKey())
                .compact();
    }

    public long getRemainingExpiry(String token) {
        Claims claims = parseClaims(token);
        long expiry   = claims.getExpiration().getTime();
        long now      = System.currentTimeMillis();
        return expiry - now;  // remaining ms until expiry
    }

    public Claims parseClaimsIgnoreExpiry(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(getSigningKey())
                    .clockSkewSeconds(Integer.MAX_VALUE)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (JwtException | IllegalArgumentException e) {
            log.error("Cannot parse token even ignoring expiry: {}",
                    e.getMessage());
            throw new AccessDeniedException("Invalid token");
        }
    }

    public boolean isTokenExpired(String token) {
        try {
            Claims claims = parseClaims(token);
            return claims.getExpiration().before(new Date());
        } catch (ExpiredJwtException e) {
            return true;
        }
    }

}
