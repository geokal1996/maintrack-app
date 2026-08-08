package com.codingfactory.maintrack.service;

import com.codingfactory.maintrack.dto.FaultCreateRequest;
import com.codingfactory.maintrack.dto.FaultResponse;
import com.codingfactory.maintrack.dto.FaultUpdateRequest;
import com.codingfactory.maintrack.dto.PageResponse;
import com.codingfactory.maintrack.dto.StatusChangeResponse;
import com.codingfactory.maintrack.exception.ResourceNotFoundException;
import com.codingfactory.maintrack.model.*;
import com.codingfactory.maintrack.repository.FaultRepository;
import com.codingfactory.maintrack.repository.FaultStatusChangeRepository;
import com.codingfactory.maintrack.repository.MaintenanceActionRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
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
    private final FaultStatusChangeRepository statusChangeRepository;

    public FaultService(FaultRepository faultRepository, MachineService machineService,
                         UserService userService, MaintenanceActionRepository actionRepository,
                         FaultStatusChangeRepository statusChangeRepository) {
        this.faultRepository = faultRepository;
        this.machineService = machineService;
        this.userService = userService;
        this.actionRepository = actionRepository;
        this.statusChangeRepository = statusChangeRepository;
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
    public PageResponse<FaultResponse> search(FaultStatus status, Long machineId, Long assignedToUserId,
                                               String q, int page, int size) {
        // Prostasia: kaneis den prepei na mporei na zitisei 100.000 grammes me ena request
        int safeSize = Math.min(Math.max(size, 1), 200);
        String query = (q == null || q.isBlank()) ? null : q.trim();

        Page<Fault> result = faultRepository.search(
                status, machineId, assignedToUserId, query,
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

        // Proairetiki anathesi idi apo ti dimiourgia
        if (request.getAssignedToUserId() != null) {
            fault.setAssignedTo(userService.findEntityById(request.getAssignedToUserId()));
        }

        Fault saved = faultRepository.save(fault);

        // Proti eggrafi sto istoriko: "dimiourgithike os ANOIXTI".
        // Xoris auti, to istoriko tha ksekinouse apo ti mesi kai den tha fainotan pote mpike i vlavi.
        recordStatusChange(saved, null, FaultStatus.OPEN);

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
        statusChangeRepository.deleteAll(statusChangeRepository.findByFaultIdOrderByChangedAtAsc(id));
        faultRepository.delete(fault);

        recalculateMachineStatus(machine);
    }

    @Transactional
    public FaultResponse updateStatus(Long id, FaultStatus newStatus) {
        Fault fault = findEntityById(id);
        FaultStatus previousStatus = fault.getStatus();

        // Business logic: den mporeis na kleiseis mia vlavi an den exei proto ginei RESOLVED.
        if (newStatus == FaultStatus.CLOSED && fault.getStatus() != FaultStatus.RESOLVED) {
            throw new IllegalStateException("Η βλάβη πρέπει πρώτα να επιλυθεί για να μπορεί να κλείσει");
        }

        fault.setStatus(newStatus);

        if (newStatus == FaultStatus.RESOLVED) {
            fault.setResolvedAt(LocalDateTime.now());
        }

        Fault saved = faultRepository.save(fault);

        // Katagrafi sto istoriko - mono an ontos allakse kati.
        // An kapoios patisei "Se ekselixi" se mia vlavi pou einai idi "Se ekselixi",
        // den theloume na gemisei to istoriko me apanoti idies grammes.
        if (previousStatus != newStatus) {
            recordStatusChange(saved, previousStatus, newStatus);
        }

        // Otan mia vlavi kleinei i lyetai, elegxoume an i mihani exei alles anoixtes vlaves.
        // An den exei kammia, tin bazoume xana OPERATIONAL.
        if (newStatus == FaultStatus.RESOLVED || newStatus == FaultStatus.CLOSED) {
            recalculateMachineStatus(fault.getMachine());
        }

        return FaultResponse.from(saved);
    }

    // ---------------------------------------------------------------
    //  Anathesi se texniko
    // ---------------------------------------------------------------

    // Kanones:
    //  - SUPERVISOR / MANAGER: anathetoun se opoiondipote energo xristi
    //  - TECHNICIAN: mporei na "parei" mia vlavi MONO gia ton eauto tou,
    //    kai na tin afisei mono an tin eixe o idios
    public FaultResponse assign(Long faultId, Long userId) {
        Fault fault = findEntityById(faultId);
        User current = userService.getCurrentUser();
        Role currentRole = userService.getCurrentUserRole();
        boolean isTechnician = currentRole == Role.TECHNICIAN;

        if (userId == null) {
            // Afairesi anathesis
            if (isTechnician && fault.getAssignedTo() != null
                    && !fault.getAssignedTo().getId().equals(current.getId())) {
                throw new AccessDeniedException("Μπορείτε να αφαιρέσετε μόνο τη δική σας ανάθεση");
            }
            fault.setAssignedTo(null);
        } else {
            if (isTechnician && !userId.equals(current.getId())) {
                throw new AccessDeniedException("Ως τεχνικός μπορείτε να αναθέσετε μια βλάβη μόνο στον εαυτό σας");
            }
            User target = userService.findEntityById(userId);
            if (!target.isActive()) {
                throw new IllegalStateException("Ο χρήστης " + target.getUsername() + " είναι ανενεργός");
            }
            fault.setAssignedTo(target);
        }

        return FaultResponse.from(faultRepository.save(fault));
    }

    public List<StatusChangeResponse> getHistory(Long faultId) {
        // Epivevaionoume oti i vlavi yparxei - alliws tha epistrefame keni lista
        // gia anyparkto id, pou einai paraplanitiko.
        findEntityById(faultId);
        return statusChangeRepository.findByFaultIdOrderByChangedAtAsc(faultId).stream()
                .map(StatusChangeResponse::from)
                .toList();
    }

    // Grafei mia grammi sto istoriko. To "poios" to pairnoume apo to security context.
    // An den yparxei syndedemenos xristis (p.x. DataSeeder sto ksekinima), grafoume
    // tin allagi me keno xristi anti na skasei i efarmogi.
    private void recordStatusChange(Fault fault, FaultStatus from, FaultStatus to) {
        User actor;
        try {
            actor = userService.getCurrentUser();
        } catch (RuntimeException ex) {
            actor = null;
        }
        statusChangeRepository.save(new FaultStatusChange(fault, from, to, actor));
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
