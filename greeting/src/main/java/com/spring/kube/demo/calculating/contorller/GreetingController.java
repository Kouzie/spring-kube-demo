package com.spring.kube.demo.calculating.contorller;

import lombok.RequiredArgsConstructor;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/greeting")
@RequiredArgsConstructor
public class GreetingController {
    private final CalculatingClient calculatingClient;

    @GetMapping
    public String greet() {
        LocalDateTime now = LocalDateTime.now();
        return "Hello World";
    }

    @GetMapping("/{num1}/{num2}")
    public String testFeignClient(@PathVariable Long num1, @PathVariable Long num2) {
        return calculatingClient.addNumbers(num1, num2).toString();
    }
}

@FeignClient(name = "calc-service")
interface CalculatingClient {
    @GetMapping("/calculating/{num1}/{num2}")
    Long addNumbers(@PathVariable Long num1, @PathVariable Long num2);
}
