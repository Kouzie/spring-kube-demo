package com.kube.demo.region.presentation.entity;

import lombok.Getter;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Entity
@Table(name = "BATCH_PROCESSING")
public class BatchProcessingEntity extends AbstractEntity {

    @Id
    @Column(name = "BATCH_NAME", length = 20, nullable = false)
    private String batchName;

    @Column(name = "LAST_EXECUTION_DATE_TIME")
    private LocalDateTime lastExecutionDateTime;

    @OneToMany(mappedBy = "batchProcessing", cascade = CascadeType.ALL)
    private List<BatchProcessingFileEntity> fileList;

    protected BatchProcessingEntity() {
    }
}