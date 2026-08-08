package com.codingfactory.maintrack.service;

import com.codingfactory.maintrack.dto.FaultCreateRequest;
import com.codingfactory.maintrack.dto.FaultResponse;
import com.codingfactory.maintrack.model.*;
import com.codingfactory.maintrack.repository.FaultRepository;
import com.codingfactory.maintrack.repository.FaultStatusChangeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

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
        // Ksekiname me ti mihani EKTOS LEITOURGIAS - auti einai i realistiki katastasi
        // otan mia sovari vlavi einai se ekselixi.
        machine.setStatus(MachineStatus.DOWN);

        Fault fault = new Fault();
        fault.setId(5L);
        fault.setMachine(machine);
        fault.setReportedBy(reporter);
        fault.setSeverity(FaultSeverity.CRITICAL);
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
        machine.setStatus(MachineStatus.DOWN);

        Fault faultPouKleinei = new Fault();
        faultPouKleinei.setId(5L);
        faultPouKleinei.setMachine(machine);
        faultPouKleinei.setReportedBy(reporter);
        faultPouKleinei.setSeverity(FaultSeverity.CRITICAL);
        faultPouKleinei.setStatus(FaultStatus.RESOLVED);

        Fault alliAnoixtiVlavi = new Fault();
        alliAnoixtiVlavi.setId(6L);
        alliAnoixtiVlavi.setMachine(machine);
        alliAnoixtiVlavi.setReportedBy(reporter);
        alliAnoixtiVlavi.setSeverity(FaultSeverity.MEDIUM);
        alliAnoixtiVlavi.setStatus(FaultStatus.OPEN);

        when(faultRepository.findById(5L)).thenReturn(Optional.of(faultPouKleinei));
        when(faultRepository.save(any(Fault.class))).thenAnswer(inv -> inv.getArgument(0));
        when(faultRepository.findByMachineId(1L)).thenReturn(List.of(faultPouKleinei, alliAnoixtiVlavi));

        faultService.updateStatus(5L, FaultStatus.CLOSED);

        assertThat(faultPouKleinei.getStatus()).isEqualTo(FaultStatus.CLOSED);
        // To simantiko: i mihani DEN ginetai OPERATIONAL, giati yparxei akomi
        // i "alliAnoixtiVlavi". Perhaei se "Se sintirisi" - i sovari vlavi ekleise,
        // alla kati ekremei akoma.
        assertThat(machine.getStatus()).isEqualTo(MachineStatus.UNDER_MAINTENANCE);
    }

    // ---------------------------------------------------------------
    //  Anathesi se texniko
    // ---------------------------------------------------------------

    @Test
    void oTexnikosDenMporeiNaAnatheseiVlaviSeAllon() {
        Fault fault = new Fault();
        fault.setId(5L);
        fault.setMachine(machine);
        fault.setReportedBy(reporter);
        fault.setStatus(FaultStatus.OPEN);

        when(faultRepository.findById(5L)).thenReturn(Optional.of(fault));
        when(userService.getCurrentUser()).thenReturn(reporter);          // id = 2
        when(userService.getCurrentUserRole()).thenReturn(Role.TECHNICIAN);

        // Prospathei na tin anathesei ston xristi 99 - oxi ston eauto tou (2)
        assertThrows(AccessDeniedException.class, () -> faultService.assign(5L, 99L));

        assertThat(fault.getAssignedTo()).isNull();
        verify(faultRepository, never()).save(any());
    }

    @Test
    void oTexnikosMporeiNaParaeiTiVlaviGiaTonEautoTou() {
        Fault fault = new Fault();
        fault.setId(5L);
        fault.setMachine(machine);
        fault.setReportedBy(reporter);
        fault.setStatus(FaultStatus.OPEN);

        when(faultRepository.findById(5L)).thenReturn(Optional.of(fault));
        when(faultRepository.save(any(Fault.class))).thenAnswer(inv -> inv.getArgument(0));
        when(userService.getCurrentUser()).thenReturn(reporter);
        when(userService.getCurrentUserRole()).thenReturn(Role.TECHNICIAN);
        when(userService.findEntityById(2L)).thenReturn(reporter);

        FaultResponse response = faultService.assign(5L, 2L);

        assertThat(fault.getAssignedTo()).isEqualTo(reporter);
        assertThat(response.getAssignedToUserId()).isEqualTo(2L);
    }

    @Test
    void oProistamenosMporeiNaAnatheseiSeOpoiondipoteEnergoXristi() {
        User supervisor = new User("proistamenos", "hash", "Δοκιμαστικός Προϊστάμενος", Role.SUPERVISOR);
        supervisor.setId(3L);

        Fault fault = new Fault();
        fault.setId(5L);
        fault.setMachine(machine);
        fault.setReportedBy(reporter);
        fault.setStatus(FaultStatus.OPEN);

        when(faultRepository.findById(5L)).thenReturn(Optional.of(fault));
        when(faultRepository.save(any(Fault.class))).thenAnswer(inv -> inv.getArgument(0));
        when(userService.getCurrentUser()).thenReturn(supervisor);
        when(userService.getCurrentUserRole()).thenReturn(Role.SUPERVISOR);
        when(userService.findEntityById(2L)).thenReturn(reporter);

        faultService.assign(5L, 2L);

        assertThat(fault.getAssignedTo()).isEqualTo(reporter);
    }

    @Test
    void denAnatithetaiVlaviSeAnenergoXristi() {
        User supervisor = new User("proistamenos", "hash", "Δοκιμαστικός Προϊστάμενος", Role.SUPERVISOR);
        supervisor.setId(3L);
        reporter.setActive(false);

        Fault fault = new Fault();
        fault.setId(5L);
        fault.setMachine(machine);
        fault.setReportedBy(reporter);
        fault.setStatus(FaultStatus.OPEN);

        when(faultRepository.findById(5L)).thenReturn(Optional.of(fault));
        when(userService.getCurrentUser()).thenReturn(supervisor);
        when(userService.getCurrentUserRole()).thenReturn(Role.SUPERVISOR);
        when(userService.findEntityById(2L)).thenReturn(reporter);

        assertThrows(IllegalStateException.class, () -> faultService.assign(5L, 2L));

        assertThat(fault.getAssignedTo()).isNull();
    }

    // ---------------------------------------------------------------
    //  Istoriko katastaseon
    // ---------------------------------------------------------------

    @Test
    void kathAllagiKatastasisKatagrafetaiStoIstoriko() {
        Fault fault = new Fault();
        fault.setId(5L);
        fault.setMachine(machine);
        fault.setReportedBy(reporter);
        fault.setStatus(FaultStatus.OPEN);

        when(faultRepository.findById(5L)).thenReturn(Optional.of(fault));
        when(faultRepository.save(any(Fault.class))).thenAnswer(inv -> inv.getArgument(0));

        faultService.updateStatus(5L, FaultStatus.IN_PROGRESS);

        ArgumentCaptor<FaultStatusChange> captor = ArgumentCaptor.forClass(FaultStatusChange.class);
        verify(statusChangeRepository).save(captor.capture());

        FaultStatusChange change = captor.getValue();
        assertThat(change.getFromStatus()).isEqualTo(FaultStatus.OPEN);
        assertThat(change.getToStatus()).isEqualTo(FaultStatus.IN_PROGRESS);
        assertThat(change.getFault()).isEqualTo(fault);
    }

    @Test
    void anIKatastasiDenAllakseDenGrafetaiEggrafiStoIstoriko() {
        Fault fault = new Fault();
        fault.setId(5L);
        fault.setMachine(machine);
        fault.setReportedBy(reporter);
        fault.setStatus(FaultStatus.IN_PROGRESS);

        when(faultRepository.findById(5L)).thenReturn(Optional.of(fault));
        when(faultRepository.save(any(Fault.class))).thenAnswer(inv -> inv.getArgument(0));

        // Idia katastasi me tin trexousa - to istoriko den prepei na gemisei
        // me apanoti panomoiotypes grammes.
        faultService.updateStatus(5L, FaultStatus.IN_PROGRESS);

        verify(statusChangeRepository, never()).save(any());
    }
}
