package com.ebitware.chatbotpayments.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Company {
    private Long id;
    private String name;
    private String keyName;
    private String mode;
    private Integer agents;
    private boolean multipleAgents;
    private Integer supervisors;
    private boolean multipleSupervisors;
    private Integer contacts;
    private String typeWorkgroups;
    private Integer workgroups;
    private boolean workgroupsActive;
    private boolean active;
    private ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;
}
