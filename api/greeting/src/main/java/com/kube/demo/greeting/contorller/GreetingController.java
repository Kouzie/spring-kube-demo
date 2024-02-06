package com.kube.demo.greeting.contorller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kube.demo.common.HelloJava;
import com.kube.demo.greeting.client.CalculatingClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@Slf4j
@RestController
@RequestMapping("/greeting")
@RequiredArgsConstructor
public class GreetingController {
    private final CalculatingClient calculatingClient;
    private final ObjectMapper objectMapper;

    @Value("${greeting.message}")
    private String greetingMessage;

    @GetMapping
    public String greet() throws JsonProcessingException {
        log.info("greet invoked");
        HelloJava helloJava = new HelloJava(greetingMessage, LocalDateTime.now());
        return objectMapper.writeValueAsString(helloJava);
    }

    @GetMapping("/{num1}/{num2}")
    public String calculate(@PathVariable Long num1, @PathVariable Long num2) {
        log.info("calculate invoked, num1:{}, num2:{}", num1, num2);
        return calculatingClient.addNumbers(num1, num2).toString();
    }
}

