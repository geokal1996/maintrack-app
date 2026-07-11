package com.codingfactory.maintrack.dto;

import com.codingfactory.maintrack.model.Role;
import com.codingfactory.maintrack.model.User;

// PROSOXI: DEN exei pedio "password" - auto einai to basiko noima tou DTO edo.
// O controller/service pote den stelnei piso ton (kryptografimeno) kodiko ston client.
public class UserResponse {

    private Long id;
    private String username;
    private String fullName;
    private Role role;
    private boolean active;

    public static UserResponse from(User user) {
        UserResponse dto = new UserResponse();
        dto.id = user.getId();
        dto.username = user.getUsername();
        dto.fullName = user.getFullName();
        dto.role = user.getRole();
        dto.active = user.isActive();
        return dto;
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getFullName() {
        return fullName;
    }

    public Role getRole() {
        return role;
    }

    public boolean isActive() {
        return active;
    }
}
