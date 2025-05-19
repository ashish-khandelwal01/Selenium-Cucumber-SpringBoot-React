package com.framework.apiserver.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "test_run_info")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TestRunInfoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String runId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    private int durationSeconds;
    private int total;
    private int passed;
    private int failed;
    private String status;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "test_run_failures", joinColumns = @JoinColumn(name = "test_run_id"))
    @Column(name = "scenario")
    private List<String> failureScenarios;
}
