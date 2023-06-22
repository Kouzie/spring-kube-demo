package com.kube.demo.region.presentation.entity;

import com.kube.demo.region.presentation.domain.model.Region;
import lombok.Getter;

import javax.persistence.*;
import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "REGION")
public class RegionEntity extends AbstractEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "REGION_ID")
    private Integer regionId;

    @Column(name = "REGION_NAME")
    private String regionName;

    @Column(name = "CREATION_TIMESTAMP")
    private LocalDateTime creationTimestamp;

    protected RegionEntity() {

    }

    public RegionEntity(Region region) {
        this.regionId = region.getRegionId();
        this.regionName = region.getRegionName();
        this.creationTimestamp = region.getCreationTimestamp();
    }
}
