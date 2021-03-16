package com.spring.kube.demo.calculating.controller;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/calculating")
public class CalculatingController {

    @Value("${test.config.}")
    private String test;

    private final CalculatingConfiguration config;

    @GetMapping
    public String calc() {
        return config.getTest() + ":" + test;
    }

    @GetMapping("/{num1}/{num2}")
    public Long addNumbers(@PathVariable Long num1, @PathVariable Long num2) {
        Long result = num1 + num2;
        return result;
    }
}

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "calc")
class CalculatingConfiguration {
    private String test;
}
