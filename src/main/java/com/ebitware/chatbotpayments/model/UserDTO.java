package com.ebitware.chatbotpayments.model;

import lombok.Data;

@Data
public class UserDTO {
    private Long id;
    private String firstName;
    private String lastName;
    private String secondLastName;
    private String email;
    private Long companyId;
    private Long roleId;
    private Boolean isSuper;
}
