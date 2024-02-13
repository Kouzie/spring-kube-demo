package com.kube.demo.greeting.contorller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kube.demo.common.HelloJava;
import com.kube.demo.greeting.client.CalculatingClient;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.api.metrics.Meter;
import jakarta.annotation.PostConstruct;
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

    private final Meter meter;
    private LongCounter counter;
    private Attributes attributes;

    @PostConstruct
    private void init() {
        // Build counter e.g. LongCounter
        this.counter = meter
                .counterBuilder("processed_jobs")
                .setDescription("Processed jobs")
                .setUnit("1")
                .build();
        this.attributes = Attributes.of(AttributeKey.stringKey("Key"), "SomeWork");
    }

    @GetMapping
    public String greet() throws JsonProcessingException {
        log.info("greet invoked");
        // meter.counterBuilder("item.num", "num", "1").increment();
        counter.add(1, attributes);
        HelloJava helloJava = new HelloJava(greetingMessage, LocalDateTime.now());
        return objectMapper.writeValueAsString(helloJava);
    }

    @GetMapping("/{num1}/{num2}")
    public String calculate(@PathVariable Long num1, @PathVariable Long num2) {
        log.info("calculate invoked, num1:{}, num2:{}", num1, num2);
        return calculatingClient.addNumbers(num1, num2).toString();
    }
}

