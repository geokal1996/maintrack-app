package com.codingfactory.maintrack.dto;

import com.codingfactory.maintrack.model.FaultSeverity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class FaultCreateRequest {

    @NotNull(message = "To machineId einai ipoxreotiko")
    private Long machineId;

    @NotNull(message = "To reportedByUserId einai ipoxreotiko")
    private Long reportedByUserId;

    @NotBlank(message = "O titlos einai ipoxreotikos")
    private String title;

    private String description;

    @NotNull(message = "I sovarotita einai ipoxreotiki")
    private FaultSeverity severity;

    public Long getMachineId() {
        return machineId;
    }

    public void setMachineId(Long machineId) {
        this.machineId = machineId;
    }

    public Long getReportedByUserId() {
        return reportedByUserId;
    }

    public void setReportedByUserId(Long reportedByUserId) {
        this.reportedByUserId = reportedByUserId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public FaultSeverity getSeverity() {
        return severity;
    }

    public void setSeverity(FaultSeverity severity) {
        this.severity = severity;
    }
}
