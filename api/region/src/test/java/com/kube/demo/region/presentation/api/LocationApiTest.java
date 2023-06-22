package com.kube.demo.region.presentation.api;


import com.kube.demo.region.presentation.RegionApplicationTests;
import com.kube.demo.region.presentation.domain.model.Location;
import com.kube.demo.region.presentation.domain.model.Region;
import com.kube.demo.region.presentation.entity.LocationEntity;
import com.kube.demo.region.presentation.entity.RegionEntity;
import com.kube.demo.region.presentation.repository.LocationRepository;
import com.kube.demo.region.presentation.repository.RegionRepository;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

//@SpringBootTest(classes = {LocationApi.class, LocationService.class, LocationRepository.class, RegionRepository.class})
public class LocationApiTest extends RegionApplicationTests {

    @Autowired
    private LocationApi api;

    @MockBean
    private RegionRepository regionRepository;

    @MockBean
    private LocationRepository locationRepository;

    @Test
    public void testGetLocationListByRegion() {
        var regionDomain = new Region(1, "지역 1", LocalDateTime.now());
        var regionEntity = new RegionEntity(regionDomain);
        BDDMockito.given(regionRepository.findById(1)).willReturn(Optional.of(regionEntity));

        var locationDomain1 = new Location(1l, "명소 1", regionDomain, "명소 1의 상세 정보입니다.");
        var loc1 = new LocationEntity(locationDomain1);

        var locationDomain2 = new Location(2l, "명소 2", regionDomain, "명소 2의 상세 정보입니다.");
        var loc2 = new LocationEntity(locationDomain2);

        var locationList = List.of(loc1, loc2);
        BDDMockito.given(locationRepository.findByRegion(regionEntity)).willReturn(locationList);

        var result = api.getLocationListByRegion(1);
        assertThat(result.getLocationList()).hasSize(2);
    }

}
