package com.kube.demo.region.presentation.api;


import com.kube.demo.region.presentation.RegionApplicationTests;
import com.kube.demo.region.presentation.domain.model.Region;
import com.kube.demo.region.presentation.entity.RegionEntity;
import com.kube.demo.region.presentation.repository.RegionRepository;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

// @SpringBootTest(classes = {RegionApi.class, RegionService.class, RegionRepository.class})
public class RegionApiTest  extends RegionApplicationTests {

    @Autowired
    private RegionApi api;

    @MockBean
    private RegionRepository repository;

    @Test
    public void testGetAllRegions() {

        var regionDomain1 = new Region(1, "지역 1", LocalDateTime.now());
        var region1 = new RegionEntity(regionDomain1);

        var regionDomain2 = new Region(2, "지역 2", LocalDateTime.now());
        var region2 = new RegionEntity(regionDomain2);

        var regionList = List.of(region1, region2);

        BDDMockito.given(repository.findAll()).willReturn(regionList);
        var result = api.getAllRegions();
        assertThat(result.getRegionList()).hasSize(2);
    }

}
