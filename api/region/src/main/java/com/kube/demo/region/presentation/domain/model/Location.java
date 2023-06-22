package com.kube.demo.region.presentation.domain.model;


import com.kube.demo.region.presentation.entity.LocationEntity;
import lombok.Getter;

@Getter
public class Location {

    private Long locationId;

    private String locationName;

    private Region region;

    private String note;

    public Location(Long locationId, String locationName, Region region, String note) {
        if (locationName == null) {
            throw new IllegalArgumentException("locationName cannot be null.");
        }
        if (region == null) {
            throw new IllegalArgumentException("region cannot be null.");
        }
        this.locationId = locationId;
        this.locationName = locationName;
        this.region = region;
        this.note = note;
    }

    public Location(String locationName, Region region, String note) {
        this(null, locationName, region, note);
    }

    public Location(LocationEntity entity) {
        this(entity.getLocationId(), entity.getLocationName(), new Region(entity.getRegion()), entity.getNote());
    }

    public void setRegion(Region region) {
        this.region = region;
    }
}
