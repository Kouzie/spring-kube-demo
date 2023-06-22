package com.kube.demo.region.presentation.api;

import com.kube.demo.region.presentation.RegionApplicationTests;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class HealthApiTest extends RegionApplicationTests {

    @Test
    public void testHealthOk() {
        var api = new HealthApi();
        var health = api.getHealth();
        assertThat(health.getStatus()).isEqualTo("OK");
    }

}
