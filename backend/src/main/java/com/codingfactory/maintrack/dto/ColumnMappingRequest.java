package com.codingfactory.maintrack.dto;

import java.util.Map;

// Leei stin efarmogi POIA stili tou arxeiou tou xristi antistoixei se POIO diko mas pedio.
//
// Kathe timi einai o ARITHMOS TIS STILIS (0 = i proti stili, 1 = i defteri, k.o.k.)
// i null an o xristis den antistoixise tipota se auto to pedio.
//
// Etsi den mas endiaferei pos legontai oi stiles tou - to arxeio mporei na erthei
// apo opoiodipote systima, arkei o xristis na mas pei ti einai i kathemia.
public class ColumnMappingRequest {

    private Integer externalRef;
    private Integer machineCode;
    private Integer machineName;
    private Integer title;
    private Integer description;
    private Integer severity;
    private Integer status;
    private Integer technician;
    private Integer action;
    private Integer downtime;

    // I PRAGMATIKI imerominia tis vlavis. An dothei, i vlavi katagrafetai me AUTI
    // tin imerominia kai oxi me tin ora tou anevasmatos - alliws ena olokliro
    // istoriko tha emfanizotan san na symvike simera.
    private Integer date;

    // "MINUTES" i "HOURS" - merika systimata (opos to SAP) dinoun ores anti gia lepta
    private String downtimeUnit = "MINUTES";

    // An i mihani den yparxei sti vasi, na dimiourgeitai automata;
    private boolean createMissingMachines = true;

    // Ti apofasise o xristis gia kathe onoma mihanis pou vrethike sto arxeio:
    //   "Πρέσα 1" -> 5      (na syndethei me tin yparxousa mihani me id 5)
    //   "Αντλία 2" -> null  (na dimiourgithei nea)
    // Symplironetai apo ton pinaka epivevaiosis sto frontend.
    private Map<String, Long> machineResolutions;

    public Integer getExternalRef() {
        return externalRef;
    }

    public void setExternalRef(Integer externalRef) {
        this.externalRef = externalRef;
    }

    public Integer getMachineCode() {
        return machineCode;
    }

    public void setMachineCode(Integer machineCode) {
        this.machineCode = machineCode;
    }

    public Integer getMachineName() {
        return machineName;
    }

    public void setMachineName(Integer machineName) {
        this.machineName = machineName;
    }

    public Integer getTitle() {
        return title;
    }

    public void setTitle(Integer title) {
        this.title = title;
    }

    public Integer getDescription() {
        return description;
    }

    public void setDescription(Integer description) {
        this.description = description;
    }

    public Integer getSeverity() {
        return severity;
    }

    public void setSeverity(Integer severity) {
        this.severity = severity;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getTechnician() {
        return technician;
    }

    public void setTechnician(Integer technician) {
        this.technician = technician;
    }

    public Integer getAction() {
        return action;
    }

    public void setAction(Integer action) {
        this.action = action;
    }

    public Integer getDowntime() {
        return downtime;
    }

    public void setDowntime(Integer downtime) {
        this.downtime = downtime;
    }

    public Integer getDate() {
        return date;
    }

    public void setDate(Integer date) {
        this.date = date;
    }

    public Map<String, Long> getMachineResolutions() {
        return machineResolutions;
    }

    public void setMachineResolutions(Map<String, Long> machineResolutions) {
        this.machineResolutions = machineResolutions;
    }

    public String getDowntimeUnit() {
        return downtimeUnit;
    }

    public void setDowntimeUnit(String downtimeUnit) {
        this.downtimeUnit = downtimeUnit;
    }

    public boolean isCreateMissingMachines() {
        return createMissingMachines;
    }

    public void setCreateMissingMachines(boolean createMissingMachines) {
        this.createMissingMachines = createMissingMachines;
    }
}
