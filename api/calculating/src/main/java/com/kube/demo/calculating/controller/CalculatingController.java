package com.kube.demo.calculating.controller;

import com.kube.demo.calculating.config.CalculatingConfiguration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/calculating")
public class CalculatingController {

    @Value("${demo}")
    private String demo;

    private final CalculatingConfiguration config;

    @GetMapping
    public String calc() {
        log.info("calc invoked");
        return config.getTest() + ":" + demo;
    }

    @GetMapping("/{num1}/{num2}")
    public Long addNumbers(@PathVariable Long num1, @PathVariable Long num2) {
        log.info("addNumbers invoked, num1:{}, num2:{}", num1, num2);
        Long result = num1 + num2;
        return result;
    }
}

