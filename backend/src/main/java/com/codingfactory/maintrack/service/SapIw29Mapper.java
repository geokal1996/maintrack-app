package com.codingfactory.maintrack.service;

import com.codingfactory.maintrack.model.FaultSeverity;
import com.codingfactory.maintrack.model.FaultStatus;
import org.apache.poi.ss.usermodel.Row;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

// Metafrazei mia grammi apo export tis SAP transaction IW29 (lista gnostopoiiseon
// syntirisis) sti diki mas domi.
//
// PROSOXI - dyskolies pou antimetopizei aftos o kodikas:
//
// 1. TO SAP EXEI DYO STILES ME TO IDIO ONOMA: dyo fores "Description" (i mia einai
//    i vlavi, i alli i leitourgiki perioxi) kai dyo fores "Priority" (kodikos kai
//    keimeno). Gi' auto psaxnoume tis stiles me VASI TI SEIRA EMFANISIS, oxi mono
//    me to onoma.
//
// 2. IMEROMINIA KAI ORA EINAI SE XORISTES STILES, me parapliritika onomata:
//    to "Malfunct.end" einai IMEROMINIA, eno to "Malfunction end" einai ORA.
//
// 3. TO "Breakdown dur." EINAI PANTA 0. Sto systima tou pelati den simeionetai o
//    deiktis vlavis, opote to SAP den ypologizei pote diarkeia. Tin ypologizoume
//    emeis apo start/end - kai an i vlavi den exei kleisei, afinoume null (OXI 0,
//    giati to 0 tha molyne to Pareto me pseftikes midenikes diarkeies).
final class SapIw29Mapper {

    private SapIw29Mapper() {
    }

    // --- Onomata stilon, kanonikopoiimena (peza, xoris kena kai teleies) ---
    private static final String NOTIFICATION = "notification";
    private static final String DESCRIPTION = "description";
    private static final String PRIORITY = "priority";
    private static final String SYSTEM_STATUS = "systemstatus";
    private static final String FLOC_AFFECTED = "flocaffected";
    private static final String EQUIPMENT = "equipment";
    private static final String EQUIPMENT_AFFECTED = "equipmtaffctd";
    private static final String MALF_END_DATE = "malfunctend";
    private static final String MALF_START_DATE = "malfunctstart";
    private static final String MALF_END_TIME = "malfunctionend";
    private static final String MALF_START_TIME = "startmalfn(t)";
    private static final String COMPLETION_DATE = "completndate";
    private static final String CREATED_BY = "createdby";
    private static final String REPORTED_BY = "reportedby";

    static String normalize(String header) {
        if (header == null) {
            return "";
        }
        return header.toLowerCase().replaceAll("[\\s.]", "");
    }

    // Anagnorizei an to arxeio einai export IW29
    static boolean matches(List<String> headers) {
        List<String> n = headers.stream().map(SapIw29Mapper::normalize).toList();
        return n.contains(NOTIFICATION)
                && (n.contains(FLOC_AFFECTED) || n.contains(EQUIPMENT_AFFECTED) || n.contains(EQUIPMENT));
    }

    // Vriskei ti thesi mias stilis. To "occurrence" einai gia tis diples stiles:
    // occurrence 0 = i proti "Description", occurrence 1 = i defteri.
    private static int col(List<String> headers, String name, int occurrence) {
        int seen = 0;
        for (int i = 0; i < headers.size(); i++) {
            if (normalize(headers.get(i)).equals(name)) {
                if (seen == occurrence) {
                    return i;
                }
                seen++;
            }
        }
        return -1;
    }

    private static int col(List<String> headers, String name) {
        return col(headers, name, 0);
    }

    static ImportRow map(Row row, List<String> headers) {
        ImportRow d = new ImportRow();

        d.externalRef = ExcelCells.str(row, col(headers, NOTIFICATION));

        // --- Katastasi: to "System status" einai syndyasmos kodikon xorismenon me kena,
        // p.x. "NOCO ORAS". Gi' auto koitame ta epimerous tokens kai oxi olo to keimeno. ---
        String statusText = ExcelCells.str(row, col(headers, SYSTEM_STATUS));
        List<String> tokens = statusText == null
                ? List.of()
                : new ArrayList<>(List.of(statusText.toUpperCase().split("\\s+")));

        if (tokens.contains("DLFL")) {
            // DLFL = Deletion Flag. I gnostopoiisi exei simadeftei gia diagrafi sto SAP.
            return ImportRow.skipped("Γνωστοποίηση με σημαία διαγραφής (DLFL)");
        }
        if (tokens.contains("NOCO")) {
            d.status = FaultStatus.CLOSED;          // Notification COmpleted
        } else if (tokens.contains("NOPR")) {
            d.status = FaultStatus.IN_PROGRESS;     // Notification in PRocess
        } else {
            d.status = FaultStatus.OPEN;            // OSNO = OutStanding NOtification
        }

        // --- Mihani: proto to functional location (p.x. "6520-HRL-TIPP"), pou einai
        // pio perigrafiko, kai an leipei pefto sto arithmo eksoplismou. ---
        String floc = ExcelCells.str(row, col(headers, FLOC_AFFECTED));
        String equipment = ExcelCells.str(row, col(headers, EQUIPMENT));
        if (equipment == null) {
            equipment = ExcelCells.str(row, col(headers, EQUIPMENT_AFFECTED));
        }
        d.machineCode = floc != null ? floc : equipment;

        // I DEFTERI stili "Description" einai i perigrafi tis leitourgikis perioxis,
        // dld to onoma tis mihanis (p.x. "ΦΟΥΡΝΟΣ Π/Θ JUNKER 4").
        d.machineName = ExcelCells.str(row, col(headers, DESCRIPTION, 1));

        // O kodikos "6520-HRL-TIPP" einai: ergostasio - perioxi - eksoplismos.
        // To mesaio kommati mas dinei tzampa tin perioxi.
        if (floc != null) {
            String[] parts = floc.split("-");
            if (parts.length >= 3) {
                d.machineArea = parts[1];
            }
        }

        // I PROTI stili "Description" einai i perigrafi tis vlavis
        d.title = ExcelCells.str(row, col(headers, DESCRIPTION, 0));

        // --- Sovarotita apo tin proteraiotita SAP ---
        String priorityCode = ExcelCells.str(row, col(headers, PRIORITY, 0));
        if (priorityCode == null) {
            priorityCode = ExcelCells.str(row, col(headers, PRIORITY, 1));
        }
        d.severity = toSeverity(priorityCode);

        // --- Poios anefere: to "Reported by" einai sxedon panta keno sto export,
        // opote pefto sto "Created By". ---
        d.technicianUsername = ExcelCells.str(row, col(headers, REPORTED_BY));
        if (d.technicianUsername == null) {
            d.technicianUsername = ExcelCells.str(row, col(headers, CREATED_BY));
        }

        // --- Xronos diakopis: ypologismos apo tis 4 stiles imerominias/oras ---
        LocalDate startDate = ExcelCells.date(row, col(headers, MALF_START_DATE));
        LocalTime startTime = ExcelCells.time(row, col(headers, MALF_START_TIME));
        LocalDate endDate = ExcelCells.date(row, col(headers, MALF_END_DATE));
        LocalTime endTime = ExcelCells.time(row, col(headers, MALF_END_TIME));

        LocalDateTime start = ExcelCells.dateTime(startDate, startTime);
        LocalDateTime end = ExcelCells.dateTime(endDate, endTime);

        if (start != null && end != null && end.isAfter(start)) {
            long minutes = Duration.between(start, end).toMinutes();
            // Ena logiko orio: pano apo 60 meres einai sxedon sigoura lathos katachorisi
            d.downtimeMinutes = minutes > 0 && minutes < 60L * 24 * 60 ? (int) minutes : null;
        }
        // An i vlavi den exei kleisei (den yparxei imerominia liksis), to downtime
        // menei null - OXI 0 - gia na min alloionei ta statistika.

        if (d.downtimeMinutes != null) {
            d.actionDescription = "Εισαγωγή από SAP (γνωστοποίηση " + d.externalRef + ")";
        }

        // --- Imerominia epilysis ---
        LocalDate completion = ExcelCells.date(row, col(headers, COMPLETION_DATE));
        if (completion != null) {
            d.resolvedAt = completion.atStartOfDay();
        } else if (end != null && d.status == FaultStatus.CLOSED) {
            d.resolvedAt = end;
        }

        return d;
    }

    // SAP: 1 = Very high, 2 = High, 3 = Medium, 4 = Low.
    // Sto export erxetai eite san "1" eite san "1-Very high", opote kratame to proto psifio.
    private static FaultSeverity toSeverity(String priority) {
        if (priority == null || priority.isBlank()) {
            return FaultSeverity.MEDIUM;
        }
        char first = priority.trim().charAt(0);
        return switch (first) {
            case '1' -> FaultSeverity.CRITICAL;
            case '2' -> FaultSeverity.HIGH;
            case '3' -> FaultSeverity.MEDIUM;
            case '4' -> FaultSeverity.LOW;
            default -> FaultSeverity.MEDIUM;
        };
    }
}
