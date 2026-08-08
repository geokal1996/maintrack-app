package com.codingfactory.maintrack.service;

import com.codingfactory.maintrack.dto.FaultCreateRequest;
import com.codingfactory.maintrack.dto.FaultResponse;
import com.codingfactory.maintrack.dto.FaultUpdateRequest;
import com.codingfactory.maintrack.dto.PageResponse;
import com.codingfactory.maintrack.exception.ResourceNotFoundException;
import com.codingfactory.maintrack.model.*;
import com.codingfactory.maintrack.repository.FaultRepository;
import com.codingfactory.maintrack.repository.MaintenanceActionRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class FaultService {

    private final FaultRepository faultRepository;
    private final MachineService machineService;
    private final UserService userService;
    private final MaintenanceActionRepository actionRepository;

    public FaultService(FaultRepository faultRepository, MachineService machineService,
                         UserService userService, MaintenanceActionRepository actionRepository) {
        this.faultRepository = faultRepository;
        this.machineService = machineService;
        this.userService = userService;
        this.actionRepository = actionRepository;
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

    // Selidopoiimeni anazitisi. Ola ta kritiria einai proairetika.
    // I taxinomisi einai stathera "oi pio prosfates prota" - auto theli o xristis
    // otan anoigei ti lista vlavon.
    public PageResponse<FaultResponse> search(FaultStatus status, Long machineId, String q,
                                               int page, int size) {
        // Prostasia: kaneis den prepei na mporei na zitisei 100.000 grammes me ena request
        int safeSize = Math.min(Math.max(size, 1), 200);
        String query = (q == null || q.isBlank()) ? null : q.trim();

        Page<Fault> result = faultRepository.search(
                status, machineId, query,
                PageRequest.of(Math.max(page, 0), safeSize, Sort.by(Sort.Direction.DESC, "createdAt"))
        );
        return PageResponse.from(result, FaultResponse::from);
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

    // Diorthosi mias yparxousas vlavis. I KATASTASI den allazei apo edo -
    // exei diko tis endpoint me tous kanones roís.
    public FaultResponse update(Long id, FaultUpdateRequest request) {
        Fault fault = findEntityById(id);
        Machine previousMachine = fault.getMachine();
        Machine newMachine = machineService.findEntityById(request.getMachineId());

        fault.setMachine(newMachine);
        fault.setTitle(request.getTitle());
        fault.setDescription(request.getDescription());
        fault.setSeverity(request.getSeverity());

        Fault saved = faultRepository.save(fault);

        // I sovarotita i i mihani mporei na allaxan, opote ksanaypologizoume tin
        // katastasi KAI tis dyo mihanon (tis palias kai tis neas).
        recalculateMachineStatus(newMachine);
        if (!previousMachine.getId().equals(newMachine.getId())) {
            recalculateMachineStatus(previousMachine);
        }

        return FaultResponse.from(saved);
    }

    // Diagrafi vlavis pou katachorithike KATA LATHOS.
    // Svinei prota tis energeies sintirisis tis (alliws to foreign key den to epitrepei).
    @Transactional
    public void delete(Long id) {
        Fault fault = findEntityById(id);
        Machine machine = fault.getMachine();

        actionRepository.deleteAll(actionRepository.findByFaultId(id));
        faultRepository.delete(fault);

        recalculateMachineStatus(machine);
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
            recalculateMachineStatus(fault.getMachine());
        }

        return FaultResponse.from(saved);
    }

    // Ksanaypologizei tin katastasi tis mihanis apo tis vlaves tis. Idios kanonas
    // pantou stin efarmogi (DataSeeder, import, edo): DOWN an yparxei anoixti sovari
    // vlavi, UNDER_MAINTENANCE an yparxei opoiadipote alli anoixti, alliws OPERATIONAL.
    private void recalculateMachineStatus(Machine machine) {
        List<Fault> machineFaults = faultRepository.findByMachineId(machine.getId());

        boolean hasSeriousOpen = machineFaults.stream().anyMatch(f ->
                (f.getStatus() == FaultStatus.OPEN || f.getStatus() == FaultStatus.IN_PROGRESS)
                        && (f.getSeverity() == FaultSeverity.HIGH || f.getSeverity() == FaultSeverity.CRITICAL));
        boolean hasAnyOpen = machineFaults.stream().anyMatch(f ->
                f.getStatus() == FaultStatus.OPEN || f.getStatus() == FaultStatus.IN_PROGRESS);

        MachineStatus newStatus = hasSeriousOpen ? MachineStatus.DOWN
                : hasAnyOpen ? MachineStatus.UNDER_MAINTENANCE
                : MachineStatus.OPERATIONAL;

        if (machine.getStatus() != newStatus) {
            machine.setStatus(newStatus);
            machineService.save(machine);
        }
    }

    public Fault findEntityById(Long id) {
        return faultRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Fault not found with id " + id));
    }
}
