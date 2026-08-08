package com.codingfactory.maintrack.dto;

// Oi dyo klasikoi deiktes axiopistias sti sintirisi.
//
// MTBF (Mean Time Between Failures) - Mesos Xronos Metaxi Vlavon
//   Poso kairo "antexei" i mihani anamesa se dyo vlaves. OSO PIO MEGALOS, TOSO KALYTERA.
//   Ypologismos: o synolikos xronos parakolouthisis dia ton arithmo ton vlavon.
//
// MTTR (Mean Time To Repair) - Mesos Xronos Episkevis
//   Poso kairo pairnei na diorthothei mia vlavi. OSO PIO MIKROS, TOSO KALYTERA.
//   Ypologismos: o synolikos xronos diakopis dia ton arithmo ton vlavon me katagegrammeni diarkeia.
//
// Diathesimotita (Availability) = MTBF / (MTBF + MTTR)
//   To pososto tou xronou pou i mihani itan diathesimi gia paragogi.
public class ReliabilityResponse {

    // Ores kata meso oro anamesa se dyo vlaves
    private Double mtbfHours;
    // Ores kata meso oro gia mia episkevi
    private Double mttrHours;
    // Pososto diathesimotitas (0-100)
    private Double availabilityPercent;

    private int totalFaults;
    private long totalDowntimeMinutes;
    // Poses meres kalyptei i periodos pou ypologisame
    private long periodDays;

    public Double getMtbfHours() {
        return mtbfHours;
    }

    public void setMtbfHours(Double mtbfHours) {
        this.mtbfHours = mtbfHours;
    }

    public Double getMttrHours() {
        return mttrHours;
    }

    public void setMttrHours(Double mttrHours) {
        this.mttrHours = mttrHours;
    }

    public Double getAvailabilityPercent() {
        return availabilityPercent;
    }

    public void setAvailabilityPercent(Double availabilityPercent) {
        this.availabilityPercent = availabilityPercent;
    }

    public int getTotalFaults() {
        return totalFaults;
    }

    public void setTotalFaults(int totalFaults) {
        this.totalFaults = totalFaults;
    }

    public long getTotalDowntimeMinutes() {
        return totalDowntimeMinutes;
    }

    public void setTotalDowntimeMinutes(long totalDowntimeMinutes) {
        this.totalDowntimeMinutes = totalDowntimeMinutes;
    }

    public long getPeriodDays() {
        return periodDays;
    }

    public void setPeriodDays(long periodDays) {
        this.periodDays = periodDays;
    }
}
