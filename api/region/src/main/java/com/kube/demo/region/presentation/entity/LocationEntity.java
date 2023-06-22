package com.kube.demo.region.presentation.entity;

import com.kube.demo.region.presentation.domain.model.Location;
import lombok.Getter;

import javax.persistence.*;

@Getter
@Entity
@Table(name = "LOCATION")
public class LocationEntity extends AbstractEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "LOCATION_ID")
    private Long locationId;

    @Column(name = "LOCATION_NAME")
    private String locationName;

    @ManyToOne
    @JoinColumn(name = "REGION_ID")
    private RegionEntity region;

    @Column(name = "NOTE")
    private String note;

    protected LocationEntity() {

    }

    public LocationEntity(Location domain) {
        this.locationName = domain.getLocationName();
        this.region = new RegionEntity(domain.getRegion());
        this.note = domain.getNote();
    }
}
