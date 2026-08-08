package com.codingfactory.maintrack.dto;

// Anathesi vlavis se texniko. To "userId" mporei na einai null - simainei
// "afairese tin anathesi", dld i vlavi ksanaginetai adiathetimeni.
public class AssignFaultRequest {

    private Long userId;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
}
