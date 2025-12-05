package com.kube.demo.user.service;

import com.kube.demo.user.config.JwtKey;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class JwtService {
    
    private final VaultService vaultService;
    
    @Value("${jwt.access-token.expiration:1800000}") // 30분
    private long accessTokenExpiration;
    
    @Value("${jwt.refresh-token.expiration:86400000}") // 24시간
    private long refreshTokenExpiration;
    
    /**
     * Access Token 생성 (최신 활성 키 사용)
     */
    public String generateAccessToken(String username, String role) {
        List<JwtKey> activeKeys = vaultService.getActiveKeys();
        
        if (activeKeys.isEmpty()) {
            throw new RuntimeException("No active JWT keys available");
        }
        
        // 가장 최신 키 사용
        JwtKey latestKey = activeKeys.get(0);
        
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", role);
        claims.put("type", "access");
        
        return createToken(claims, username, accessTokenExpiration, latestKey);
    }
    
    /**
     * Refresh Token 생성
     */
    public String generateRefreshToken(String username) {
        List<JwtKey> activeKeys = vaultService.getActiveKeys();
        
        if (activeKeys.isEmpty()) {
            throw new RuntimeException("No active JWT keys available");
        }
        
        JwtKey latestKey = activeKeys.get(0);
        
        Map<String, Object> claims = new HashMap<>();
        claims.put("type", "refresh");
        
        return createToken(claims, username, refreshTokenExpiration, latestKey);
    }
    
    /**
     * JWT 토큰 생성
     */
    private String createToken(Map<String, Object> claims, String subject, long expiration, JwtKey jwtKey) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);
        
        SecretKey key = Keys.hmacShaKeyFor(jwtKey.getSecret().getBytes(StandardCharsets.UTF_8));
        
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .setHeaderParam("kid", jwtKey.getKeyId()) // Key ID 헤더에 포함
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }
    
    /**
     * 토큰에서 사용자명 추출
     */
    public String extractUsername(String token) {
        return extractClaims(token).getSubject();
    }
    
    /**
     * 토큰에서 Role 추출
     */
    public String extractRole(String token) {
        return (String) extractClaims(token).get("role");
    }
    
    /**
     * 토큰 유효성 검증 (모든 활성 키로 시도)
     */
    public boolean validateToken(String token) {
        try {
            extractClaims(token);
            return true;
        } catch (Exception e) {
            log.debug("Token validation failed: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * 토큰에서 Claims 추출 (멀티 키 지원)
     */
    private Claims extractClaims(String token) {
        // 토큰 헤더에서 Key ID 추출
        String keyId = extractKeyId(token);
        
        // 모든 활성 키 가져오기
        List<JwtKey> activeKeys = vaultService.getActiveKeys();
        
        // Key ID가 있으면 해당 키로 먼저 시도
        if (keyId != null) {
            Optional<JwtKey> matchingKey = activeKeys.stream()
                    .filter(k -> k.getKeyId().equals(keyId))
                    .findFirst();
            
            if (matchingKey.isPresent()) {
                try {
                    return parseToken(token, matchingKey.get());
                } catch (Exception e) {
                    log.debug("Failed to parse token with key ID: {}", keyId);
                }
            }
        }
        
        // Key ID가 없거나 실패한 경우, 모든 활성 키로 시도
        for (JwtKey jwtKey : activeKeys) {
            try {
                return parseToken(token, jwtKey);
            } catch (Exception e) {
                // 다음 키로 시도
                log.debug("Failed to parse token with key: {}", jwtKey.getKeyId());
            }
        }
        
        throw new JwtException("Token validation failed with all available keys");
    }
    
    /**
     * 토큰 파싱
     */
    private Claims parseToken(String token, JwtKey jwtKey) {
        SecretKey key = Keys.hmacShaKeyFor(jwtKey.getSecret().getBytes(StandardCharsets.UTF_8));
        
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
    
    /**
     * 토큰 헤더에서 Key ID 추출
     */
    private String extractKeyId(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length < 2) {
                return null;
            }
            
            String header = new String(Base64.getUrlDecoder().decode(parts[0]), StandardCharsets.UTF_8);
            
            // JSON 파싱 간단히 처리 (실제로는 Jackson 사용 권장)
            if (header.contains("\"kid\"")) {
                int kidStart = header.indexOf("\"kid\"") + 6;
                int valueStart = header.indexOf("\"", kidStart) + 1;
                int valueEnd = header.indexOf("\"", valueStart);
                return header.substring(valueStart, valueEnd);
            }
        } catch (Exception e) {
            log.debug("Failed to extract key ID from token", e);
        }
        return null;
    }
    
    /**
     * 토큰 만료 확인
     */
    public boolean isTokenExpired(String token) {
        try {
            Claims claims = extractClaims(token);
            return claims.getExpiration().before(new Date());
        } catch (Exception e) {
            return true;
        }
    }
}

