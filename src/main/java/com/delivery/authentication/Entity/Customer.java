package com.delivery.authentication.Entity;

public class Customer extends User {
    public Customer() {
        super();
        this.role = ROLES.CUSTOMER_ROLE.role;
    }
}
