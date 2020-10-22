package com.sso.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor @Data
public class UserLoginDTO {
    private String username;
    private String password;
}
