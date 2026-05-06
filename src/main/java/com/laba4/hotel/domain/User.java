package com.laba4.hotel.domain;

public class User {
    private Long id;
    private String email;
    private String fullName;
    private String phone;
    private String password;
    private String role; // e.g. "guest", "staff"

    public User(Long id, String email, String fullName, String phone, String password, String role) {
        this.id = id;
        this.email = email;
        this.fullName = fullName;
        this.phone = phone;
        this.password = password;
        this.role = role;
    }

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getFullName() {
        return fullName;
    }

    public String getPhone() {
        return phone;
    }

    public String getPassword() {
        return password;
    }

    public String getRole() {
        return role;
    }
}

