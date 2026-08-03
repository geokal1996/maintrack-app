package com.codingfactory.maintrack.service;

import com.codingfactory.maintrack.dto.ImportResultResponse;
import com.codingfactory.maintrack.model.*;
import com.codingfactory.maintrack.repository.FaultRepository;
import com.codingfactory.maintrack.repository.MachineRepository;
import com.codingfactory.maintrack.repository.MaintenanceActionRepository;
import com.codingfactory.maintrack.repository.UserRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

// Diavazei ena arxeio Excel (.xlsx) kai dimiourgei vlaves (kai proairetika mia
// energeia sintirisis gia tin kathemia).
//
// Vasiki arxi: MIA proximatiki grammi DEN stamataei olo to import. Kathe grammi
// elegxetai monh tis - oses einai sostes perhoun, kai gia oses den einai
// epistrefoume akrivos ti ftaiei kai se poia grammi.
@Service
public class FaultImportService {

    // I seira ton stilon sto template mas (0-based)
    private static final int COL_EXTERNAL_REF = 0;
    private static final int COL_MACHINE_CODE = 1;
    private static final int COL_TITLE = 2;
    private static final int COL_DESCRIPTION = 3;
    private static final int COL_SEVERITY = 4;
    private static final int COL_STATUS = 5;
    private static final int COL_TECHNICIAN = 6;
    private static final int COL_ACTION = 7;
    private static final int COL_DOWNTIME = 8;

    private static final String[] HEADERS = {
            "Αρ. Γνωστοποίησης", "Κωδικός Μηχανής", "Τίτλος Βλάβης", "Περιγραφή",
            "Σοβαρότητα", "Κατάσταση", "Τεχνικός (username)", "Ενέργεια Συντήρησης",
            "Χρόνος Διακοπής (λεπτά)"
    };

    private final FaultRepository faultRepository;
    private final MachineRepository machineRepository;
    private final UserRepository userRepository;
    private final MaintenanceActionRepository maintenanceActionRepository;

    public FaultImportService(FaultRepository faultRepository,
                               MachineRepository machineRepository,
                               UserRepository userRepository,
                               MaintenanceActionRepository maintenanceActionRepository) {
        this.faultRepository = faultRepository;
        this.machineRepository = machineRepository;
        this.userRepository = userRepository;
        this.maintenanceActionRepository = maintenanceActionRepository;
    }

    @Transactional
    public ImportResultResponse importFromExcel(MultipartFile file, User uploadedBy) {
        ImportResultResponse result = new ImportResultResponse();

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Δεν στάλθηκε αρχείο");
        }
        String filename = file.getOriginalFilename();
        if (filename == null || !filename.toLowerCase().endsWith(".xlsx")) {
            throw new IllegalArgumentException("Επιτρέπονται μόνο αρχεία .xlsx");
        }

        try (InputStream in = file.getInputStream(); Workbook workbook = new XSSFWorkbook(in)) {
            Sheet sheet = workbook.getSheetAt(0);

            // Ksekiname apo ti grammi 1 (i 0 einai oi epikefalides)
            for (int rowIdx = 1; rowIdx <= sheet.getLastRowNum(); rowIdx++) {
                Row row = sheet.getRow(rowIdx);
                if (isBlankRow(row)) {
                    continue;
                }
                result.setTotalRows(result.getTotalRows() + 1);
                // Sto Excel oi grammes fainontai 1-based, opote +1 gia na tairiazei
                // me auto pou vlepei o xristis sto arxeio tou.
                processRow(row, rowIdx + 1, uploadedBy, result);
            }
        } catch (IOException e) {
            throw new IllegalArgumentException("Δεν ήταν δυνατή η ανάγνωση του αρχείου Excel");
        }

        return result;
    }

    private void processRow(Row row, int excelRowNumber, User uploadedBy, ImportResultResponse result) {
        String externalRef = readString(row, COL_EXTERNAL_REF);
        String machineCode = readString(row, COL_MACHINE_CODE);
        String title = readString(row, COL_TITLE);
        String description = readString(row, COL_DESCRIPTION);
        String severityText = readString(row, COL_SEVERITY);
        String statusText = readString(row, COL_STATUS);
        String technicianUsername = readString(row, COL_TECHNICIAN);
        String actionDescription = readString(row, COL_ACTION);
        Integer downtimeMinutes = readInteger(row, COL_DOWNTIME);

        // --- Elegxoi ---
        if (machineCode == null) {
            result.addError(excelRowNumber, "Λείπει ο κωδικός μηχανής");
            return;
        }
        if (title == null) {
            result.addError(excelRowNumber, "Λείπει ο τίτλος της βλάβης");
            return;
        }

        // Idempotency: an exoume idi eisagei auti ti gnostopoiisi, tin prospername.
        if (externalRef != null && faultRepository.findByExternalRef(externalRef).isPresent()) {
            result.setSkipped(result.getSkipped() + 1);
            return;
        }

        Optional<Machine> machineOpt = machineRepository.findByCode(machineCode);
        if (machineOpt.isEmpty()) {
            result.addError(excelRowNumber, "Δεν βρέθηκε μηχανή με κωδικό '" + machineCode + "'");
            return;
        }
        Machine machine = machineOpt.get();

        FaultSeverity severity;
        try {
            severity = severityText == null ? FaultSeverity.MEDIUM
                    : FaultSeverity.valueOf(severityText.toUpperCase());
        } catch (IllegalArgumentException e) {
            result.addError(excelRowNumber, "Άγνωστη σοβαρότητα '" + severityText
                    + "' (επιτρέπονται: LOW, MEDIUM, HIGH, CRITICAL)");
            return;
        }

        FaultStatus status;
        try {
            status = statusText == null ? FaultStatus.OPEN
                    : FaultStatus.valueOf(statusText.toUpperCase());
        } catch (IllegalArgumentException e) {
            result.addError(excelRowNumber, "Άγνωστη κατάσταση '" + statusText
                    + "' (επιτρέπονται: OPEN, IN_PROGRESS, RESOLVED, CLOSED)");
            return;
        }

        // O texnikos einai proairetikos. An den dothei, xrisimopoioume auton pou anevase to arxeio.
        User technician = uploadedBy;
        if (technicianUsername != null) {
            Optional<User> technicianOpt = userRepository.findByUsername(technicianUsername);
            if (technicianOpt.isEmpty()) {
                result.addError(excelRowNumber, "Δεν βρέθηκε χρήστης με username '" + technicianUsername + "'");
                return;
            }
            technician = technicianOpt.get();
        }

        // --- Dimiourgia ---
        Fault fault = new Fault();
        fault.setMachine(machine);
        fault.setReportedBy(technician);
        fault.setTitle(title);
        fault.setDescription(description);
        fault.setSeverity(severity);
        fault.setStatus(status);
        fault.setExternalRef(externalRef);
        if (status == FaultStatus.RESOLVED || status == FaultStatus.CLOSED) {
            fault.setResolvedAt(java.time.LocalDateTime.now());
        }
        Fault savedFault = faultRepository.save(fault);

        // An i grammi exei energeia sintirisis i xrono diakopis, ti dimiourgoume kiolas.
        if (actionDescription != null || downtimeMinutes != null) {
            MaintenanceAction action = new MaintenanceAction();
            action.setFault(savedFault);
            action.setTechnician(technician);
            action.setDescription(actionDescription != null ? actionDescription : "Εισαγωγή από Excel");
            action.setDowntimeMinutes(downtimeMinutes);
            maintenanceActionRepository.save(action);
        }

        recalculateMachineStatus(machine);
        result.setImported(result.getImported() + 1);
    }

    // Idios kanonas me to DataSeeder/FaultService: DOWN an yparxei anoixti sovari vlavi,
    // UNDER_MAINTENANCE an yparxei opoiadipote alli anoixti, alliws OPERATIONAL.
    private void recalculateMachineStatus(Machine machine) {
        var faults = faultRepository.findByMachineId(machine.getId());

        boolean hasSeriousOpen = faults.stream().anyMatch(f ->
                (f.getStatus() == FaultStatus.OPEN || f.getStatus() == FaultStatus.IN_PROGRESS)
                        && (f.getSeverity() == FaultSeverity.HIGH || f.getSeverity() == FaultSeverity.CRITICAL));
        boolean hasAnyOpen = faults.stream().anyMatch(f ->
                f.getStatus() == FaultStatus.OPEN || f.getStatus() == FaultStatus.IN_PROGRESS);

        MachineStatus newStatus = hasSeriousOpen ? MachineStatus.DOWN
                : hasAnyOpen ? MachineStatus.UNDER_MAINTENANCE
                : MachineStatus.OPERATIONAL;

        if (machine.getStatus() != newStatus) {
            machine.setStatus(newStatus);
            machineRepository.save(machine);
        }
    }

    // ---------- Dimiourgia tou ypodeigmatos (template) ----------

    public byte[] buildTemplate() {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Βλάβες");

            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            Row header = sheet.createRow(0);
            for (int i = 0; i < HEADERS.length; i++) {
                Cell cell = header.createCell(i);
                cell.setCellValue(HEADERS[i]);
                cell.setCellStyle(headerStyle);
            }

            // Dio grammes-paradeigma me FANTASTIKA dedomena, gia na fainetai i morfi.
            Row example1 = sheet.createRow(1);
            example1.createCell(0).setCellValue("10000001");
            example1.createCell(1).setCellValue("CRL1");
            example1.createCell(2).setCellValue("Διαρροή λαδιού από υδραυλική μονάδα");
            example1.createCell(3).setCellValue("Παρατηρήθηκε διαρροή στη βάση της μονάδας");
            example1.createCell(4).setCellValue("MEDIUM");
            example1.createCell(5).setCellValue("CLOSED");
            example1.createCell(6).setCellValue("n.theodorou");
            example1.createCell(7).setCellValue("Αντικατάσταση τσιμούχας");
            example1.createCell(8).setCellValue(45);

            Row example2 = sheet.createRow(2);
            example2.createCell(0).setCellValue("10000002");
            example2.createCell(1).setCellValue("SAW1");
            example2.createCell(2).setCellValue("Φθορά λάμας κοπής");
            example2.createCell(3).setCellValue("Ανομοιόμορφη κοπή λόγω φθοράς");
            example2.createCell(4).setCellValue("LOW");
            example2.createCell(5).setCellValue("OPEN");
            example2.createCell(6).setCellValue("");
            example2.createCell(7).setCellValue("");
            example2.createCell(8).setCellValue("");

            for (int i = 0; i < HEADERS.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException("Δεν ήταν δυνατή η δημιουργία του υποδείγματος");
        }
    }

    // ---------- Voithitikes ----------

    private boolean isBlankRow(Row row) {
        if (row == null) {
            return true;
        }
        for (int i = 0; i <= COL_DOWNTIME; i++) {
            if (readString(row, i) != null) {
                return false;
            }
        }
        return true;
    }

    // Diavazei ena kelu san keimeno, anexartita an mesa exei arithmo i keimeno.
    // Epistrefei null an einai adeio - etsi elegxoume pantou me "== null".
    private String readString(Row row, int columnIndex) {
        Cell cell = row.getCell(columnIndex, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
        if (cell == null) {
            return null;
        }
        String value;
        if (cell.getCellType() == CellType.NUMERIC) {
            double numeric = cell.getNumericCellValue();
            // An einai akeraios (p.x. arithmos gnostopoiisis) na min grafei "1.0E7"
            value = numeric == Math.floor(numeric)
                    ? String.valueOf((long) numeric)
                    : String.valueOf(numeric);
        } else {
            value = cell.toString();
        }
        value = value.trim();
        return value.isEmpty() ? null : value;
    }

    private Integer readInteger(Row row, int columnIndex) {
        String text = readString(row, columnIndex);
        if (text == null) {
            return null;
        }
        try {
            return (int) Double.parseDouble(text);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
