package com.kube.demo.greeting.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name="calc", url = "http://calc-service:8080")
public interface CalculatingClient {
    @GetMapping("/calculating/{num1}/{num2}")
    Long addNumbers(@PathVariable Long num1, @PathVariable Long num2);
}
