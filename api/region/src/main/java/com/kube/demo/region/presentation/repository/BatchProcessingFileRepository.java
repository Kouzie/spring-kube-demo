package com.kube.demo.region.presentation.repository;

import com.kube.demo.region.presentation.entity.BatchProcessingFileEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BatchProcessingFileRepository extends JpaRepository<BatchProcessingFileEntity, Long> {

    void deleteByFileName(String fileName);

}
