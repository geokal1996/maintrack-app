package com.codingfactory.maintrack.service;

import com.codingfactory.maintrack.dto.ParetoDashboardResponse;
import com.codingfactory.maintrack.dto.ParetoItemResponse;
import com.codingfactory.maintrack.model.Fault;
import com.codingfactory.maintrack.model.MaintenanceAction;
import com.codingfactory.maintrack.repository.FaultRepository;
import com.codingfactory.maintrack.repository.MaintenanceActionRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

// Ypologizei "live" (apo ti trexousa katastasi tis vasis) ta idia 3 Pareto
// pou exoume ftiaxei sto Power BI - etsi to frontend mporei na ta deixnei
// ana pasa stigmi, xoris na xreiazetai na anoixoume xoristo ergaleio.
@Service
public class StatsService {

    private final FaultRepository faultRepository;
    private final MaintenanceActionRepository maintenanceActionRepository;

    public StatsService(FaultRepository faultRepository, MaintenanceActionRepository maintenanceActionRepository) {
        this.faultRepository = faultRepository;
        this.maintenanceActionRepository = maintenanceActionRepository;
    }

    public ParetoDashboardResponse getParetoDashboard() {
        return new ParetoDashboardResponse(
                downtimeByMachine(),
                faultsByMachine(),
                faultsBySeverity()
        );
    }

    // Pareto 1: synoliko downtime (lepta) ana mihani
    private List<ParetoItemResponse> downtimeByMachine() {
        Map<String, Long> totals = new LinkedHashMap<>();
        for (MaintenanceAction action : maintenanceActionRepository.findAll()) {
            String machineLabel = action.getFault().getMachine().getCode();
            long minutes = action.getDowntimeMinutes() != null ? action.getDowntimeMinutes() : 0L;
            totals.merge(machineLabel, minutes, Long::sum);
        }
        return toPareto(totals);
    }

    // Pareto 2: arithmos vlavon ana mihani
    private List<ParetoItemResponse> faultsByMachine() {
        Map<String, Long> totals = new LinkedHashMap<>();
        for (Fault fault : faultRepository.findAll()) {
            String machineLabel = fault.getMachine().getCode();
            totals.merge(machineLabel, 1L, Long::sum);
        }
        return toPareto(totals);
    }

    // Pareto 3: arithmos vlavon ana sovarotita
    private List<ParetoItemResponse> faultsBySeverity() {
        Map<String, Long> totals = new LinkedHashMap<>();
        for (Fault fault : faultRepository.findAll()) {
            totals.merge(fault.getSeverity().name(), 1L, Long::sum);
        }
        return toPareto(totals);
    }

    // Koino "bima" gia ola ta Pareto: taxinomisi fthinousa (pio megalo protta),
    // kai meta athroistiko pososto. I deyterevousa taxinomisi (alfavitika sto label)
    // "spaei" tis isopalies - etsi apofevgoume to provlima pou eixame sto Power BI
    // (DAX RANKX/TOPN me Dense ranking) me isopales times.
    private List<ParetoItemResponse> toPareto(Map<String, Long> totals) {
        List<Map.Entry<String, Long>> sorted = totals.entrySet().stream()
                .sorted(Comparator.<Map.Entry<String, Long>>comparingLong(e -> -e.getValue())
                        .thenComparing(Map.Entry::getKey))
                .collect(Collectors.toList());

        long grandTotal = sorted.stream().mapToLong(Map.Entry::getValue).sum();

        List<ParetoItemResponse> result = new ArrayList<>();
        long running = 0;
        for (Map.Entry<String, Long> entry : sorted) {
            running += entry.getValue();
            double cumulativePercent = grandTotal == 0 ? 0 : (running * 100.0) / grandTotal;
            result.add(new ParetoItemResponse(entry.getKey(), entry.getValue(), round1(cumulativePercent)));
        }
        return result;
    }

    private double round1(double value) {
        return Math.round(value * 10) / 10.0;
    }
}
