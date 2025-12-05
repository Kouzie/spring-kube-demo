package com.kube.demo.user.config;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JwtKey {
    private String keyId;
    private String secret;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
    private boolean active;
}

