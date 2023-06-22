package com.kube.demo.region.presentation.dto;


import com.kube.demo.region.presentation.domain.model.Location;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class LocationDto {

    private Long locationId;

    private String locationName;

    private RegionDto region;

    private String note;

    public LocationDto(Location location) {
        this.locationId = location.getLocationId();
        this.locationName = location.getLocationName();
        this.region = new RegionDto(location.getRegion());
        this.note = location.getNote();
    }
}
