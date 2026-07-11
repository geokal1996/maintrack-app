package com.codingfactory.maintrack.dto;

import com.codingfactory.maintrack.model.MachineStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

// Auto einai to "sxima" pou perimenoume NA MAS STEILEI o xristis otan ftiaxnei/enimeronei mia mihani.
// DEN einai idio me to Machine entity - den exei p.x. tipota parapano, mono ta pedia pou xreiazetai.
public class MachineRequest {

    @NotBlank(message = "O kodikos einai ipoxreotikos")
    private String code;

    @NotBlank(message = "To onoma einai ipoxreotiko")
    private String name;

    private String area;

    @NotNull(message = "I katastasi einai ipoxreotiki")
    private MachineStatus status;

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
