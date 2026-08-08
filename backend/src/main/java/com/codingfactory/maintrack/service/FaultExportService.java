package com.codingfactory.maintrack.service;

import com.codingfactory.maintrack.model.Fault;
import com.codingfactory.maintrack.model.FaultSeverity;
import com.codingfactory.maintrack.model.FaultStatus;
import com.codingfactory.maintrack.model.MaintenanceAction;
import com.codingfactory.maintrack.repository.FaultRepository;
import com.codingfactory.maintrack.repository.MaintenanceActionRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Vgazei tis vlaves se arxeio Excel, me TA IDIA FILTRA pou vlepei o xristis sti lista.
//
// Giati exei simasia na einai ta idia filtra: o proistamenos filtrarei "anoixtes vlaves
// tis grammis 3", to vlepei stin othoni, kai patai "Eksagogi". An to arxeio eferne OLES
// tis vlaves, tha itan mia dysarestii ekplixi - kai tha to katalavaine argotera.
@Service
public class FaultExportService {

    private static final String[] HEADERS = {
            "Αρ. Γνωστοποίησης", "Κωδικός Μηχανής", "Μηχανή", "Περιοχή",
            "Τίτλος Βλάβης", "Περιγραφή", "Σοβαρότητα", "Κατάσταση",
            "Ανέφερε", "Ανατέθηκε σε", "Ημ/νία Βλάβης", "Ημ/νία Επίλυσης",
            "Χρόνος Διακοπής (λεπτά)"
    };

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final FaultRepository faultRepository;
    private final MaintenanceActionRepository maintenanceActionRepository;

    public FaultExportService(FaultRepository faultRepository,
                               MaintenanceActionRepository maintenanceActionRepository) {
        this.faultRepository = faultRepository;
        this.maintenanceActionRepository = maintenanceActionRepository;
    }

    public byte[] export(FaultStatus status, Long machineId, Long assignedToUserId, String q) {
        String query = (q == null || q.isBlank()) ? null : q.trim();

        // Xoris selidopoiisi: to arxeio Excel prepei na exei OLA osa vlepei o xristis
        // me ta trexonta filtra, oxi mono tin proti selida.
        List<Fault> faults = faultRepository.searchAll(
                status, machineId, assignedToUserId, query,
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        Map<Long, Long> downtimePerFault = downtimePerFault();

        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Βλάβες");
            writeHeader(workbook, sheet);

            int rowNum = 1;
            for (Fault fault : faults) {
                writeFault(sheet.createRow(rowNum++), fault, downtimePerFault);
            }

            // "Pagoma" tis protis grammis: otan o xristis skrolarei 200 vlaves,
            // synexizei na vlepei ti simainei kathe stili.
            sheet.createFreezePane(0, 1);
            sheet.setAutoFilter(new CellRangeAddress(0, Math.max(0, rowNum - 1), 0, HEADERS.length - 1));
            autoSizeColumns(sheet);

            workbook.write(out);
            return out.toByteArray();

        } catch (IOException e) {
            throw new IllegalStateException("Δεν ήταν δυνατή η δημιουργία του αρχείου Excel", e);
        }
    }

    // Ta lepta diakopis ana vlavi. Ta diavazoume MIA fora kai ta omadopoioume,
    // anti gia ena erotima ana vlavi (pou me 1000 vlaves tha itan 1000 erotimata).
    private Map<Long, Long> downtimePerFault() {
        Map<Long, Long> totals = new HashMap<>();
        for (MaintenanceAction action : maintenanceActionRepository.findAll()) {
            if (action.getDowntimeMinutes() != null) {
                totals.merge(action.getFault().getId(), action.getDowntimeMinutes().longValue(), Long::sum);
            }
        }
        return totals;
    }

    private void writeHeader(Workbook workbook, Sheet sheet) {
        CellStyle headerStyle = workbook.createCellStyle();
        Font bold = workbook.createFont();
        bold.setBold(true);
        headerStyle.setFont(bold);
        headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        Row header = sheet.createRow(0);
        for (int i = 0; i < HEADERS.length; i++) {
            Cell cell = header.createCell(i);
            cell.setCellValue(HEADERS[i]);
            cell.setCellStyle(headerStyle);
        }
    }

    private void writeFault(Row row, Fault fault, Map<Long, Long> downtimePerFault) {
        int col = 0;
        row.createCell(col++).setCellValue(nullToEmpty(fault.getExternalRef()));
        row.createCell(col++).setCellValue(fault.getMachine().getCode());
        row.createCell(col++).setCellValue(nullToEmpty(fault.getMachine().getName()));
        row.createCell(col++).setCellValue(nullToEmpty(fault.getMachine().getArea()));
        row.createCell(col++).setCellValue(fault.getTitle());
        row.createCell(col++).setCellValue(nullToEmpty(fault.getDescription()));
        row.createCell(col++).setCellValue(severityLabel(fault.getSeverity()));
        row.createCell(col++).setCellValue(statusLabel(fault.getStatus()));
        row.createCell(col++).setCellValue(fault.getReportedBy() != null ? fault.getReportedBy().getFullName() : "");
        row.createCell(col++).setCellValue(fault.getAssignedTo() != null ? fault.getAssignedTo().getFullName() : "");
        row.createCell(col++).setCellValue(formatDate(fault.getCreatedAt()));
        row.createCell(col++).setCellValue(formatDate(fault.getResolvedAt()));

        Long downtime = downtimePerFault.get(fault.getId());
        if (downtime != null) {
            row.createCell(col).setCellValue(downtime);
        } else {
            // Keno keli, OXI midenika. To "0 lepta" simainei "stamatise kai epanilthe
            // amesos" - to keno simainei "den to metrisame". Diaforetika pragmata.
            row.createCell(col).setCellValue("");
        }
    }

    // To autoSizeColumn tou POI metraei to platos ton grammaton, opote xreiazetai
    // grammatoseires apo to leitourgiko. Se ena "gymno" Docker container mporei
    // na min yparxoun kai na petaxei exception. Se afti tin periptosi den akyronoume
    // olo to arxeio gia ena kallitexniko thema - vazoume stathero platos.
    private void autoSizeColumns(Sheet sheet) {
        try {
            for (int i = 0; i < HEADERS.length; i++) {
                sheet.autoSizeColumn(i);
                // I perigrafi mporei na einai terastia - vazoume ano orio platous
                if (sheet.getColumnWidth(i) > 15000) {
                    sheet.setColumnWidth(i, 15000);
                }
            }
        } catch (RuntimeException ex) {
            for (int i = 0; i < HEADERS.length; i++) {
                sheet.setColumnWidth(i, 5000);
            }
        }
    }

    private String formatDate(LocalDateTime value) {
        return value == null ? "" : value.format(DATE_FORMAT);
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    private String severityLabel(FaultSeverity severity) {
        if (severity == null) {
            return "";
        }
        return switch (severity) {
            case LOW -> "Χαμηλή";
            case MEDIUM -> "Μεσαία";
            case HIGH -> "Υψηλή";
            case CRITICAL -> "Κρίσιμη";
        };
    }

    private String statusLabel(FaultStatus status) {
        if (status == null) {
            return "";
        }
        return switch (status) {
            case OPEN -> "Ανοιχτή";
            case IN_PROGRESS -> "Σε εξέλιξη";
            case RESOLVED -> "Επιλύθηκε";
            case CLOSED -> "Έκλεισε";
        };
    }
}
