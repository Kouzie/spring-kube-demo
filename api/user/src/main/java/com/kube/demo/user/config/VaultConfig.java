package com.kube.demo.user.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.vault.authentication.TokenAuthentication;
import org.springframework.vault.client.VaultEndpoint;
import org.springframework.vault.core.VaultTemplate;
import org.springframework.beans.factory.annotation.Value;

import java.net.URI;

@Configuration
public class VaultConfig {
    
    @Value("${spring.cloud.vault.host:localhost}")
    private String vaultHost;
    
    @Value("${spring.cloud.vault.port:8200}")
    private int vaultPort;
    
    @Value("${spring.cloud.vault.scheme:http}")
    private String vaultScheme;
    
    @Value("${spring.cloud.vault.token:root}")
    private String vaultToken;
    
    @Bean
    public VaultTemplate vaultTemplate() {
        VaultEndpoint endpoint = VaultEndpoint.from(URI.create(String.format("%s://%s:%d", vaultScheme, vaultHost, vaultPort)));
        TokenAuthentication authentication = new TokenAuthentication(vaultToken);
        return new VaultTemplate(endpoint, authentication);
    }
}

