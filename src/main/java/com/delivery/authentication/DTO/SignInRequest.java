package com.delivery.authentication.DTO;

import lombok.Data;

@Data
public class SignInRequest {
    private String email;
    private String password;
    private String role;

    public SignInRequest(String email, String password, String role) {
        this.email = email;
        this.password = password;
        this.role = role;
    }
}
