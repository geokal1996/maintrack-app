package com.codingfactory.maintrack.service;

import com.codingfactory.maintrack.dto.MaintenanceActionRequest;
import com.codingfactory.maintrack.model.*;
import com.codingfactory.maintrack.repository.FaultRepository;
import com.codingfactory.maintrack.repository.MaintenanceActionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

// Elenxei tin ayto mati metavasi mias vlavis apo OPEN se IN_PROGRESS,
// otan katachoreitai I PROTI energeia syntirisis pano tis.
@ExtendWith(MockitoExtension.class)
class MaintenanceActionServiceTest {

    @Mock
    private MaintenanceActionRepository actionRepository;

    @Mock
    private FaultRepository faultRepository;

    @Mock
    private FaultService faultService;

    @Mock
    private UserService userService;

    @InjectMocks
    private MaintenanceActionService maintenanceActionService;

    @Test
    void iProtiEnergeiaPernaeiTiVlaviApoOpenSeInProgress() {
        Fault fault = new Fault();
        fault.setId(10L);
        fault.setStatus(FaultStatus.OPEN);

        User technician = new User("automatistis", "hashed", "Test Automatistis", Role.TECHNICIAN);
        technician.setId(3L);

        when(faultService.findEntityById(10L)).thenReturn(fault);
        when(userService.findEntityById(3L)).thenReturn(technician);
        when(actionRepository.save(any(MaintenanceAction.class))).thenAnswer(inv -> inv.getArgument(0));

        MaintenanceActionRequest request = new MaintenanceActionRequest();
        request.setTechnicianUserId(3L);
        request.setDescription("Elenxos kai epanaforá");
        request.setDowntimeMinutes(20);

        maintenanceActionService.create(10L, request);

        assertThat(fault.getStatus()).isEqualTo(FaultStatus.IN_PROGRESS);
        verify(faultRepository).save(fault);
    }

    @Test
    void anIVlaviEinaiIdiInProgressDenXanaAllazeiKatastasi() {
        Fault fault = new Fault();
        fault.setId(10L);
        fault.setStatus(FaultStatus.IN_PROGRESS);

        User technician = new User("automatistis", "hashed", "Test Automatistis", Role.TECHNICIAN);
        technician.setId(3L);

        when(faultService.findEntityById(10L)).thenReturn(fault);
        when(userService.findEntityById(3L)).thenReturn(technician);
        when(actionRepository.save(any(MaintenanceAction.class))).thenAnswer(inv -> inv.getArgument(0));

        MaintenanceActionRequest request = new MaintenanceActionRequest();
        request.setTechnicianUserId(3L);
        request.setDescription("Deuteri energeia");

        maintenanceActionService.create(10L, request);

        assertThat(fault.getStatus()).isEqualTo(FaultStatus.IN_PROGRESS);
        // Afou den itan OPEN, den prepei na klithike xana to save tou fault
        verify(faultRepository, never()).save(any());
    }
}
