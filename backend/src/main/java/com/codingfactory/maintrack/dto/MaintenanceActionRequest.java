package com.codingfactory.maintrack.dto;

import jakarta.validation.constraints.NotNull;

public class MaintenanceActionRequest {

    @NotNull(message = "To technicianUserId einai ipoxreotiko")
    private Long technicianUserId;

    private String description;

    private Integer downtimeMinutes;

    public Long getTechnicianUserId() {
        return technicianUserId;
    }

    public void setTechnicianUserId(Long technicianUserId) {
        this.technicianUserId = technicianUserId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getDowntimeMinutes() {
        return downtimeMinutes;
    }

    public void setDowntimeMinutes(Integer downtimeMinutes) {
        this.downtimeMinutes = downtimeMinutes;
    }
}
