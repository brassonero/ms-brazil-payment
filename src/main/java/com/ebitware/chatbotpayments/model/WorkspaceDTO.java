package com.ebitware.chatbotpayments.model;

import lombok.Data;

import java.util.List;

@Data
public class WorkspaceDTO {
    private boolean active;
    private String mode;
    private List<AccessDTO> accessList;
    private String agents;
    private boolean appleBot;
    private List<BotDTO> bots;
    private String contacts;
    private String keyName;
    private boolean multipleAgents;
    private boolean multipleSupervisors;
    private String name;
    private String supervisors;
    private String typeWorkgroups;
    private UserDTO user;
    private String workgroups;
    private boolean workgroupsActive;
}
