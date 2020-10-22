package com.sso.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor @Data
public class UserRegistrationDTO {
    private String email;
    private String password;
    private String firstName;
    private String lastName;
    private  int statusCode;
    private String status;
}
