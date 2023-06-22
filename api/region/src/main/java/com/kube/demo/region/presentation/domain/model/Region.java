package com.kube.demo.region.presentation.domain.model;


import com.kube.demo.region.presentation.entity.RegionEntity;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class Region {

    private Integer regionId;

    private String regionName;

    private LocalDateTime creationTimestamp;

    public Region(Integer regionId, String regionName, LocalDateTime creationTimestamp) {
        if (regionName == null) {
            throw new IllegalArgumentException("regionName cannot be null.");
        }
        this.regionId = regionId;
        this.regionName = regionName;
        this.creationTimestamp = creationTimestamp;
    }

    public Region(RegionEntity entity) {
        this(entity.getRegionId(), entity.getRegionName(), entity.getCreationTimestamp());
    }
}
