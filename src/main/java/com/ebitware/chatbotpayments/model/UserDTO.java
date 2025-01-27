package com.ebitware.chatbotpayments.model;

import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
public class UserDTO {
    private Long id;

    @NotNull
    @Size(max = 50)
    private String firstName;

    @Size(max = 50)
    private String lastName;

    @Size(max = 50)
    private String secondLastName;

    @NotNull
    @Email
    @Size(max = 100)
    private String email;

    private Long companyId;

    private Long roleId;

    private Boolean isSuper;
}
