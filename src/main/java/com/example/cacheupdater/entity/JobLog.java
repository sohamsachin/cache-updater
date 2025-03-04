package com.example.cacheupdater.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "JOB_LOG")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class JobLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "JOB_NAME")
    private String jobName;

    @Column(name = "START_TIME")
    private LocalDateTime startTime;

    @Column(name = "END_TIME")
    private LocalDateTime endTime;

    @Column(name = "DURATION_MS")
    private Long duration;

    @Column(name = "STATUS")
    private String status;

    @Column(name = "ERROR_MESSAGE", length = 4000)
    private String errorMessage;
}