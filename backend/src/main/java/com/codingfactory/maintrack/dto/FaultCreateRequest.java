package com.codingfactory.maintrack.dto;

import com.codingfactory.maintrack.model.FaultSeverity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class FaultCreateRequest {

    @NotNull(message = "Η μηχανή είναι υποχρεωτική")
    private Long machineId;

    @NotNull(message = "Ο χρήστης που αναφέρει τη βλάβη είναι υποχρεωτικός")
    private Long reportedByUserId;

    @NotBlank(message = "Ο τίτλος είναι υποχρεωτικός")
    private String title;

    private String description;

    @NotNull(message = "Η σοβαρότητα είναι υποχρεωτική")
    private FaultSeverity severity;

    // Proairetiko: se poion texniko anatithetai amesos i vlavi.
    // An meinei keno, i vlavi menei adiathetimeni kai anatithetai argotera.
    private Long assignedToUserId;

    public Long getAssignedToUserId() {
        return assignedToUserId;
    }

    public void setAssignedToUserId(Long assignedToUserId) {
        this.assignedToUserId = assignedToUserId;
    }

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
