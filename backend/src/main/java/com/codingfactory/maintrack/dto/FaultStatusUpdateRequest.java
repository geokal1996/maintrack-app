package com.codingfactory.maintrack.dto;

import com.codingfactory.maintrack.model.FaultStatus;
import jakarta.validation.constraints.NotNull;

public class FaultStatusUpdateRequest {

    @NotNull(message = "I nea katastasi einai ipoxreotiki")
    private FaultStatus status;

    public FaultStatus getStatus() {
        return status;
    }

    public void setStatus(FaultStatus status) {
        this.status = status;
    }
}
