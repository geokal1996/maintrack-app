package com.codingfactory.maintrack.dto;

import com.codingfactory.maintrack.model.Role;
import jakarta.validation.constraints.NotNull;

// Allagi rolou se yparxonta xristi (p.x. proagogi texnikou se epopti).
// Isxyoun oi idioi kanones ierarxias me ti dimiourgia: kaneis den mporei na dosei
// rolo iso i anotero apo ton diko tou.
public class RoleUpdateRequest {

    @NotNull(message = "Ο ρόλος είναι υποχρεωτικός")
    private Role role;

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }
}
