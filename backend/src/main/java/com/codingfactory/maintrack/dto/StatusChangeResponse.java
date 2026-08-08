package com.codingfactory.maintrack.dto;

import com.codingfactory.maintrack.model.FaultStatus;
import com.codingfactory.maintrack.model.FaultStatusChange;

import java.time.LocalDateTime;

public class StatusChangeResponse {

    private Long id;
    private FaultStatus fromStatus;
    private FaultStatus toStatus;
    private String changedByUsername;
    private String changedByFullName;
    private LocalDateTime changedAt;

    public static StatusChangeResponse from(FaultStatusChange change) {
        StatusChangeResponse dto = new StatusChangeResponse();
        dto.id = change.getId();
        dto.fromStatus = change.getFromStatus();
        dto.toStatus = change.getToStatus();
        if (change.getChangedBy() != null) {
            dto.changedByUsername = change.getChangedBy().getUsername();
            dto.changedByFullName = change.getChangedBy().getFullName();
        }
        dto.changedAt = change.getChangedAt();
        return dto;
    }

    public Long getId() {
        return id;
    }

    public FaultStatus getFromStatus() {
        return fromStatus;
    }

    public FaultStatus getToStatus() {
        return toStatus;
    }

    public String getChangedByUsername() {
        return changedByUsername;
    }

    public String getChangedByFullName() {
        return changedByFullName;
    }

    public LocalDateTime getChangedAt() {
        return changedAt;
    }
}
