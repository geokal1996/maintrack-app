package com.codingfactory.maintrack.dto;

public class LoginResponse {

    private Long userId;
    private String token;
    private String username;
    private String fullName;
    private String role;

    public LoginResponse(Long userId, String token, String username, String fullName, String role) {
        this.userId = userId;
        this.token = token;
        this.username = username;
        this.fullName = fullName;
        this.role = role;
    }

    public Long getUserId() {
        return userId;
    }

    public String getToken() {
        return token;
    }

    public String getUsername() {
        return username;
    }

    public String getFullName() {
        return fullName;
    }

    public String getRole() {
        return role;
    }
}
