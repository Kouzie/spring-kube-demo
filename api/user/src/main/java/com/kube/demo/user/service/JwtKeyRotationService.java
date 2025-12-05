package com.kube.demo.user.service;

import com.kube.demo.user.config.JwtKey;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class JwtKeyRotationService {
    
    private final VaultService vaultService;
    
    @Value("${jwt.key-rotation.interval:1800000}") // 30분 (밀리초)
    private long rotationInterval;
    
    private final SecureRandom secureRandom = new SecureRandom();
    
    /**
     * 애플리케이션 시작 시 초기 키 설정
     */
    @PostConstruct
    public void initialize() {
        log.info("Initializing JWT key rotation service");
        
        List<JwtKey> activeKeys = vaultService.getActiveKeys();
        
        if (activeKeys.isEmpty()) {
            log.info("No active keys found, generating initial key");
            generateAndSaveNewKey();
        } else {
            log.info("Found {} active keys", activeKeys.size());
        }
    }
    
    /**
     * 30분마다 자동으로 키 로테이션 (설정 가능)
     */
    @Scheduled(fixedDelayString = "${jwt.key-rotation.interval:1800000}")
    public void rotateKeys() {
        log.info("Starting JWT key rotation");
        
        try {
            // 새로운 키 생성 및 저장
            generateAndSaveNewKey();
            
            // 이전 키들의 상태 업데이트 (active는 유지하되 만료 시간만 설정)
            updatePreviousKeys();
            
            // 오래된 키 정리
            vaultService.cleanupExpiredKeys();
            
            log.info("JWT key rotation completed successfully");
        } catch (Exception e) {
            log.error("Failed to rotate JWT keys", e);
        }
    }
    
    /**
     * 새로운 JWT 키 생성 및 저장
     */
    private void generateAndSaveNewKey() {
        String keyId = "key_" + UUID.randomUUID().toString().replace("-", "");
        String secret = generateSecureSecret();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiresAt = now.plusMinutes(60); // 키는 60분 후 만료 (30분 중복 기간 보장)
        
        JwtKey newKey = JwtKey.builder()
                .keyId(keyId)
                .secret(secret)
                .createdAt(now)
                .expiresAt(expiresAt)
                .active(true)
                .build();
        
        vaultService.saveJwtKey(newKey);
        
        log.info("Generated new JWT key: {} (expires at: {})", keyId, expiresAt);
    }
    
    /**
     * 이전 키들의 상태 업데이트
     */
    private void updatePreviousKeys() {
        List<JwtKey> allKeys = vaultService.getAllJwtKeys();
        LocalDateTime now = LocalDateTime.now();
        
        // 만료되지 않은 키들은 active 상태 유지
        for (JwtKey key : allKeys) {
            if (key.getExpiresAt().isBefore(now)) {
                key.setActive(false);
            }
        }
        
        vaultService.updateJwtKeys(allKeys);
    }
    
    /**
     * 안전한 시크릿 키 생성 (256-bit)
     */
    private String generateSecureSecret() {
        byte[] key = new byte[32]; // 256 bits
        secureRandom.nextBytes(key);
        return Base64.getEncoder().encodeToString(key);
    }
    
    /**
     * 현재 활성 키 개수 조회
     */
    public int getActiveKeyCount() {
        return vaultService.getActiveKeys().size();
    }
    
    /**
     * 수동으로 키 로테이션 트리거
     */
    public void manualRotation() {
        log.info("Manual key rotation triggered");
        rotateKeys();
    }
}

