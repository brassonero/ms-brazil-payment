package com.ebitware.chatbotpayments.model;

import java.util.List;
import lombok.Data;

@Data
public class CompanyRequest {
    private boolean active;
    private String mode;
    private List<AccessList> accessList;
    private String agents;
    private boolean appleBot;
    private List<Bot> bots;
    private String contacts;
    private String keyName;
    private boolean multipleAgents;
    private boolean multipleSupervisors;
    private String name;
    private String supervisors;
    private String typeWorkgroups;
    private UserRequest user;
    private String workgroups;
    private boolean workgroupsActive;

    @Data
    public static class AccessList {
        private Long id;
        private String name;
        private String keyName;
        private String createdAt;
    }

    @Data
    public static class Bot {
        private String name;
        private String port;
        private String customMessage;
        private String message;
    }

    @Data
    public static class UserRequest {
        private String firstName;
        private String lastName;
        private String secondLastName;
        private String email;
    }
}
