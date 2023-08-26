package com.delivery.authentication.Entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.Data;

import java.util.List;

@Data
public class Customer {
    private Long id;

    private String name;
    private String password;
    private String email;
    private String phone;

    private final String role = "CUSTOMER";

}
