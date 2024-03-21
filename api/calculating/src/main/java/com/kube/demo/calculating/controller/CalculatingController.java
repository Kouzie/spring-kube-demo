package com.kube.demo.calculating.controller;

import com.kube.demo.calculating.config.CalculatingConfiguration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Random;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/calculating")
public class CalculatingController {

    @Value("${demo}")
    private String demo;

    @Value("${is.poison}")
    private Boolean poison;

    private final CalculatingConfiguration config;

    @GetMapping
    public String calc() {
        log.info("calc invoked");
        return config.getTest() + ":" + demo;
    }

    @GetMapping("/{num1}/{num2}")
    public Long addNumbers(@PathVariable Long num1, @PathVariable Long num2) {
        log.info("addNumbers invoked, num1:{}, num2:{}", num1, num2);
        if (poison) return poison(num1, num2);
        else return num1 + num2;
    }

    Random random = new Random();

    private Long poison(Long num1, Long num2) {
        // 0과 1 사이의 무작위 수 생성
        double chance = random.nextDouble();
        // 30% 확률로 실패로 간주
        if (chance < 0.3) {
            throw new IllegalArgumentException("poison!!");
        }
        Long result = num1 + num2;
        return result;
    }

}

