package com.codingfactory.maintrack.dto;

import com.codingfactory.maintrack.model.Machine;
import com.codingfactory.maintrack.model.MachineStatus;

// Auto einai to sxima pou EPISTREFOUME sto xristi. Edo tha mporousame na kryapsoume
// pedia an xreiazotan (den yparxei tetoio thema stin Machine, alla sto User p.x. yparxei - to password).
public class MachineResponse {

    private Long id;
    private String code;
    private String name;
    private String area;
    private MachineStatus status;

    // Static "factory method" - eukolos tropos na ftiaxnoume MachineResponse apo ena Machine entity
    public static MachineResponse from(Machine machine) {
        MachineResponse dto = new MachineResponse();
        dto.id = machine.getId();
        dto.code = machine.getCode();
        dto.name = machine.getName();
        dto.area = machine.getArea();
        dto.status = machine.getStatus();
        return dto;
    }

    public Long getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getArea() {
        return area;
    }

    public MachineStatus getStatus() {
        return status;
    }
}
