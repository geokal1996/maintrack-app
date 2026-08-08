package com.codingfactory.maintrack.service;

import com.codingfactory.maintrack.dto.ColumnMappingRequest;
import com.codingfactory.maintrack.dto.ImportPreviewResponse;
import com.codingfactory.maintrack.dto.ImportResultResponse;
import com.codingfactory.maintrack.dto.MachineMatchResponse;
import com.codingfactory.maintrack.model.*;
import com.codingfactory.maintrack.repository.FaultRepository;
import com.codingfactory.maintrack.repository.FaultStatusChangeRepository;
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
import java.time.LocalDateTime;
import java.util.*;

// Diavazei ena arxeio Excel (.xlsx) kai dimiourgei vlaves.
//
// YPOSTIRIZEI DYO MORFES, kai anagnorizei MONO TOU poia einai apo tis epikefalides:
//   1. To diko mas ypodeigma (9 stiles sta ellinika)
//   2. Export apo ti SAP transaction IW29 (lista gnostopoiiseon syntirisis)
//
// Ti PARAMENEI koino kai gia tis dyo morfes: mia proximatiki grammi DEN stamataei
// olo to import. Kathe grammi elegxetai moni tis - oses einai sostes perhoun, kai
// gia oses den einai epistrefoume akrivos ti ftaiei kai se poia grammi.
@Service
public class FaultImportService {

    // --- Oi stiles tou DIKOU MAS ypodeigmatos (0-based) ---
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
    private final FaultStatusChangeRepository statusChangeRepository;

    public FaultImportService(FaultRepository faultRepository,
                               MachineRepository machineRepository,
                               UserRepository userRepository,
                               MaintenanceActionRepository maintenanceActionRepository,
                               FaultStatusChangeRepository statusChangeRepository) {
        this.faultRepository = faultRepository;
        this.machineRepository = machineRepository;
        this.userRepository = userRepository;
        this.maintenanceActionRepository = maintenanceActionRepository;
        this.statusChangeRepository = statusChangeRepository;
    }

    // ---------------------------------------------------------------
    //  1o VIMA: "Ti exei mesa auto to arxeio;"
    //  Kaleitai MOLIS o xristis dialexei arxeio, PRIN kanei eisagogi.
    // ---------------------------------------------------------------

    public ImportPreviewResponse inspect(MultipartFile file) {
        validateFile(file);
        ImportPreviewResponse preview = new ImportPreviewResponse();

        try (InputStream in = file.getInputStream(); Workbook workbook = new XSSFWorkbook(in)) {
            Sheet sheet = workbook.getSheetAt(0);
            Row headerRow = sheet.getRow(0);
            if (headerRow == null) {
                throw new IllegalArgumentException("Το αρχείο δεν έχει γραμμή επικεφαλίδων");
            }

            List<String> headers = readHeaders(headerRow);
            preview.setHeaders(headers);

            if (SapIw29Mapper.matches(headers)) {
                preview.setDetectedFormat("SAP_IW29");
            } else if (matchesOurTemplate(headers)) {
                preview.setDetectedFormat("MAINTRACK_TEMPLATE");
            } else {
                preview.setDetectedFormat("UNKNOWN");
            }

            // Deigma apo tis protes grammes, gia na dei o xristis ti periexei kathe stili
            List<List<String>> sample = new ArrayList<>();
            int counted = 0;
            for (int r = 1; r <= sheet.getLastRowNum(); r++) {
                Row row = sheet.getRow(r);
                if (isBlankRow(row, headers.size())) {
                    continue;
                }
                counted++;
                if (sample.size() < 5) {
                    List<String> cells = new ArrayList<>();
                    for (int c = 0; c < headers.size(); c++) {
                        String v = ExcelCells.str(row, c);
                        cells.add(v != null ? v : "");
                    }
                    sample.add(cells);
                }
            }
            preview.setSampleRows(sample);
            preview.setTotalRows(counted);
            preview.setSuggestedMapping(ColumnGuesser.guess(headers));

        } catch (IOException e) {
            throw new IllegalArgumentException("Δεν ήταν δυνατή η ανάγνωση του αρχείου Excel");
        }

        return preview;
    }

    // ---------------------------------------------------------------
    //  ENDIAMESO VIMA: "poies mihanes anaferei to arxeio kai se poies dikes mas
    //  antistoixoun;" - kaleitai afou o xristis dialexei ti stili tis mihanis.
    //
    //  I protasi PAEI PANTA STON XRISTI gia epivevaiosi. DEN grafetai tipota sti
    //  vasi se auto to vima - gia auto akrivos den einai @Transactional.
    // ---------------------------------------------------------------

    public MachineMatchResponse matchMachines(MultipartFile file, int machineColumn) {
        validateFile(file);
        if (machineColumn < 0) {
            throw new IllegalArgumentException("Δεν ορίστηκε στήλη μηχανής");
        }

        // Metrame se poses grammes emfanizetai to kathe onoma, diatirontas ti seira emfanisis
        Map<String, Integer> occurrences = new LinkedHashMap<>();

        try (InputStream in = file.getInputStream(); Workbook workbook = new XSSFWorkbook(in)) {
            Sheet sheet = workbook.getSheetAt(0);
            for (int r = 1; r <= sheet.getLastRowNum(); r++) {
                Row row = sheet.getRow(r);
                if (row == null) {
                    continue;
                }
                String value = ExcelCells.str(row, machineColumn);
                if (value != null) {
                    occurrences.merge(value, 1, Integer::sum);
                }
            }
        } catch (IOException e) {
            throw new IllegalArgumentException("Δεν ήταν δυνατή η ανάγνωση του αρχείου Excel");
        }

        List<Machine> existing = machineRepository.findAll();
        List<MachineMatchResponse.MachineMatch> matches = new ArrayList<>();

        for (Map.Entry<String, Integer> entry : occurrences.entrySet()) {
            MachineMatchResponse.MachineMatch match =
                    new MachineMatchResponse.MachineMatch(entry.getKey(), entry.getValue());

            MachineMatcher.findBest(entry.getKey(), existing).ifPresent(found -> {
                match.setMachineId(found.machine().getId());
                match.setMachineCode(found.machine().getCode());
                match.setMachineName(found.machine().getName());
                match.setConfidence(found.confidence());
            });

            matches.add(match);
        }

        return new MachineMatchResponse(matches);
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Δεν στάλθηκε αρχείο");
        }
        String filename = file.getOriginalFilename();
        if (filename == null || !filename.toLowerCase().endsWith(".xlsx")) {
            throw new IllegalArgumentException("Επιτρέπονται μόνο αρχεία .xlsx");
        }
    }

    // ---------------------------------------------------------------
    //  2o VIMA: i eisagogi
    // ---------------------------------------------------------------

    @Transactional
    public ImportResultResponse importFromExcel(MultipartFile file, User uploadedBy) {
        return importFromExcel(file, uploadedBy, null);
    }

    // An dothei "mapping", i antistoixisi erxetai APO TON XRISTI kai to arxeio mporei
    // na exei opoiadipote onomata stilon. An den dothei, prospathoume na anagnorisoume
    // moni mas ti morfi (diko mas ypodeigma i SAP IW29).
    @Transactional
    public ImportResultResponse importFromExcel(MultipartFile file, User uploadedBy,
                                                 ColumnMappingRequest mapping) {
        ImportResultResponse result = new ImportResultResponse();
        validateFile(file);

        // Oi mihanes pou "aggixame" - i katastasi tous ypologizetai MIA FORA sto telos.
        // (An to kanoume se kathe grammi, ena arxeio 1000 grammon kanei xiliades
        // peritta erotimata sti vasi kai to import argei poly.)
        Set<Long> touchedMachineIds = new LinkedHashSet<>();

        try (InputStream in = file.getInputStream(); Workbook workbook = new XSSFWorkbook(in)) {
            Sheet sheet = workbook.getSheetAt(0);
            Row headerRow = sheet.getRow(0);
            if (headerRow == null) {
                throw new IllegalArgumentException("Το αρχείο δεν έχει γραμμή επικεφαλίδων");
            }

            List<String> headers = readHeaders(headerRow);

            boolean isSap = false;
            boolean useCustomMapping = mapping != null && mapping.getMachineCode() != null
                    && mapping.getTitle() != null;

            if (!useCustomMapping) {
                // Anagnorisi morfis. An DEN tairiazei se kammia apo tis dyo gnostes morfes,
                // stamatame EDO me kathara minima - anti na prospathisoume na diavasoume tis
                // stiles me ti seira kai na gemisoume tin othoni me akatanoita lathi ana grammi.
                isSap = SapIw29Mapper.matches(headers);
                if (!isSap && !matchesOurTemplate(headers)) {
                    throw new IllegalArgumentException(describeUnknownFormat(headers));
                }
            }

            // Otan i antistoixisi erxetai apo ton xristi, i dimiourgia mihanon einai
            // diki tou apofasi (checkbox sto frontend).
            boolean allowMachineCreation = useCustomMapping ? mapping.isCreateMissingMachines() : isSap;
            // Mono sto DIKO MAS ypodeigma theoroume lathos ena agnosto username -
            // se arxeia allon systimaton ta usernames einai fysiologika diaforetika.
            boolean strictUsernames = !useCustomMapping && !isSap;

            for (int rowIdx = 1; rowIdx <= sheet.getLastRowNum(); rowIdx++) {
                Row row = sheet.getRow(rowIdx);
                if (isBlankRow(row, headers.size())) {
                    continue;
                }
                result.setTotalRows(result.getTotalRows() + 1);

                // Sto Excel oi grammes fainontai 1-based, opote +1 gia na tairiazei
                // me auto pou vlepei o xristis sto arxeio tou.
                int excelRowNumber = rowIdx + 1;

                ImportRow data;
                try {
                    if (useCustomMapping) {
                        data = mapWithUserMapping(row, mapping);
                    } else if (isSap) {
                        data = SapIw29Mapper.map(row, headers);
                    } else {
                        data = mapTemplateRow(row);
                    }
                } catch (IllegalArgumentException e) {
                    result.addError(excelRowNumber, e.getMessage());
                    continue;
                } catch (Exception e) {
                    result.addError(excelRowNumber, "Η γραμμή δεν μπόρεσε να διαβαστεί");
                    continue;
                }

                persistRow(data, excelRowNumber, uploadedBy, allowMachineCreation, strictUsernames,
                        result, touchedMachineIds);
            }
        } catch (IOException e) {
            throw new IllegalArgumentException("Δεν ήταν δυνατή η ανάγνωση του αρχείου Excel");
        }

        touchedMachineIds.forEach(this::recalculateMachineStatus);
        return result;
    }

    // ---------------------------------------------------------------
    //  Apothikefsi - koini kai gia tis dyo morfes arxeiou
    // ---------------------------------------------------------------

    // allowMachineCreation = na dimiourgountai automata oi mihanes pou den yparxoun
    // strictUsernames    = an ena agnosto username einai lathos (diko mas ypodeigma)
    //                      i apla to agnooume (arxeia apo alla systimata)
    private void persistRow(ImportRow data, int excelRowNumber, User uploadedBy,
                             boolean allowMachineCreation, boolean strictUsernames,
                             ImportResultResponse result, Set<Long> touchedMachineIds) {

        if (data.skip) {
            result.setSkipped(result.getSkipped() + 1);
            return;
        }
        if (data.machineCode == null) {
            result.addError(excelRowNumber, "Λείπει ο κωδικός μηχανής / λειτουργικής περιοχής");
            return;
        }
        if (data.title == null) {
            result.addError(excelRowNumber, "Λείπει ο τίτλος της βλάβης");
            return;
        }

        // Prostasia apo diplografes: an i idia gnostopoiisi exei idi eisaxthei, tin prospername.
        if (data.externalRef != null && faultRepository.findByExternalRef(data.externalRef).isPresent()) {
            result.setSkipped(result.getSkipped() + 1);
            return;
        }

        // An o xristis epelexe rita mihani ston pinaka epivevaiosis, ti xrisimopoioume.
        // Alliws psaxnoume me ton kodiko opos einai grammenos sto arxeio.
        Optional<Machine> machineOpt = data.resolvedMachineId != null
                ? machineRepository.findById(data.resolvedMachineId)
                : machineRepository.findByCode(data.machineCode);

        Machine machine;
        if (machineOpt.isPresent()) {
            machine = machineOpt.get();
        } else if (allowMachineCreation) {
            // Dimiourgia sti stigmi: exoume kodiko, kai (an dothike) onoma kai perioxi.
            machine = new Machine(
                    data.machineCode,
                    data.machineName != null ? data.machineName : data.machineCode,
                    data.machineArea,
                    MachineStatus.OPERATIONAL
            );
            machine = machineRepository.save(machine);
        } else {
            result.addError(excelRowNumber, "Δεν βρέθηκε μηχανή με κωδικό '" + data.machineCode + "'");
            return;
        }

        // O texnikos einai proairetikos. An to username den antistoixei se xristi mas
        // (p.x. logariasmoi SAP), xrisimopoioume auton pou anevase to arxeio.
        User technician = uploadedBy;
        if (data.technicianUsername != null) {
            Optional<User> found = userRepository.findByUsername(data.technicianUsername);
            if (found.isPresent()) {
                technician = found.get();
            } else if (strictUsernames) {
                // Sto diko mas ypodeigma to lathos username einai onto lathos tou xristi
                result.addError(excelRowNumber,
                        "Δεν βρέθηκε χρήστης με username '" + data.technicianUsername + "'");
                return;
            }
        }

        // Deftero diktyo prostasias, gia arxeia XORIS monadiko kodiko (p.x. to
        // xeirokinito Excel): idia mihani + idios titlos + idia mera = idia vlavi.
        if (data.externalRef == null && data.occurredAt != null) {
            LocalDateTime dayStart = data.occurredAt.toLocalDate().atStartOfDay();
            boolean alreadyThere = faultRepository.existsByMachineIdAndTitleAndCreatedAtBetween(
                    machine.getId(), data.title, dayStart, dayStart.plusDays(1).minusNanos(1));
            if (alreadyThere) {
                result.setSkipped(result.getSkipped() + 1);
                return;
            }
        }

        Fault fault = new Fault();
        fault.setMachine(machine);
        fault.setReportedBy(technician);
        // Stin eisagogi apo arxeio, o texnikos tis grammis einai kai o ypeuthinos tis vlavis
        fault.setAssignedTo(technician);
        // I pragmatiki imerominia tis vlavis - to @PrePersist tou Fault vazei "tora"
        // MONO an afiso to pedio null.
        fault.setCreatedAt(data.occurredAt);
        fault.setTitle(data.title);
        fault.setDescription(data.description);
        fault.setSeverity(data.severity != null ? data.severity : FaultSeverity.MEDIUM);
        fault.setStatus(data.status != null ? data.status : FaultStatus.OPEN);
        fault.setExternalRef(data.externalRef);

        if (data.resolvedAt != null) {
            fault.setResolvedAt(data.resolvedAt);
        } else if (fault.getStatus() == FaultStatus.RESOLVED || fault.getStatus() == FaultStatus.CLOSED) {
            fault.setResolvedAt(LocalDateTime.now());
        }

        Fault savedFault = faultRepository.save(fault);

        // Proti eggrafi sto istoriko: pote katachorithike i vlavi kai apo poion aneva to arxeio.
        // Ta endiamesa vimata den ta kseroume (to arxeio dinei mono tin teliki katastasi),
        // gi' auto katagrafoume MONO ti dimiourgia - den efevriskoume istoriko pou den yparxei.
        FaultStatusChange creation = new FaultStatusChange(
                savedFault, null, savedFault.getStatus(), technician);
        creation.setChangedAt(savedFault.getCreatedAt());
        statusChangeRepository.save(creation);

        // An i grammi exei energeia sintirisis i xrono diakopis, ti dimiourgoume kiolas.
        if (data.actionDescription != null || data.downtimeMinutes != null) {
            MaintenanceAction action = new MaintenanceAction();
            action.setFault(savedFault);
            action.setTechnician(technician);
            action.setDescription(data.actionDescription != null ? data.actionDescription : "Εισαγωγή από Excel");
            action.setDowntimeMinutes(data.downtimeMinutes);
            // I energeia na exei tin imerominia tis vlavis, oxi tin ora tou anevasmatos
            if (data.occurredAt != null) {
                action.setActionDate(data.occurredAt);
            }
            maintenanceActionRepository.save(action);
        }

        touchedMachineIds.add(machine.getId());
        result.setImported(result.getImported() + 1);
    }

    // ---------------------------------------------------------------
    //  Metafrasi grammis me tin antistoixisi POU EDOSE O XRISTIS
    // ---------------------------------------------------------------

    private ImportRow mapWithUserMapping(Row row, ColumnMappingRequest m) {
        ImportRow d = new ImportRow();

        d.externalRef = ExcelCells.str(row, orNeg(m.getExternalRef()));
        d.machineCode = ExcelCells.str(row, orNeg(m.getMachineCode()));
        d.machineName = ExcelCells.str(row, orNeg(m.getMachineName()));

        // Ti apofasise o xristis ston pinaka epivevaiosis gia auto to onoma mihanis
        if (m.getMachineResolutions() != null && d.machineCode != null) {
            d.resolvedMachineId = m.getMachineResolutions().get(d.machineCode);
        }

        // I pragmatiki imerominia tis vlavis
        if (m.getDate() != null) {
            java.time.LocalDate day = ExcelCells.date(row, m.getDate());
            if (day != null) {
                d.occurredAt = day.atStartOfDay();
            }
        }

        d.title = ExcelCells.str(row, orNeg(m.getTitle()));
        d.description = ExcelCells.str(row, orNeg(m.getDescription()));
        d.technicianUsername = ExcelCells.str(row, orNeg(m.getTechnician()));
        d.actionDescription = ExcelCells.str(row, orNeg(m.getAction()));

        // Xronos diakopis - me metatropi an to arxeio dinei ores
        Double raw = ExcelCells.number(row, orNeg(m.getDowntime()));
        if (raw != null && raw > 0) {
            double minutes = "HOURS".equalsIgnoreCase(m.getDowntimeUnit()) ? raw * 60 : raw;
            d.downtimeMinutes = (int) Math.round(minutes);
        }

        // Sovarotita: dexomaste kai ta dika mas onomata (LOW/MEDIUM/...) kai
        // arithmitiki proteraiotita typou SAP (1 = pio sovari).
        d.severity = parseSeverity(ExcelCells.str(row, orNeg(m.getSeverity())));

        // Katastasi: dexomaste ta dika mas onomata kai tous kodikous SAP
        d.status = parseStatus(ExcelCells.str(row, orNeg(m.getStatus())));

        return d;
    }

    private int orNeg(Integer index) {
        return index != null ? index : -1;
    }

    private FaultSeverity parseSeverity(String text) {
        if (text == null || text.isBlank()) {
            return FaultSeverity.MEDIUM;
        }
        String t = text.trim().toUpperCase();
        for (FaultSeverity s : FaultSeverity.values()) {
            if (t.startsWith(s.name())) {
                return s;
            }
        }
        return switch (t.charAt(0)) {
            case '1' -> FaultSeverity.CRITICAL;
            case '2' -> FaultSeverity.HIGH;
            case '3' -> FaultSeverity.MEDIUM;
            case '4' -> FaultSeverity.LOW;
            default -> FaultSeverity.MEDIUM;
        };
    }

    private FaultStatus parseStatus(String text) {
        if (text == null || text.isBlank()) {
            return FaultStatus.OPEN;
        }
        String t = text.trim().toUpperCase();
        for (FaultStatus s : FaultStatus.values()) {
            if (t.contains(s.name())) {
                return s;
            }
        }
        if (t.contains("NOCO")) return FaultStatus.CLOSED;
        if (t.contains("NOPR")) return FaultStatus.IN_PROGRESS;
        return FaultStatus.OPEN;
    }

    // ---------------------------------------------------------------
    //  Metafrasi grammis apo TO DIKO MAS ypodeigma
    // ---------------------------------------------------------------

    private ImportRow mapTemplateRow(Row row) {
        ImportRow d = new ImportRow();
        d.externalRef = ExcelCells.str(row, COL_EXTERNAL_REF);
        d.machineCode = ExcelCells.str(row, COL_MACHINE_CODE);
        d.title = ExcelCells.str(row, COL_TITLE);
        d.description = ExcelCells.str(row, COL_DESCRIPTION);
        d.technicianUsername = ExcelCells.str(row, COL_TECHNICIAN);
        d.actionDescription = ExcelCells.str(row, COL_ACTION);
        d.downtimeMinutes = ExcelCells.integer(row, COL_DOWNTIME);

        String severityText = ExcelCells.str(row, COL_SEVERITY);
        if (severityText != null) {
            try {
                d.severity = FaultSeverity.valueOf(severityText.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Άγνωστη σοβαρότητα '" + severityText
                        + "' (επιτρέπονται: LOW, MEDIUM, HIGH, CRITICAL)");
            }
        }

        String statusText = ExcelCells.str(row, COL_STATUS);
        if (statusText != null) {
            try {
                d.status = FaultStatus.valueOf(statusText.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Άγνωστη κατάσταση '" + statusText
                        + "' (επιτρέπονται: OPEN, IN_PROGRESS, RESOLVED, CLOSED)");
            }
        }

        return d;
    }

    // ---------------------------------------------------------------
    //  Voithitikes
    // ---------------------------------------------------------------

    private List<String> readHeaders(Row headerRow) {
        List<String> headers = new ArrayList<>();
        for (int c = 0; c < headerRow.getLastCellNum(); c++) {
            String h = ExcelCells.str(headerRow, c);
            headers.add(h != null ? h : "");
        }
        return headers;
    }

    // Anagnorizei to DIKO MAS ypodeigma. Den apaitoume na yparxoun OLES oi stiles -
    // arkoun oi dyo ypoxreotikes (kodikos mihanis kai titlos vlavis).
    private boolean matchesOurTemplate(List<String> headers) {
        Set<String> normalized = new HashSet<>();
        for (String h : headers) {
            normalized.add(normalizeHeader(h));
        }
        return normalized.contains(normalizeHeader(HEADERS[COL_MACHINE_CODE]))
                && normalized.contains(normalizeHeader(HEADERS[COL_TITLE]));
    }

    private String normalizeHeader(String header) {
        return header == null ? "" : header.toLowerCase().replaceAll("[\\s.]", "");
    }

    // Xtizei ena minima pou leei ston xristi TI VRIKAME kai TI PERIMENAME,
    // oste na katalavei amesos giati aporrifthike to arxeio tou.
    private String describeUnknownFormat(List<String> headers) {
        String found = headers.stream()
                .filter(h -> h != null && !h.isBlank())
                .limit(12)
                .collect(java.util.stream.Collectors.joining(", "));

        return "Δεν αναγνωρίστηκε η μορφή του αρχείου."
                + " Βρέθηκαν οι στήλες: " + (found.isBlank() ? "(καμία)" : found) + "."
                + " Απαιτείται είτε το υπόδειγμα του Maintrack (στήλες «"
                + HEADERS[COL_MACHINE_CODE] + "» και «" + HEADERS[COL_TITLE]
                + "» — κατέβασέ το από το κουμπί «Κατέβασε υπόδειγμα»),"
                + " είτε export γνωστοποιήσεων από SAP IW29 (στήλες «Notification» και"
                + " «FLoc. affected» ή «Equipment»).";
    }

    private boolean isBlankRow(Row row, int columnCount) {
        if (row == null) {
            return true;
        }
        for (int i = 0; i < Math.max(columnCount, COL_DOWNTIME + 1); i++) {
            if (ExcelCells.str(row, i) != null) {
                return false;
            }
        }
        return true;
    }

    // Idios kanonas me to DataSeeder/FaultService: DOWN an yparxei anoixti sovari vlavi,
    // UNDER_MAINTENANCE an yparxei opoiadipote alli anoixti, alliws OPERATIONAL.
    private void recalculateMachineStatus(Long machineId) {
        Machine machine = machineRepository.findById(machineId).orElse(null);
        if (machine == null) {
            return;
        }
        List<Fault> faults = faultRepository.findByMachineId(machineId);

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

    // ---------------------------------------------------------------
    //  Dimiourgia tou ypodeigmatos (template)
    // ---------------------------------------------------------------

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
}
