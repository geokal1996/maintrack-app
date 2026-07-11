package com.codingfactory.maintrack.dto;

import com.codingfactory.maintrack.model.MaintenanceAction;

import java.time.LocalDateTime;

public class MaintenanceActionResponse {

    private Long id;
    private Long faultId;
    private Long technicianUserId;
    private String technicianUsername;
    private String description;
    private LocalDateTime actionDate;
    private Integer downtimeMinutes;

    public static MaintenanceActionResponse from(MaintenanceAction action) {
        MaintenanceActionResponse dto = new MaintenanceActionResponse();
        dto.id = action.getId();
        dto.faultId = action.getFault().getId();
        dto.technicianUserId = action.getTechnician().getId();
        dto.technicianUsername = action.getTechnician().getUsername();
        dto.description = action.getDescription();
        dto.actionDate = action.getActionDate();
        dto.downtimeMinutes = action.getDowntimeMinutes();
        return dto;
    }

    public Long getId() {
        return id;
    }

    public Long getFaultId() {
        return faultId;
    }

    public Long getTechnicianUserId() {
        return technicianUserId;
    }

    public String getTechnicianUsername() {
        return technicianUsername;
    }

    public String getDescription() {
        return description;
    }

    public LocalDateTime getActionDate() {
        return actionDate;
    }

    public Integer getDowntimeMinutes() {
        return downtimeMinutes;
    }
}
