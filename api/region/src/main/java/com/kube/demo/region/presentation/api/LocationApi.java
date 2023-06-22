package com.kube.demo.region.presentation.api;


import com.kube.demo.region.presentation.domain.service.LocationService;
import com.kube.demo.region.presentation.dto.LocationDto;
import com.kube.demo.region.presentation.dto.LocationsDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;

@RestController
@RequestMapping("location")
@CrossOrigin("*")
public class LocationApi {

    private static final Logger LOGGER = LoggerFactory.getLogger(LocationApi.class);

    @Autowired
    private LocationService service;

    @GetMapping(value = "region/{regionId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public LocationsDto getLocationListByRegion(@PathVariable("regionId") Integer regionId) {
        LOGGER.info("LOCATION LIST BY REGION ID API");

        var locationList = service.getLocationListByRegionId(regionId);
        var dtoList = new ArrayList<LocationDto>();
        locationList.forEach(location -> {
            var dto = new LocationDto(location);
            dtoList.add(dto);
        });
        var locationsDto = new LocationsDto(dtoList);
        return locationsDto;
    }

}
