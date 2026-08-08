package com.codingfactory.maintrack.service;

import com.codingfactory.maintrack.dto.ParetoDashboardResponse;
import com.codingfactory.maintrack.dto.ParetoItemResponse;
import com.codingfactory.maintrack.dto.ReliabilityResponse;
import com.codingfactory.maintrack.dto.TrendPointResponse;
import com.codingfactory.maintrack.model.*;
import com.codingfactory.maintrack.repository.FaultRepository;
import com.codingfactory.maintrack.repository.MachineRepository;
import com.codingfactory.maintrack.repository.MaintenanceActionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

// Elenxei tous deiktes axiopistias kai ta Pareto me XEIROPIASTOUS arithmous,
// pou mporei kaneis na epalithefsei me xarti kai molyvi.
//
// Giati einai simantiko: to MTBF/MTTR einai o logos pou yparxei i efarmogi.
// An o typos einai lathos, i efarmogi vgazei noumera pou FAINONTAI sosta alla
// odigoun se lathos apofaseis sintirisis - kai kaneis den to katalavainei.
@ExtendWith(MockitoExtension.class)
class StatsServiceTest {

    @Mock
    private FaultRepository faultRepository;

    @Mock
    private MaintenanceActionRepository maintenanceActionRepository;

    @Mock
    private MachineRepository machineRepository;

    @InjectMocks
    private StatsService statsService;

    private Machine crl1;
    private Machine saw1;
    private User technician;
    private List<Fault> faults;
    private List<MaintenanceAction> actions;

    @BeforeEach
    void setUp() {
        crl1 = new Machine("CRL1", "Γραμμή Ψυχρής Έλασης 1", "Έλαση", MachineStatus.OPERATIONAL);
        crl1.setId(1L);
        saw1 = new Machine("SAW1", "Πριόνι Κοπής Ράβδων 1", "Κοπή", MachineStatus.OPERATIONAL);
        saw1.setId(2L);

        technician = new User("texnikos", "hash", "Δοκιμαστικός Τεχνικός", Role.TECHNICIAN);
        technician.setId(10L);

        faults = new ArrayList<>();
        actions = new ArrayList<>();

        // 3 vlaves stin CRL1 kai 1 stin SAW1, oles mesa stin idia periodo
        faults.add(fault(1L, crl1, FaultSeverity.CRITICAL, LocalDateTime.of(2026, 1, 2, 8, 0)));
        faults.add(fault(2L, crl1, FaultSeverity.MEDIUM, LocalDateTime.of(2026, 1, 4, 10, 0)));
        faults.add(fault(3L, crl1, FaultSeverity.MEDIUM, LocalDateTime.of(2026, 1, 6, 12, 0)));
        faults.add(fault(4L, saw1, FaultSeverity.LOW, LocalDateTime.of(2026, 1, 8, 14, 0)));

        // Xronos diakopis: 30' + 90' = 120' se DYO vlaves.
        // I trit i exei 0 lepta (den metraei ston MTTR) kai i tetarti kammia energeia.
        actions.add(action(faults.get(0), 30));
        actions.add(action(faults.get(1), 90));
        actions.add(action(faults.get(2), 0));

        lenient().when(faultRepository.findAll()).thenReturn(faults);
        lenient().when(maintenanceActionRepository.findAll()).thenReturn(actions);
        lenient().when(machineRepository.findAll()).thenReturn(List.of(crl1, saw1));
    }

    private Fault fault(Long id, Machine machine, FaultSeverity severity, LocalDateTime createdAt) {
        Fault f = new Fault();
        f.setId(id);
        f.setMachine(machine);
        f.setSeverity(severity);
        f.setStatus(FaultStatus.CLOSED);
        f.setCreatedAt(createdAt);
        f.setTitle("Δοκιμαστική βλάβη " + id);
        return f;
    }

    private MaintenanceAction action(Fault fault, int downtimeMinutes) {
        MaintenanceAction a = new MaintenanceAction();
        a.setFault(fault);
        a.setTechnician(technician);
        a.setDowntimeMinutes(downtimeMinutes);
        a.setActionDate(fault.getCreatedAt());
        return a;
    }

    // ---------------------------------------------------------------
    //  MTBF / MTTR / Diathesimotita
    // ---------------------------------------------------------------

    @Test
    void ypologizeiMtbfMttrKaiDiathesimotitaSostaGiaOrismeniPeriodo() {
        // Periodos 1-10 Ianouariou = 10 meres = 240 ores, me 4 vlaves
        ReliabilityResponse r = statsService.getReliability(
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 10), null, null);

        assertThat(r.getTotalFaults()).isEqualTo(4);
        assertThat(r.getPeriodDays()).isEqualTo(10);
        assertThat(r.getTotalDowntimeMinutes()).isEqualTo(120);

        // MTBF = 240 ores / 4 vlaves = 60 ores
        assertThat(r.getMtbfHours()).isEqualTo(60.0);
        // MTTR = 120 lepta / 60 = 2 ores, moirasmena se DYO vlaves = 1 ora
        assertThat(r.getMttrHours()).isEqualTo(1.0);
        // Diathesimotita = 60 / (60 + 1) = 98,36% -> 98,4%
        assertThat(r.getAvailabilityPercent()).isEqualTo(98.4);
    }

    @Test
    void oiVlavesMeMidenLeptaDiakopisDenMetraneStonMttr() {
        ReliabilityResponse r = statsService.getReliability(
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 10), null, null);

        // An metrousame kai ti vlavi me 0 lepta, o MTTR tha itan 120/60/3 = 0,7 ores.
        // Auto tha edine psefti eikona "grigoron episkevon".
        assertThat(r.getMttrHours()).isEqualTo(1.0).isNotEqualTo(0.7);
    }

    @Test
    void toFiltroPeriohisKratameiMonoTisMihanesTisPeriohis() {
        ReliabilityResponse r = statsService.getReliability(
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 10), "Έλαση", null);

        // Mono oi 3 vlaves tis CRL1
        assertThat(r.getTotalFaults()).isEqualTo(3);
    }

    @Test
    void toFiltroMihanisKratameiMonoTisDikesTisVlaves() {
        ReliabilityResponse r = statsService.getReliability(
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 10), null, 2L);

        assertThat(r.getTotalFaults()).isEqualTo(1);
    }

    @Test
    void vlavesEktosPeriodouDenMetrane() {
        ReliabilityResponse r = statsService.getReliability(
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 3), null, null);

        // Mono i vlavi tis 2as Ianouariou
        assertThat(r.getTotalFaults()).isEqualTo(1);
    }

    @Test
    void xorisVlavesDenYpologizontaiDeiktes() {
        when(faultRepository.findAll()).thenReturn(List.of());

        ReliabilityResponse r = statsService.getReliability(null, null, null, null);

        assertThat(r.getTotalFaults()).isZero();
        // Simantiko: null, OXI 0. To "0 ores MTBF" tha simaine "xalaei synexeia",
        // eno i alitheia einai "den exoume dedomena".
        assertThat(r.getMtbfHours()).isNull();
        assertThat(r.getMttrHours()).isNull();
        assertThat(r.getAvailabilityPercent()).isNull();
    }

    // ---------------------------------------------------------------
    //  Pareto
    // ---------------------------------------------------------------

    @Test
    void toParetoVlavonAnaMihaniEinaiTaxinomimenoKaiTaAthroistikaPosostaFtanounSto100() {
        ParetoDashboardResponse dashboard = statsService.getParetoDashboard(null, null, null);
        List<ParetoItemResponse> byMachine = dashboard.getFaultsByMachine();

        assertThat(byMachine).hasSize(2);
        // Prota i xeiroteri mihani
        assertThat(byMachine.get(0).getLabel()).isEqualTo("CRL1");
        assertThat(byMachine.get(0).getValue()).isEqualTo(3);
        assertThat(byMachine.get(0).getCumulativePercent()).isEqualTo(75.0);

        assertThat(byMachine.get(1).getLabel()).isEqualTo("SAW1");
        assertThat(byMachine.get(1).getCumulativePercent()).isEqualTo(100.0);

        assertThat(dashboard.getTotalFaults()).isEqualTo(4);
    }

    @Test
    void toParetoXronouDiakopisAgnoeiTisMihanesMeMidenLepta() {
        List<ParetoItemResponse> byDowntime =
                statsService.getParetoDashboard(null, null, null).getDowntimeByMachine();

        // Mono i CRL1 exei lepta diakopis (30 + 90 + 0). I SAW1 den emfanizetai katholou.
        assertThat(byDowntime).hasSize(1);
        assertThat(byDowntime.get(0).getLabel()).isEqualTo("CRL1");
        assertThat(byDowntime.get(0).getValue()).isEqualTo(120);
    }

    @Test
    void oiDiathesimesPeriohesErxontaiApoTisMihanesTaxinomimenes() {
        List<String> areas = statsService.getParetoDashboard(null, null, null).getAvailableAreas();

        assertThat(areas).containsExactly("Έλαση", "Κοπή");
    }

    // ---------------------------------------------------------------
    //  Tasi ana mina
    // ---------------------------------------------------------------

    @Test
    void iTasiOmadopoieiAnaMinaMeEllinikoOnomaMina() {
        List<TrendPointResponse> trend = statsService.getTrend(null, null, null, null);

        // Oles oi vlaves einai ton Ianouario 2026 -> ena simeio
        assertThat(trend).hasSize(1);
        assertThat(trend.get(0).getPeriod()).isEqualTo("2026-01");
        assertThat(trend.get(0).getLabel()).isEqualTo("Ιαν 2026");
        assertThat(trend.get(0).getFaultCount()).isEqualTo(4);
        assertThat(trend.get(0).getDowntimeMinutes()).isEqualTo(120);
    }
}
