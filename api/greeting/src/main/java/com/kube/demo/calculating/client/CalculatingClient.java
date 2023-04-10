package com.kube.demo.calculating.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "calc-service")
public interface CalculatingClient {
    @GetMapping("/calculating/{num1}/{num2}")
    Long addNumbers(@PathVariable Long num1, @PathVariable Long num2);
}
