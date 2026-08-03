package com.codingfactory.maintrack.dto;

// Mia "grammi" mesa se ena Pareto diagramma: p.x. { label: "CCP3", value: 12, cumulativePercent: 45.5 }
public class ParetoItemResponse {

    private String label;
    private long value;
    private double cumulativePercent;

    public ParetoItemResponse() {
    }

    public ParetoItemResponse(String label, long value, double cumulativePercent) {
        this.label = label;
        this.value = value;
        this.cumulativePercent = cumulativePercent;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public long getValue() {
        return value;
    }

    public void setValue(long value) {
        this.value = value;
    }

    public double getCumulativePercent() {
        return cumulativePercent;
    }

    public void setCumulativePercent(double cumulativePercent) {
        this.cumulativePercent = cumulativePercent;
    }
}
