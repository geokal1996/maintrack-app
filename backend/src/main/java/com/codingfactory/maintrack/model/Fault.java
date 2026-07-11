package com.codingfactory.maintrack.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "faults")
public class Fault {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Polles vlaves anikoun se MIA mihani -> @ManyToOne
    @ManyToOne(optional = false)
    @JoinColumn(name = "machine_id", nullable = false)
    private Machine machine;

    // Poios anefere ti vlavi (enas apo tous xristes)
    @ManyToOne(optional = false)
    @JoinColumn(name = "reported_by_id", nullable = false)
    private User reportedBy;

    @Column(nullable = false)
    private String title;

    @Column(length = 2000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FaultSeverity severity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FaultStatus status;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime resolvedAt;

    public Fault() {
    }

    // Otan dimiourgoume mia kainouria vlavi, tin bazoume automata se OPEN
    // kai kratame tin ora pou dimiourgithike.
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.status == null) {
            this.status = FaultStatus.OPEN;
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Machine getMachine() {
        return machine;
    }

    public void setMachine(Machine machine) {
        this.machine = machine;
    }

    public User getReportedBy() {
        return reportedBy;
    }

    public void setReportedBy(User reportedBy) {
        this.reportedBy = reportedBy;
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

    public FaultStatus getStatus() {
        return status;
    }

    public void setStatus(FaultStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getResolvedAt() {
        return resolvedAt;
    }

    public void setResolvedAt(LocalDateTime resolvedAt) {
        this.resolvedAt = resolvedAt;
    }
}
