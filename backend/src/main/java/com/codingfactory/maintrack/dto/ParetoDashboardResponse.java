package com.codingfactory.maintrack.dto;

import java.util.List;

// Ta 3 Pareto pou eixame ftiaxei sto Power BI, tora ypologismena "live" apo ti vasi.
public class ParetoDashboardResponse {

    private List<ParetoItemResponse> downtimeByMachine;
    private List<ParetoItemResponse> faultsByMachine;
    private List<ParetoItemResponse> faultsBySeverity;

    public ParetoDashboardResponse() {
    }

    public ParetoDashboardResponse(List<ParetoItemResponse> downtimeByMachine,
                                    List<ParetoItemResponse> faultsByMachine,
                                    List<ParetoItemResponse> faultsBySeverity) {
        this.downtimeByMachine = downtimeByMachine;
        this.faultsByMachine = faultsByMachine;
        this.faultsBySeverity = faultsBySeverity;
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
