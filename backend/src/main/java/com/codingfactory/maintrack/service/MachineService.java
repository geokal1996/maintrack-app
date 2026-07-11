package com.codingfactory.maintrack.service;

import com.codingfactory.maintrack.dto.MachineRequest;
import com.codingfactory.maintrack.dto.MachineResponse;
import com.codingfactory.maintrack.exception.ResourceNotFoundException;
import com.codingfactory.maintrack.model.Machine;
import com.codingfactory.maintrack.repository.MachineRepository;
import org.springframework.stereotype.Service;

import java.util.List;

// @Service leei sto Spring "auti i klasi einai Service - dimiourgise ena antikeimeno
// automata kai dose to opou to xreiazetai" (Dependency Injection).
@Service
public class MachineService {

    private final MachineRepository machineRepository;

    // "Constructor injection" - to Spring perase mono tou to MachineRepository edo.
    public MachineService(MachineRepository machineRepository) {
        this.machineRepository = machineRepository;
    }

    public List<MachineResponse> getAll() {
        return machineRepository.findAll()
                .stream()
                .map(MachineResponse::from)
                .toList();
    }

    public MachineResponse getById(Long id) {
        return MachineResponse.from(findEntityById(id));
    }

    public MachineResponse create(MachineRequest request) {
        Machine machine = new Machine(request.getCode(), request.getName(), request.getArea(), request.getStatus());
        Machine saved = machineRepository.save(machine);
        return MachineResponse.from(saved);
    }

    public MachineResponse update(Long id, MachineRequest request) {
        Machine machine = findEntityById(id);
        machine.setCode(request.getCode());
        machine.setName(request.getName());
        machine.setArea(request.getArea());
        machine.setStatus(request.getStatus());
        return MachineResponse.from(machineRepository.save(machine));
    }

    public void delete(Long id) {
        Machine machine = findEntityById(id);
        machineRepository.delete(machine);
    }

    // Xrisimo kai gia alla services (p.x. to FaultService xreiazetai to idio to Machine entity, oxi to DTO)
    public Machine findEntityById(Long id) {
        return machineRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Machine not found with id " + id));
    }

    // Xrisimo otan allo service (p.x. FaultService) allazei tin katastasi mias mihanis
    // kai prepei na apothikeftei i allagi sti vasi.
    public void save(Machine machine) {
        machineRepository.save(machine);
    }
}
