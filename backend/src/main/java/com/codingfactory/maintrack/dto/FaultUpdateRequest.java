package com.codingfactory.maintrack.dto;

import com.codingfactory.maintrack.model.FaultSeverity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

// Gia diorthosi mias yparxousas vlavis (p.x. lathos titlos i lathos mihani).
// I KATASTASI den allazei apo edo - gia auti yparxei xoristo endpoint me tous
// dikous tou kanones roís (Anoixti -> Se exelixi -> Epilythike -> Ekleise).
public class FaultUpdateRequest {

    @NotNull(message = "Η μηχανή είναι υποχρεωτική")
    private Long machineId;

    @NotBlank(message = "Ο τίτλος είναι υποχρεωτικός")
    private String title;

    private String description;

    @NotNull(message = "Η σοβαρότητα είναι υποχρεωτική")
    private FaultSeverity severity;

    public Long getMachineId() {
        return machineId;
    }

    public void setMachineId(Long machineId) {
        this.machineId = machineId;
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
