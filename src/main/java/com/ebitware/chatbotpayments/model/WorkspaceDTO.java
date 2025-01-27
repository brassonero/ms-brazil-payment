package com.ebitware.chatbotpayments.model;

import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

@Data
public class WorkspaceDTO {
    private boolean active;

    @NotNull
    @Size(max = 50)
    private String mode;

    private List<AccessDTO> accessList;

    @NotNull
    private Integer agents;

    private boolean appleBot;

    private List<BotDTO> bots;

    @NotNull
    private Integer contacts;

    @NotNull
    @Size(max = 20)
    private String keyName;

    private boolean multipleAgents;

    private boolean multipleSupervisors;

    @NotNull
    @Size(max = 25)
    private String name;

    private Integer supervisors;

    private String typeWorkgroups;

    private UserDTO user;

    private Integer workgroups;

    private boolean workgroupsActive;
}
