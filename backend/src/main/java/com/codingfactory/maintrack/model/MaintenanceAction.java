package com.codingfactory.maintrack.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "maintenance_actions")
public class MaintenanceAction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Polles energeies sintirisis anikoun se MIA vlavi -> @ManyToOne
    @ManyToOne(optional = false)
    @JoinColumn(name = "fault_id", nullable = false)
    private Fault fault;

    // Poios texnikos ekane tin energeia
    @ManyToOne(optional = false)
    @JoinColumn(name = "technician_id", nullable = false)
    private User technician;

    @Column(length = 2000)
    private String description;

    @Column(nullable = false)
    private LocalDateTime actionDate;

    private Integer downtimeMinutes;

    public MaintenanceAction() {
    }

    @PrePersist
    protected void onCreate() {
        if (this.actionDate == null) {
            this.actionDate = LocalDateTime.now();
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

    public User getTechnician() {
        return technician;
    }

    public void setTechnician(User technician) {
        this.technician = technician;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getActionDate() {
        return actionDate;
    }

    public void setActionDate(LocalDateTime actionDate) {
        this.actionDate = actionDate;
    }

    public Integer getDowntimeMinutes() {
        return downtimeMinutes;
    }

    public void setDowntimeMinutes(Integer downtimeMinutes) {
        this.downtimeMinutes = downtimeMinutes;
    }
}
