package com.codingfactory.maintrack.model;

import jakarta.persistence.*;

@Entity
@Table(name = "machines")
public class Machine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // O kodikos tis mihanis, p.x. "CCP3" - prepei na einai monadikos
    @Column(nullable = false, unique = true)
    private String code;

    @Column(nullable = false)
    private String name;

    private String area;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MachineStatus status;

    public Machine() {
    }

    public Machine(String code, String name, String area, MachineStatus status) {
        this.code = code;
        this.name = name;
        this.area = area;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public MachineStatus getStatus() {
        return status;
    }

    public void setStatus(MachineStatus status) {
        this.status = status;
    }
}
