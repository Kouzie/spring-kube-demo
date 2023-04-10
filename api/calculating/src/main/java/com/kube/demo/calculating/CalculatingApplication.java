package com.kube.demo.calculating;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class CalculatingApplication {
    public static void main(String[] args) {
        SpringApplication.run(CalculatingApplication.class, args);
    }
}
