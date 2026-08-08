package com.codingfactory.maintrack.service;

import com.codingfactory.maintrack.dto.ParetoDashboardResponse;
import com.codingfactory.maintrack.dto.ParetoItemResponse;
import com.codingfactory.maintrack.dto.ReliabilityResponse;
import com.codingfactory.maintrack.dto.TrendPointResponse;
import com.codingfactory.maintrack.model.Fault;
import com.codingfactory.maintrack.model.MaintenanceAction;
import com.codingfactory.maintrack.repository.FaultRepository;
import com.codingfactory.maintrack.repository.MachineRepository;
import com.codingfactory.maintrack.repository.MaintenanceActionRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

// Ypologizei "live" (apo tin trexousa katastasi tis vasis) ta 3 Pareto pou exoume
// ftiaxei kai sto Power BI - etsi to frontend mporei na ta deixnei ana pasa stigmi.
//
// Ta filtra (periodos kai perioxi) einai simantika: an exeis 3 xronia istorikou,
// to "ola" sou leei poia mihani itan i xeiroteri SYNOLIKA - alla oxi an ti
// diorthosate pernsi. Xoris filtro xronou den mporeis na deis an veltionesai.
@Service
public class StatsService {

    private final FaultRepository faultRepository;
    private final MaintenanceActionRepository maintenanceActionRepository;
    private final MachineRepository machineRepository;

    public StatsService(FaultRepository faultRepository,
                         MaintenanceActionRepository maintenanceActionRepository,
                         MachineRepository machineRepository) {
        this.faultRepository = faultRepository;
        this.maintenanceActionRepository = maintenanceActionRepository;
        this.machineRepository = machineRepository;
    }

    // ---------------------------------------------------------------
    //  Deiktes axiopistias: MTBF / MTTR / Diathesimotita
    // ---------------------------------------------------------------

    public ReliabilityResponse getReliability(LocalDate from, LocalDate to, String area, Long machineId) {
        List<Fault> faults = filteredFaults(from, to, area).stream()
                .filter(f -> machineId == null || f.getMachine().getId().equals(machineId))
                .toList();

        ReliabilityResponse response = new ReliabilityResponse();
        response.setTotalFaults(faults.size());

        Set<Long> faultIds = faults.stream().map(Fault::getId).collect(Collectors.toSet());

        // Synoliko downtime kai poses vlaves exoun ONTOS katagegrammeni diarkeia.
        // Prosoxi: den metrame tis vlaves xoris diarkeia ston MTTR - tha travousan
        // ton meso oro pros ta kato kai tha edine psefti eikona "grigoron episkevon".
        long totalDowntime = 0;
        int faultsWithDowntime = 0;
        for (MaintenanceAction action : maintenanceActionRepository.findAll()) {
            if (!faultIds.contains(action.getFault().getId())) {
                continue;
            }
            if (action.getDowntimeMinutes() != null && action.getDowntimeMinutes() > 0) {
                totalDowntime += action.getDowntimeMinutes();
                faultsWithDowntime++;
            }
        }
        response.setTotalDowntimeMinutes(totalDowntime);

        // I periodos parakolouthisis: eite auti pou zitise o xristis, eite (an den
        // orise) apo tin proti mexri tin teleftaia vlavi.
        LocalDate start = from != null ? from : earliestFaultDate(faults);
        LocalDate end = to != null ? to : LocalDate.now();
        long days = start == null ? 0 : Math.max(1, ChronoUnit.DAYS.between(start, end) + 1);
        response.setPeriodDays(days);

        if (!faults.isEmpty() && days > 0) {
            double totalHours = days * 24.0;
            // MTBF: poses ores "antexei" i mihani anamesa se dyo vlaves
            response.setMtbfHours(round1(totalHours / faults.size()));
        }
        if (faultsWithDowntime > 0) {
            // MTTR: poses ores pairnei kata meso oro mia episkevi
            response.setMttrHours(round1((totalDowntime / 60.0) / faultsWithDowntime));
        }
        if (response.getMtbfHours() != null && response.getMttrHours() != null) {
            double mtbf = response.getMtbfHours();
            double mttr = response.getMttrHours();
            response.setAvailabilityPercent(round1(100.0 * mtbf / (mtbf + mttr)));
        }

        return response;
    }

    private LocalDate earliestFaultDate(List<Fault> faults) {
        return faults.stream()
                .map(Fault::getCreatedAt)
                .filter(Objects::nonNull)
                .map(LocalDateTime::toLocalDate)
                .min(LocalDate::compareTo)
                .orElse(null);
    }

    // ---------------------------------------------------------------
    //  Tasi ston xrono: vlaves kai xronos diakopis ana mina
    // ---------------------------------------------------------------

    public List<TrendPointResponse> getTrend(LocalDate from, LocalDate to, String area, Long machineId) {
        List<Fault> faults = filteredFaults(from, to, area).stream()
                .filter(f -> machineId == null || f.getMachine().getId().equals(machineId))
                .filter(f -> f.getCreatedAt() != null)
                .toList();

        // Posa lepta diakopis antistoixoun se kathe vlavi
        Map<Long, Long> downtimePerFault = new HashMap<>();
        Set<Long> faultIds = faults.stream().map(Fault::getId).collect(Collectors.toSet());
        for (MaintenanceAction action : maintenanceActionRepository.findAll()) {
            Long faultId = action.getFault().getId();
            if (faultIds.contains(faultId) && action.getDowntimeMinutes() != null) {
                downtimePerFault.merge(faultId, action.getDowntimeMinutes().longValue(), Long::sum);
            }
        }

        // Omadopoiisi ana mina
        Map<YearMonth, long[]> byMonth = new TreeMap<>();
        for (Fault fault : faults) {
            YearMonth month = YearMonth.from(fault.getCreatedAt());
            long[] totals = byMonth.computeIfAbsent(month, m -> new long[2]);
            totals[0]++;                                                  // arithmos vlavon
            totals[1] += downtimePerFault.getOrDefault(fault.getId(), 0L); // lepta diakopis
        }

        List<TrendPointResponse> result = new ArrayList<>();
        for (Map.Entry<YearMonth, long[]> entry : byMonth.entrySet()) {
            YearMonth month = entry.getKey();
            result.add(new TrendPointResponse(
                    month.toString(),
                    GREEK_MONTHS[month.getMonthValue() - 1] + " " + month.getYear(),
                    entry.getValue()[0],
                    entry.getValue()[1]
            ));
        }
        return result;
    }

    private static final String[] GREEK_MONTHS = {
            "Ιαν", "Φεβ", "Μάρ", "Απρ", "Μάι", "Ιούν",
            "Ιούλ", "Αύγ", "Σεπ", "Οκτ", "Νοέ", "Δεκ"
    };

    // ---------------------------------------------------------------
    //  Ta tria Pareto
    // ---------------------------------------------------------------

    public ParetoDashboardResponse getParetoDashboard(LocalDate from, LocalDate to, String area) {
        List<Fault> faults = filteredFaults(from, to, area);
        Set<Long> faultIds = faults.stream().map(Fault::getId).collect(Collectors.toSet());

        ParetoDashboardResponse response = new ParetoDashboardResponse(
                downtimeByMachine(faultIds),
                faultsByMachine(faults),
                faultsBySeverity(faults)
        );
        response.setTotalFaults(faults.size());
        response.setAvailableAreas(availableAreas());
        return response;
    }

    // ---------------- Filtra ----------------

    // Ola ta filtra efarmozontai PANO STIS VLAVES. Gia ta statistika xronou diakopis,
    // kratame tis energeies pou anikoun stis filtrarismenes vlaves - dld metrame
    // "ti mas kostise oti xalase se auti tin periodo", oxi "pote egine i episkevi".
    // Alliws mia vlavi tou Dekemvriou pou episkevastike ton Ianouario tha metrouse
    // se lathos mina.
    private List<Fault> filteredFaults(LocalDate from, LocalDate to, String area) {
        return faultRepository.findAll().stream()
                .filter(f -> matchesPeriod(f, from, to))
                .filter(f -> matchesArea(f, area))
                .toList();
    }

    private boolean matchesPeriod(Fault fault, LocalDate from, LocalDate to) {
        LocalDateTime createdAt = fault.getCreatedAt();
        if (createdAt == null) {
            return true;
        }
        if (from != null && createdAt.isBefore(from.atStartOfDay())) {
            return false;
        }
        // To "to" einai symperilamvanomeno: mexri to telos tis imeras
        return to == null || !createdAt.isAfter(to.atTime(23, 59, 59));
    }

    private boolean matchesArea(Fault fault, String area) {
        if (area == null || area.isBlank()) {
            return true;
        }
        String machineArea = fault.getMachine().getArea();
        return machineArea != null && machineArea.equalsIgnoreCase(area.trim());
    }

    private List<String> availableAreas() {
        return machineRepository.findAll().stream()
                .map(m -> m.getArea())
                .filter(a -> a != null && !a.isBlank())
                .map(String::trim)
                .distinct()
                .sorted()
                .toList();
    }

    // ---------------- Ta tria Pareto ----------------

    // Pareto 1: synoliko downtime (lepta) ana mihani
    private List<ParetoItemResponse> downtimeByMachine(Set<Long> faultIds) {
        Map<String, Long> totals = new LinkedHashMap<>();
        for (MaintenanceAction action : maintenanceActionRepository.findAll()) {
            if (!faultIds.contains(action.getFault().getId())) {
                continue;
            }
            String machineLabel = action.getFault().getMachine().getCode();
            long minutes = action.getDowntimeMinutes() != null ? action.getDowntimeMinutes() : 0L;
            totals.merge(machineLabel, minutes, Long::sum);
        }
        // Mihanes me 0 lepta den exoun noima sto diagramma
        totals.values().removeIf(v -> v == 0);
        return toPareto(totals);
    }

    // Pareto 2: arithmos vlavon ana mihani
    private List<ParetoItemResponse> faultsByMachine(List<Fault> faults) {
        Map<String, Long> totals = new LinkedHashMap<>();
        for (Fault fault : faults) {
            totals.merge(fault.getMachine().getCode(), 1L, Long::sum);
        }
        return toPareto(totals);
    }

    // Pareto 3: arithmos vlavon ana sovarotita
    private List<ParetoItemResponse> faultsBySeverity(List<Fault> faults) {
        Map<String, Long> totals = new LinkedHashMap<>();
        for (Fault fault : faults) {
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
