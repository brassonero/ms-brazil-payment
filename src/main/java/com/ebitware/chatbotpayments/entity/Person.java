package com.ebitware.chatbotpayments.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Person {
    private Long id;
    private String firstName;
    private String lastName;
    private String secondLastName;
    private String username;
    private String email;
    private String password;
    private Boolean firstLogin;
    private Long companyId;
    private Long roleId;
    private Boolean isSuper;

    public String getFullName() {
        return firstName + " " + lastName;
    }
}
