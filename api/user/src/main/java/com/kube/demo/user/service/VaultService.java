package com.kube.demo.user.service;

import com.kube.demo.user.config.JwtKey;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.vault.core.VaultTemplate;
import org.springframework.vault.support.VaultResponse;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class VaultService {
    
    private final VaultTemplate vaultTemplate;
    
    @Value("${spring.cloud.vault.kv.backend:secret}")
    private String backend;
    
    @Value("${spring.cloud.vault.kv.default-context:user-service}")
    private String context;
    
    /**
     * Vault에서 모든 JWT 키 가져오기
     */
    public List<JwtKey> getAllJwtKeys() {
        try {
            String path = String.format("%s/data/%s/jwt-keys", backend, context);
            VaultResponse response = vaultTemplate.read(path);
            
            if (response == null || response.getData() == null) {
                log.warn("No JWT keys found in Vault at path: {}", path);
                return new ArrayList<>();
            }
            
            Map<String, Object> data = (Map<String, Object>) response.getData().get("data");
            if (data == null) {
                return new ArrayList<>();
            }
            
            List<JwtKey> keys = new ArrayList<>();
            for (Map.Entry<String, Object> entry : data.entrySet()) {
                if (entry.getKey().startsWith("key_")) {
                    Map<String, String> keyData = (Map<String, String>) entry.getValue();
                    keys.add(JwtKey.builder()
                            .keyId(entry.getKey())
                            .secret(keyData.get("secret"))
                            .createdAt(LocalDateTime.parse(keyData.get("createdAt")))
                            .expiresAt(LocalDateTime.parse(keyData.get("expiresAt")))
                            .active(Boolean.parseBoolean(keyData.get("active")))
                            .build());
                }
            }
            
            return keys;
        } catch (Exception e) {
            log.error("Failed to retrieve JWT keys from Vault", e);
            return new ArrayList<>();
        }
    }
    
    /**
     * Vault에 새로운 JWT 키 저장
     */
    public void saveJwtKey(JwtKey key) {
        try {
            List<JwtKey> allKeys = getAllJwtKeys();
            allKeys.add(key);
            
            Map<String, Object> keysMap = new HashMap<>();
            for (JwtKey k : allKeys) {
                Map<String, String> keyData = new HashMap<>();
                keyData.put("secret", k.getSecret());
                keyData.put("createdAt", k.getCreatedAt().toString());
                keyData.put("expiresAt", k.getExpiresAt().toString());
                keyData.put("active", String.valueOf(k.isActive()));
                keysMap.put(k.getKeyId(), keyData);
            }
            
            Map<String, Object> data = new HashMap<>();
            data.put("data", keysMap);
            
            String path = String.format("%s/data/%s/jwt-keys", backend, context);
            vaultTemplate.write(path, data);
            
            log.info("Successfully saved JWT key {} to Vault", key.getKeyId());
        } catch (Exception e) {
            log.error("Failed to save JWT key to Vault", e);
            throw new RuntimeException("Failed to save JWT key", e);
        }
    }
    
    /**
     * Vault의 JWT 키 업데이트
     */
    public void updateJwtKeys(List<JwtKey> keys) {
        try {
            Map<String, Object> keysMap = new HashMap<>();
            for (JwtKey k : keys) {
                Map<String, String> keyData = new HashMap<>();
                keyData.put("secret", k.getSecret());
                keyData.put("createdAt", k.getCreatedAt().toString());
                keyData.put("expiresAt", k.getExpiresAt().toString());
                keyData.put("active", String.valueOf(k.isActive()));
                keysMap.put(k.getKeyId(), keyData);
            }
            
            Map<String, Object> data = new HashMap<>();
            data.put("data", keysMap);
            
            String path = String.format("%s/data/%s/jwt-keys", backend, context);
            vaultTemplate.write(path, data);
            
            log.info("Successfully updated {} JWT keys in Vault", keys.size());
        } catch (Exception e) {
            log.error("Failed to update JWT keys in Vault", e);
            throw new RuntimeException("Failed to update JWT keys", e);
        }
    }
    
    /**
     * 활성 키 가져오기
     */
    public List<JwtKey> getActiveKeys() {
        return getAllJwtKeys().stream()
                .filter(JwtKey::isActive)
                .filter(key -> key.getExpiresAt().isAfter(LocalDateTime.now()))
                .sorted(Comparator.comparing(JwtKey::getCreatedAt).reversed())
                .collect(Collectors.toList());
    }
    
    /**
     * 만료된 키 정리
     */
    public void cleanupExpiredKeys() {
        try {
            List<JwtKey> allKeys = getAllJwtKeys();
            LocalDateTime now = LocalDateTime.now();
            
            // 만료된 키 중에서도 최근 1시간 이내의 키는 유지 (토큰 검증용)
            List<JwtKey> validKeys = allKeys.stream()
                    .filter(key -> key.getExpiresAt().isAfter(now.minusHours(1)))
                    .collect(Collectors.toList());
            
            if (validKeys.size() < allKeys.size()) {
                updateJwtKeys(validKeys);
                log.info("Cleaned up {} expired JWT keys", allKeys.size() - validKeys.size());
            }
        } catch (Exception e) {
            log.error("Failed to cleanup expired keys", e);
        }
    }
}

