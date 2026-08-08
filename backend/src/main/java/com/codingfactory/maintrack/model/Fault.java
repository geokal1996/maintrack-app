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

    // Exoteriki anafora (p.x. arithmos gnostopoiisis apo SAP). Xrisimopoieitai
    // apo to import gia na MIN ksanadimiourgithei i idia vlavi an anevasoume
    // to idio arxeio dio fores. Menei null gia vlaves pou ftiaxnontai xeirokinita.
    //
    // PROSOXI: DEN bazoume "unique = true" epitides. Sto SQL Server ena UNIQUE
    // constraint theorei ola ta NULL isa metaxi tous, opote tha epetrepe MONO MIA
    // vlavi me keno externalRef - kai oles oi yparxouses vlaves exoun akrivos auto.
    // O elegxos monadikotitas ginetai sto FaultImportService (findByExternalRef).
    @Column(name = "external_ref")
    private String externalRef;

    public Fault() {
    }

    // Otan dimiourgoume mia kainouria vlavi, tin bazoume automata se OPEN
    // kai kratame tin ora pou dimiourgithike.
    @PrePersist
    protected void onCreate() {
        // PROSOXI: mono AN den exei idi oristei. Sto import apo Excel theloume na
        // kratisoume tin PRAGMATIKI imerominia tis vlavis, oxi tin ora tou anevasmatos -
        // alliws ena olokliro istoriko tha emfanizotan san na symvike simera.
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
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

    public String getExternalRef() {
        return externalRef;
    }

    public void setExternalRef(String externalRef) {
        this.externalRef = externalRef;
    }
}
