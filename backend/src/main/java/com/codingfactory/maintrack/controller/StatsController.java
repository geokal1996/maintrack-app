package com.codingfactory.maintrack.controller;

import com.codingfactory.maintrack.dto.ParetoDashboardResponse;
import com.codingfactory.maintrack.dto.ReliabilityResponse;
import com.codingfactory.maintrack.dto.TrendPointResponse;
import com.codingfactory.maintrack.service.StatsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@Tag(name = "Stats", description = "Στατιστικά συντήρησης: Pareto, MTBF/MTTR, τάση στον χρόνο")
@RestController
@RequestMapping("/api/stats")
public class StatsController {

    private final StatsService statsService;

    public StatsController(StatsService statsService) {
        this.statsService = statsService;
    }

    // Den vazoume periorismo rolou edo - opoios einai syndedemenos (opoiosdipote
    // rolos) mporei na dei ta statistika, akrivos opos vlepei kai to Dashboard.
    // O geniko kanonas sto SecurityConfig (.anyRequest().authenticated()) arkei.
    @Operation(summary = "Τα τρία διαγράμματα Pareto",
            description = "Υπολογίζονται ζωντανά από τη βάση: χρόνος διακοπής ανά μηχανή, "
                    + "αριθμός βλαβών ανά μηχανή, βλάβες ανά σοβαρότητα. "
                    + "Τα φίλτρα είναι προαιρετικά — χωρίς αυτά επιστρέφονται όλα τα δεδομένα. "
                    + "Το φίλτρο περιόδου εφαρμόζεται στην ημερομηνία της ΒΛΑΒΗΣ, όχι της επισκευής.")
    @GetMapping("/pareto")
    public ParetoDashboardResponse getPareto(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) String area) {
        return statsService.getParetoDashboard(from, to, area);
    }

    @Operation(summary = "Δείκτες αξιοπιστίας: MTBF, MTTR, διαθεσιμότητα",
            description = "MTBF = μέσος χρόνος μεταξύ βλαβών (όσο μεγαλύτερος τόσο καλύτερα). "
                    + "MTTR = μέσος χρόνος επισκευής (όσο μικρότερος τόσο καλύτερα). "
                    + "Διαθεσιμότητα = MTBF / (MTBF + MTTR). "
                    + "Στον MTTR μετρώνται μόνο οι βλάβες με καταγεγραμμένη διάρκεια.")
    @GetMapping("/reliability")
    public ReliabilityResponse getReliability(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) String area,
            @RequestParam(required = false) Long machineId) {
        return statsService.getReliability(from, to, area, machineId);
    }

    @Operation(summary = "Τάση βλαβών και χρόνου διακοπής ανά μήνα",
            description = "Δείχνει αν η κατάσταση βελτιώνεται ή χειροτερεύει με τον χρόνο — "
                    + "κάτι που τα διαγράμματα Pareto, ως στατική εικόνα, δεν μπορούν να δείξουν.")
    @GetMapping("/trend")
    public List<TrendPointResponse> getTrend(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) String area,
            @RequestParam(required = false) Long machineId) {
        return statsService.getTrend(from, to, area, machineId);
    }
}
