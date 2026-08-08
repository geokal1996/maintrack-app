package com.codingfactory.maintrack.service;

import com.codingfactory.maintrack.dto.FaultCreateRequest;
import com.codingfactory.maintrack.dto.FaultResponse;
import com.codingfactory.maintrack.model.*;
import com.codingfactory.maintrack.repository.FaultRepository;
import com.codingfactory.maintrack.repository.FaultStatusChangeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

// Auto to test class elenxei tin PIO SIMANTIKI epixeirisiaki logiki tis efarmogis mas:
// tin AYTOMATI allagi katastasis mias mihanis analoga me tis vlaves tis.
//
// Xrisimopoioume Mockito gia na "prospoiithoume" ta repositories/services pou xreiazetai
// to FaultService - etsi to test trexei ASTRAPIAIA (den agizei kan tin pragmatiki vasi dedomenon)
// kai elegxei MONO ti logiki pou grapsame emeis.
@ExtendWith(MockitoExtension.class)
class FaultServiceTest {

    @Mock
    private FaultRepository faultRepository;

    @Mock
    private MachineService machineService;

    @Mock
    private UserService userService;

    // Xreiazetai gia to istoriko katastaseon. Xoris auto to mock, to FaultService
    // tha epairne null kai tha eskage me NullPointerException.
    @Mock
    private FaultStatusChangeRepository statusChangeRepository;

    @InjectMocks
    private FaultService faultService;

    private Machine machine;
    private User reporter;

    @BeforeEach
    void setUp() {
        machine = new Machine("CRL1", "Test Mihani", "Test Periohi", MachineStatus.OPERATIONAL);
        machine.setId(1L);

        reporter = new User("texnikos", "hashed-pass", "Test Texnikos", Role.TECHNICIAN);
        reporter.setId(2L);
    }

    @Test
    void otanDimiourgeitaiCriticalVlaviIMihaniPigainoiSeDown() {
        when(machineService.findEntityById(1L)).thenReturn(machine);
        when(userService.findEntityById(2L)).thenReturn(reporter);
        when(faultRepository.save(any(Fault.class))).thenAnswer(inv -> inv.getArgument(0));

        FaultCreateRequest request = new FaultCreateRequest();
        request.setMachineId(1L);
        request.setReportedByUserId(2L);
        request.setTitle("Thoryvos se roulemán");
        request.setSeverity(FaultSeverity.CRITICAL);

        FaultResponse response = faultService.create(request);

        assertThat(machine.getStatus()).isEqualTo(MachineStatus.DOWN);
        assertThat(response.getSeverity()).isEqualTo(FaultSeverity.CRITICAL);
        verify(machineService).save(machine);
    }

    @Test
    void otanDimiourgeitaiLowSeverityVlaviSeLeitourgikiMihaniPigainoiSeUnderMaintenance() {
        when(machineService.findEntityById(1L)).thenReturn(machine);
        when(userService.findEntityById(2L)).thenReturn(reporter);
        when(faultRepository.save(any(Fault.class))).thenAnswer(inv -> inv.getArgument(0));

        FaultCreateRequest request = new FaultCreateRequest();
        request.setMachineId(1L);
        request.setReportedByUserId(2L);
        request.setTitle("Mikri fthora");
        request.setSeverity(FaultSeverity.LOW);

        faultService.create(request);

        assertThat(machine.getStatus()).isEqualTo(MachineStatus.UNDER_MAINTENANCE);
        verify(machineService).save(machine);
    }

    @Test
    void denEpitrepetaiToKleisimoVlavisPrinGineiResolved() {
        Fault fault = new Fault();
        fault.setId(5L);
        fault.setMachine(machine);
        fault.setStatus(FaultStatus.OPEN);

        when(faultRepository.findById(5L)).thenReturn(Optional.of(fault));

        assertThrows(IllegalStateException.class,
                () -> faultService.updateStatus(5L, FaultStatus.CLOSED));

        // Den prepei na kanei save afou petaxtike i exception
        verify(faultRepository, never()).save(any());
    }

    @Test
    void otanIVlaviGineiResolvedIMihaniGyrizeiSeOperationalAnDenExeiAlesAnoixtesVlaves() {
        Fault fault = new Fault();
        fault.setId(5L);
        fault.setMachine(machine);
        fault.setReportedBy(reporter);
        fault.setStatus(FaultStatus.IN_PROGRESS);

        when(faultRepository.findById(5L)).thenReturn(Optional.of(fault));
        when(faultRepository.save(any(Fault.class))).thenAnswer(inv -> inv.getArgument(0));
        // Meta to resolve, i mihani DEN exei allo anoixto/in-progress vlavi
        when(faultRepository.findByMachineId(1L)).thenReturn(List.of(fault));

        faultService.updateStatus(5L, FaultStatus.RESOLVED);

        assertThat(fault.getStatus()).isEqualTo(FaultStatus.RESOLVED);
        assertThat(fault.getResolvedAt()).isNotNull();
        assertThat(machine.getStatus()).isEqualTo(MachineStatus.OPERATIONAL);
        verify(machineService).save(machine);
    }

    @Test
    void iMihaniDenGyrizeiSeOperationalAnYparxeiKiAlliAnoixtiVlavi() {
        Fault faultPouKleinei = new Fault();
        faultPouKleinei.setId(5L);
        faultPouKleinei.setMachine(machine);
        faultPouKleinei.setReportedBy(reporter);
        faultPouKleinei.setStatus(FaultStatus.RESOLVED);

        Fault alliAnoixtiVlavi = new Fault();
        alliAnoixtiVlavi.setId(6L);
        alliAnoixtiVlavi.setMachine(machine);
        alliAnoixtiVlavi.setReportedBy(reporter);
        alliAnoixtiVlavi.setStatus(FaultStatus.OPEN);

        when(faultRepository.findById(5L)).thenReturn(Optional.of(faultPouKleinei));
        when(faultRepository.save(any(Fault.class))).thenAnswer(inv -> inv.getArgument(0));
        when(faultRepository.findByMachineId(1L)).thenReturn(List.of(faultPouKleinei, alliAnoixtiVlavi));

        faultService.updateStatus(5L, FaultStatus.CLOSED);

        assertThat(faultPouKleinei.getStatus()).isEqualTo(FaultStatus.CLOSED);
        // I mihani DEN prepei na ginei OPERATIONAL - yparxei akomi i "alliAnoixtiVlavi"
        verify(machineService, never()).save(any());
    }
}
