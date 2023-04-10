package com.kube.demo.calculating.contorller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kube.demo.calculating.client.CalculatingClient;
import com.kube.demo.common.HelloJava;
import lombok.RequiredArgsConstructor;
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
    private final ObjectMapper objectMapper;

    @GetMapping
    public String greet() throws JsonProcessingException {
        HelloJava helloJava = new HelloJava("Hello World", LocalDateTime.now());
        return objectMapper.writeValueAsString(helloJava);
    }

    @GetMapping("/{num1}/{num2}")
    public String testFeignClient(@PathVariable Long num1, @PathVariable Long num2) {
        return calculatingClient.addNumbers(num1, num2).toString();
    }
}

