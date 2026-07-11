package com.codingfactory.maintrack.controller;

import com.codingfactory.maintrack.dto.*;
import com.codingfactory.maintrack.model.FaultStatus;
import com.codingfactory.maintrack.service.FaultService;
import com.codingfactory.maintrack.service.MaintenanceActionService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Faults", description = "Katagrafi kai diaxeirisi vlavon")
@RestController
@RequestMapping("/api/faults")
public class FaultController {

    private final FaultService faultService;
    private final MaintenanceActionService maintenanceActionService;

    public FaultController(FaultService faultService, MaintenanceActionService maintenanceActionService) {
        this.faultService = faultService;
        this.maintenanceActionService = maintenanceActionService;
    }

    // p.x. GET /api/faults?status=OPEN  i  GET /api/faults?machineId=3
    @GetMapping
    public List<FaultResponse> getAll(@RequestParam(required = false) FaultStatus status,
                                       @RequestParam(required = false) Long machineId) {
        return faultService.getAll(status, machineId);
    }

    @GetMapping("/{id}")
    public FaultResponse getById(@PathVariable Long id) {
        return faultService.getById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public FaultResponse create(@Valid @RequestBody FaultCreateRequest request) {
        return faultService.create(request);
    }

    @PatchMapping("/{id}/status")
    public FaultResponse updateStatus(@PathVariable Long id, @Valid @RequestBody FaultStatusUpdateRequest request) {
        return faultService.updateStatus(id, request.getStatus());
    }

    @GetMapping("/{id}/actions")
    public List<MaintenanceActionResponse> getActions(@PathVariable Long id) {
        return maintenanceActionService.getForFault(id);
    }

    @PostMapping("/{id}/actions")
    @ResponseStatus(HttpStatus.CREATED)
    public MaintenanceActionResponse addAction(@PathVariable Long id, @Valid @RequestBody MaintenanceActionRequest request) {
        return maintenanceActionService.create(id, request);
    }
}
