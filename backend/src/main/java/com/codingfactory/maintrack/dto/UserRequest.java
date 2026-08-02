package com.codingfactory.maintrack.dto;

import com.codingfactory.maintrack.model.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class UserRequest {

    @NotBlank(message = "To username einai ipoxreotiko")
    private String username;

    @NotBlank(message = "O kodikos einai ipoxreotikos")
    @Size(min = 4, message = "O kodikos prepei na exei toulaxiston 4 xaraktires")
    private String password;

    @NotBlank(message = "To onomatepwnymo einai ipoxreotiko")
    private String fullName;

    @NotNull(message = "O rolos einai ipoxreotikos")
    private Role role;

    // Proaiaretiko - den exei @NotBlank giati mporei na meinei kenos
    private String jobTitle;

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

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public String getJobTitle() {
        return jobTitle;
    }

    public void setJobTitle(String jobTitle) {
        this.jobTitle = jobTitle;
    }
}
