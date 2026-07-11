package com.codingfactory.maintrack.service;

import com.codingfactory.maintrack.dto.FaultCreateRequest;
import com.codingfactory.maintrack.dto.FaultResponse;
import com.codingfactory.maintrack.exception.ResourceNotFoundException;
import com.codingfactory.maintrack.model.*;
import com.codingfactory.maintrack.repository.FaultRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class FaultService {

    private final FaultRepository faultRepository;
    private final MachineService machineService;
    private final UserService userService;

    public FaultService(FaultRepository faultRepository, MachineService machineService, UserService userService) {
        this.faultRepository = faultRepository;
        this.machineService = machineService;
        this.userService = userService;
    }

    public List<FaultResponse> getAll(FaultStatus status, Long machineId) {
        List<Fault> faults;
        if (status != null) {
            faults = faultRepository.findByStatus(status);
        } else if (machineId != null) {
            faults = faultRepository.findByMachineId(machineId);
        } else {
            faults = faultRepository.findAll();
        }
        return faults.stream().map(FaultResponse::from).toList();
    }

    public FaultResponse getById(Long id) {
        return FaultResponse.from(findEntityById(id));
    }

    public FaultResponse create(FaultCreateRequest request) {
        // Prin ftiaksoume ti vlavi, epivevaionoume oti i mihani kai o xristis pou tin anefere YPARXOUN.
        // An den yparxoun, to findEntityById tha petaxei ResourceNotFoundException automata.
        Machine machine = machineService.findEntityById(request.getMachineId());
        User reportedBy = userService.findEntityById(request.getReportedByUserId());

        Fault fault = new Fault();
        fault.setMachine(machine);
        fault.setReportedBy(reportedBy);
        fault.setTitle(request.getTitle());
        fault.setDescription(request.getDescription());
        fault.setSeverity(request.getSeverity());
        fault.setStatus(FaultStatus.OPEN);

        Fault saved = faultRepository.save(fault);

        // Business logic: mia sovari vlavi (HIGH i CRITICAL) bazei automata ti mihani DOWN.
        if (request.getSeverity() == FaultSeverity.HIGH || request.getSeverity() == FaultSeverity.CRITICAL) {
            machine.setStatus(MachineStatus.DOWN);
            machineService.save(machine);
        } else if (machine.getStatus() == MachineStatus.OPERATIONAL) {
            machine.setStatus(MachineStatus.UNDER_MAINTENANCE);
            machineService.save(machine);
        }

        return FaultResponse.from(saved);
    }

    public FaultResponse updateStatus(Long id, FaultStatus newStatus) {
        Fault fault = findEntityById(id);

        // Business logic: den mporeis na kleiseis mia vlavi an den exei proto ginei RESOLVED.
        if (newStatus == FaultStatus.CLOSED && fault.getStatus() != FaultStatus.RESOLVED) {
            throw new IllegalStateException("I vlavi prepei na ginei RESOLVED prin kleisei (CLOSED)");
        }

        fault.setStatus(newStatus);

        if (newStatus == FaultStatus.RESOLVED) {
            fault.setResolvedAt(LocalDateTime.now());
        }

        Fault saved = faultRepository.save(fault);

        // Otan mia vlavi kleinei i lyetai, elegxoume an i mihani exei alles anoixtes vlaves.
        // An den exei kammia, tin bazoume xana OPERATIONAL.
        if (newStatus == FaultStatus.RESOLVED || newStatus == FaultStatus.CLOSED) {
            updateMachineStatusIfNoOpenFaults(fault.getMachine());
        }

        return FaultResponse.from(saved);
    }

    private void updateMachineStatusIfNoOpenFaults(Machine machine) {
        List<Fault> machineFaults = faultRepository.findByMachineId(machine.getId());
        boolean hasOpenFaults = machineFaults.stream()
                .anyMatch(f -> f.getStatus() == FaultStatus.OPEN || f.getStatus() == FaultStatus.IN_PROGRESS);

        if (!hasOpenFaults) {
            machine.setStatus(MachineStatus.OPERATIONAL);
            machineService.save(machine);
        }
    }

    public Fault findEntityById(Long id) {
        return faultRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Fault not found with id " + id));
    }
}
