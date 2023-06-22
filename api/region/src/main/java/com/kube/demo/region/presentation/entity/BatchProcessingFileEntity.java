package com.kube.demo.region.presentation.entity;

import lombok.Getter;

import javax.persistence.*;

@Getter
@Entity
@Table(name = "BATCH_PROCESSING_FILE")
public class BatchProcessingFileEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long batchProcessingFileId;

    @ManyToOne
    @JoinColumn(name = "BATCH_NAME", nullable = false)
    private BatchProcessingEntity batchProcessing;

    @Column(name = "FILE_NAME", length = 300, nullable = false)
    private String fileName;

    protected BatchProcessingFileEntity() {
    }
}
