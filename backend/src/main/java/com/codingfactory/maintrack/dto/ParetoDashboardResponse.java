package com.codingfactory.maintrack.dto;

import java.util.List;

// Ta 3 Pareto pou eixame ftiaxei sto Power BI, tora ypologismena "live" apo ti vasi.
public class ParetoDashboardResponse {

    private List<ParetoItemResponse> downtimeByMachine;
    private List<ParetoItemResponse> faultsByMachine;
    private List<ParetoItemResponse> faultsBySeverity;

    // Poses vlaves mpikan sto ypologismo meta ta filtra - gia na kserei o xristis
    // an to deigma einai arketo i an ta filtra einai poly stena.
    private int totalFaults;

    // Oles oi perioxes pou yparxoun stis mihanes, gia na gemisei to dropdown
    private List<String> availableAreas;

    public ParetoDashboardResponse() {
    }

    public ParetoDashboardResponse(List<ParetoItemResponse> downtimeByMachine,
                                    List<ParetoItemResponse> faultsByMachine,
                                    List<ParetoItemResponse> faultsBySeverity) {
        this.downtimeByMachine = downtimeByMachine;
        this.faultsByMachine = faultsByMachine;
        this.faultsBySeverity = faultsBySeverity;
    }

    public int getTotalFaults() {
        return totalFaults;
    }

    public void setTotalFaults(int totalFaults) {
        this.totalFaults = totalFaults;
    }

    public List<String> getAvailableAreas() {
        return availableAreas;
    }

    public void setAvailableAreas(List<String> availableAreas) {
        this.availableAreas = availableAreas;
    }

    public List<ParetoItemResponse> getDowntimeByMachine() {
        return downtimeByMachine;
    }

    public void setDowntimeByMachine(List<ParetoItemResponse> downtimeByMachine) {
        this.downtimeByMachine = downtimeByMachine;
    }

    public List<ParetoItemResponse> getFaultsByMachine() {
        return faultsByMachine;
    }

    public void setFaultsByMachine(List<ParetoItemResponse> faultsByMachine) {
        this.faultsByMachine = faultsByMachine;
    }

    public List<ParetoItemResponse> getFaultsBySeverity() {
        return faultsBySeverity;
    }

    public void setFaultsBySeverity(List<ParetoItemResponse> faultsBySeverity) {
        this.faultsBySeverity = faultsBySeverity;
    }
}
