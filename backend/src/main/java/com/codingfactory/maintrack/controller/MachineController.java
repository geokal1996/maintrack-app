package com.codingfactory.maintrack.controller;

import com.codingfactory.maintrack.dto.MachineRequest;
import com.codingfactory.maintrack.dto.MachineResponse;
import com.codingfactory.maintrack.service.MachineService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Machines", description = "Diaxeirisi mihanon/eksoplismou")
@RestController
@RequestMapping("/api/machines")
public class MachineController {

    private final MachineService machineService;

    public MachineController(MachineService machineService) {
        this.machineService = machineService;
    }

    @GetMapping
    public List<MachineResponse> getAll() {
        return machineService.getAll();
    }

    @GetMapping("/{id}")
    public MachineResponse getById(@PathVariable Long id) {
        return machineService.getById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MachineResponse create(@Valid @RequestBody MachineRequest request) {
        return machineService.create(request);
    }

    @PutMapping("/{id}")
    public MachineResponse update(@PathVariable Long id, @Valid @RequestBody MachineRequest request) {
        return machineService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        machineService.delete(id);
    }
}
