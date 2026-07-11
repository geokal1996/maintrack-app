package com.codingfactory.maintrack.exception;

import java.time.LocalDateTime;

// Auto einai to "sxima" tou minimatos lathous pou tha blepei o client
// p.x. {"timestamp": "...", "status": 404, "message": "Machine not found with id 5"}
public class ApiError {

    private LocalDateTime timestamp;
    private int status;
    private String message;

    public ApiError(int status, String message) {
        this.timestamp = LocalDateTime.now();
        this.status = status;
        this.message = message;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public int getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }
}
