package com.codingfactory.maintrack.dto;

import jakarta.validation.constraints.NotBlank;

public class LoginRequest {

    @NotBlank(message = "To username einai ipoxreotiko")
    private String username;

    @NotBlank(message = "O kodikos einai ipoxreotikos")
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
