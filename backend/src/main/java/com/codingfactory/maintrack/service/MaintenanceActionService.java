package com.codingfactory.maintrack.service;

import com.codingfactory.maintrack.dto.MaintenanceActionRequest;
import com.codingfactory.maintrack.dto.MaintenanceActionResponse;
import com.codingfactory.maintrack.model.Fault;
import com.codingfactory.maintrack.model.FaultStatus;
import com.codingfactory.maintrack.model.MaintenanceAction;
import com.codingfactory.maintrack.model.User;
import com.codingfactory.maintrack.repository.FaultRepository;
import com.codingfactory.maintrack.repository.MaintenanceActionRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MaintenanceActionService {

    private final MaintenanceActionRepository actionRepository;
    private final FaultRepository faultRepository;
    private final FaultService faultService;
    private final UserService userService;

    public MaintenanceActionService(MaintenanceActionRepository actionRepository,
                                     FaultRepository faultRepository,
                                     FaultService faultService,
                                     UserService userService) {
        this.actionRepository = actionRepository;
        this.faultRepository = faultRepository;
        this.faultService = faultService;
        this.userService = userService;
    }

    public List<MaintenanceActionResponse> getForFault(Long faultId) {
        return actionRepository.findByFaultId(faultId)
                .stream()
                .map(MaintenanceActionResponse::from)
                .toList();
    }

    public MaintenanceActionResponse create(Long faultId, MaintenanceActionRequest request) {
        Fault fault = faultService.findEntityById(faultId);
        User technician = userService.findEntityById(request.getTechnicianUserId());

        MaintenanceAction action = new MaintenanceAction();
        action.setFault(fault);
        action.setTechnician(technician);
        action.setDescription(request.getDescription());
        action.setDowntimeMinutes(request.getDowntimeMinutes());

        MaintenanceAction saved = actionRepository.save(action);

        // Business logic: an i vlavi itan akoma OPEN, i proti energeia sintirisis
        // tin pernaei automata se IN_PROGRESS.
        if (fault.getStatus() == FaultStatus.OPEN) {
            fault.setStatus(FaultStatus.IN_PROGRESS);
            faultRepository.save(fault);
        }

        return MaintenanceActionResponse.from(saved);
    }
}
