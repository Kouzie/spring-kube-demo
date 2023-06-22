package com.kube.demo.region.presentation.repository;

import com.kube.demo.region.presentation.entity.LocationEntity;
import com.kube.demo.region.presentation.entity.RegionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LocationRepository extends JpaRepository<LocationEntity, Long> {

    List<LocationEntity> findByRegion(RegionEntity region);

}
