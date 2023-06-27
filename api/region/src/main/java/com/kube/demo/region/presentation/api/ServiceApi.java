package com.kube.demo.region.presentation.api;


import com.kube.demo.common.HelloJava;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/service")
public class ServiceApi {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceApi.class);

    @Autowired
    private RestTemplate restTemplate;

    @GetMapping("/greeting")
    public HelloJava greeting() {
        HelloJava result = restTemplate.getForObject("http://greet-service:8080", HelloJava.class);
        return result;
    }

    @GetMapping("/calculating")
    public Long calculating() {
        Long result = restTemplate.getForObject("http://calc-service:8080/calculating/{num1}/{num2}", Long.class, 1, 2);

        return result;
    }

}
