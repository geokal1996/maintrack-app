package com.codingfactory.maintrack.controller;

import com.codingfactory.maintrack.dto.ParetoDashboardResponse;
import com.codingfactory.maintrack.service.StatsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@Tag(name = "Stats", description = "Statistika kai analytics (Pareto diagrammata)")
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
}
