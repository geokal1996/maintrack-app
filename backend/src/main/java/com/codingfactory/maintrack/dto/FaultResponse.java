package com.codingfactory.maintrack.dto;

import com.codingfactory.maintrack.model.Fault;
import com.codingfactory.maintrack.model.FaultSeverity;
import com.codingfactory.maintrack.model.FaultStatus;

import java.time.LocalDateTime;

public class FaultResponse {

    private Long id;
    private Long machineId;
    private String machineCode;
    private String machineName;
    private Long reportedByUserId;
    private String reportedByUsername;
    private Long assignedToUserId;
    private String assignedToUsername;
    private String assignedToFullName;
    private String title;
    private String description;
    private FaultSeverity severity;
    private FaultStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime resolvedAt;
    private String externalRef;

    public static FaultResponse from(Fault fault) {
        FaultResponse dto = new FaultResponse();
        dto.id = fault.getId();
        dto.machineId = fault.getMachine().getId();
        dto.machineCode = fault.getMachine().getCode();
        dto.machineName = fault.getMachine().getName();
        dto.reportedByUserId = fault.getReportedBy().getId();
        dto.reportedByUsername = fault.getReportedBy().getUsername();
        if (fault.getAssignedTo() != null) {
            dto.assignedToUserId = fault.getAssignedTo().getId();
            dto.assignedToUsername = fault.getAssignedTo().getUsername();
            dto.assignedToFullName = fault.getAssignedTo().getFullName();
        }
        dto.title = fault.getTitle();
        dto.description = fault.getDescription();
        dto.severity = fault.getSeverity();
        dto.status = fault.getStatus();
        dto.createdAt = fault.getCreatedAt();
        dto.resolvedAt = fault.getResolvedAt();
        dto.externalRef = fault.getExternalRef();
        return dto;
    }

    public Long getId() {
        return id;
    }

    public Long getMachineId() {
        return machineId;
    }

    public String getMachineCode() {
        return machineCode;
    }

    public String getMachineName() {
        return machineName;
    }

    public Long getReportedByUserId() {
        return reportedByUserId;
    }

    public String getReportedByUsername() {
        return reportedByUsername;
    }

    public Long getAssignedToUserId() {
        return assignedToUserId;
    }

    public String getAssignedToUsername() {
        return assignedToUsername;
    }

    public String getAssignedToFullName() {
        return assignedToFullName;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public FaultSeverity getSeverity() {
        return severity;
    }

    public FaultStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getResolvedAt() {
        return resolvedAt;
    }

    public String getExternalRef() {
        return externalRef;
    }
}
