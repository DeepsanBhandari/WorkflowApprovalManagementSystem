package com.dep.Workflow.model;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Approval {
    private Long id;
    private String title;
    private String description;
    private String status;  // PENDING, APPROVED, REJECTED
    private String requestedBy;
    private LocalDateTime createdAt;


    public Approval(String title, String description, String requestedBy) {
    }
}
