package com.codingfactory.maintrack.dto;

// Ena simeio sto grafima taseis: enas minas me ta noumera tou.
// Etsi fainetai an i katastasi veltionetai i xeiroterevei me ton kairo -
// kati pou ta Pareto (pou deixnoun mia statiki eikona) den mporoun na deixoun.
public class TrendPointResponse {

    // "2026-03" - to xrisimopoiei to frontend gia taxinomisi
    private String period;
    // "Μάρ 2026" - auto pou vlepei o xristis
    private String label;
    private long faultCount;
    private long downtimeMinutes;

    public TrendPointResponse() {
    }

    public TrendPointResponse(String period, String label, long faultCount, long downtimeMinutes) {
        this.period = period;
        this.label = label;
        this.faultCount = faultCount;
        this.downtimeMinutes = downtimeMinutes;
    }

    public String getPeriod() {
        return period;
    }

    public void setPeriod(String period) {
        this.period = period;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public long getFaultCount() {
        return faultCount;
    }

    public void setFaultCount(long faultCount) {
        this.faultCount = faultCount;
    }

    public long getDowntimeMinutes() {
        return downtimeMinutes;
    }

    public void setDowntimeMinutes(long downtimeMinutes) {
        this.downtimeMinutes = downtimeMinutes;
    }
}
