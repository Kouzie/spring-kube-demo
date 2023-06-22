package com.kube.demo.region.presentation.api;


import com.kube.demo.region.presentation.RegionApplicationTests;
import com.kube.demo.region.presentation.domain.model.Region;
import com.kube.demo.region.presentation.entity.RegionEntity;
import com.kube.demo.region.presentation.repository.RegionRepository;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.io.File;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
public class RegionApiMockTest extends RegionApplicationTests {

    @MockBean
    private RegionRepository repository;

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void testGetAllRegions() throws Exception {
        var regionDomain1 = new Region(1, "지역 1", LocalDateTime.now());
        var region1 = new RegionEntity(regionDomain1);

        var regionDomain2 = new Region(2, "지역 2", LocalDateTime.now());
        var region2 = new RegionEntity(regionDomain2);

        var regionList = List.of(region1, region2);
        BDDMockito.given(repository.findAll()).willReturn(regionList);
        File resource = new ClassPathResource("RegionApiMockTest_testGetAllRegions.json").getFile();
        String jsonString = new String(Files.readAllBytes(resource.toPath()));
        mockMvc.perform(get("/region").accept(MediaType.ALL_VALUE))
                .andDo(print()).andExpect(status().isOk())
                .andExpect(content().json(jsonString));
    }
}