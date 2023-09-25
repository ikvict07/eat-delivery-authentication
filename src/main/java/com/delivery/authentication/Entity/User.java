package com.delivery.authentication.Entity;

import lombok.Data;

@Data
public class User {
    private Long id;

    private String name;
    private String password;
    private String email;
    private String phone;

    protected String role = "user";

}
