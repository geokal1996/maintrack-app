package com.codingfactory.maintrack.controller;

import com.codingfactory.maintrack.dto.ParetoDashboardResponse;
import com.codingfactory.maintrack.service.StatsService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Stats", description = "Statistika kai analytics (Pareto diagrammata)")
@RestController
@RequestMapping("/api/stats")
public class StatsController {

    private final StatsService statsService;

    public StatsController(StatsService statsService) {
        this.statsService = statsService;
    }

    // Den vazoume periorismo rolou edo - opoios einai syndedemenos (opoiosdipote
    // rolos) mporei na deis ta statistika, akrivos opos vlepei kai to Dashboard.
    // O geniko kanonas sto SecurityConfig (.anyRequest().authenticated()) arkei.
    @GetMapping("/pareto")
    public ParetoDashboardResponse getPareto() {
        return statsService.getParetoDashboard();
    }
}
