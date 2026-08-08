package com.codingfactory.maintrack.dto;

import jakarta.validation.constraints.NotBlank;

public class LoginRequest {

    @NotBlank(message = "Το username είναι υποχρεωτικό")
    private String username;

    @NotBlank(message = "Ο κωδικός είναι υποχρεωτικός")
    private String password;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
