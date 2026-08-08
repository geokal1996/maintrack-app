package com.codingfactory.maintrack.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

// Katagrafei KATHE allagi katastasis mias vlavis: apo pou, pou, apo poion kai pote.
//
// Giati xreiazetai: xoris auto, an mia vlavi einai "Ekleise", den kseroume pote
// kleise oute poios tin ekleise. Se ena systima sintirisis auto einai vasiko -
// otan kapoios rotisei "giati emeine anoixti 3 vdomades;", prepei na yparxei apantisi.
@Entity
@Table(name = "fault_status_changes")
public class FaultStatusChange {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "fault_id", nullable = false)
    private Fault fault;

    // Null gia tin proti katagrafi (dimiourgia tis vlavis)
    @Enumerated(EnumType.STRING)
    private FaultStatus fromStatus;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FaultStatus toStatus;

    @ManyToOne
    @JoinColumn(name = "changed_by_id")
    private User changedBy;

    @Column(nullable = false)
    private LocalDateTime changedAt;

    public FaultStatusChange() {
    }

    public FaultStatusChange(Fault fault, FaultStatus fromStatus, FaultStatus toStatus, User changedBy) {
        this.fault = fault;
        this.fromStatus = fromStatus;
        this.toStatus = toStatus;
        this.changedBy = changedBy;
    }

    @PrePersist
    protected void onCreate() {
        if (this.changedAt == null) {
            this.changedAt = LocalDateTime.now();
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Fault getFault() {
        return fault;
    }

    public void setFault(Fault fault) {
        this.fault = fault;
    }

    public FaultStatus getFromStatus() {
        return fromStatus;
    }

    public void setFromStatus(FaultStatus fromStatus) {
        this.fromStatus = fromStatus;
    }

    public FaultStatus getToStatus() {
        return toStatus;
    }

    public void setToStatus(FaultStatus toStatus) {
        this.toStatus = toStatus;
    }

    public User getChangedBy() {
        return changedBy;
    }

    public void setChangedBy(User changedBy) {
        this.changedBy = changedBy;
    }

    public LocalDateTime getChangedAt() {
        return changedAt;
    }

    public void setChangedAt(LocalDateTime changedAt) {
        this.changedAt = changedAt;
    }
}
