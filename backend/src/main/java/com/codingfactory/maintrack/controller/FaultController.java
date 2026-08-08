package com.codingfactory.maintrack.controller;

import com.codingfactory.maintrack.dto.*;
import com.codingfactory.maintrack.model.FaultStatus;
import com.codingfactory.maintrack.service.FaultExportService;
import com.codingfactory.maintrack.service.FaultService;
import com.codingfactory.maintrack.service.MaintenanceActionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Tag(name = "Faults", description = "Καταγραφή και διαχείριση βλαβών")
@RestController
@RequestMapping("/api/faults")
public class FaultController {

    private final FaultService faultService;
    private final MaintenanceActionService maintenanceActionService;
    private final FaultExportService faultExportService;

    public FaultController(FaultService faultService,
                            MaintenanceActionService maintenanceActionService,
                            FaultExportService faultExportService) {
        this.faultService = faultService;
        this.maintenanceActionService = maintenanceActionService;
        this.faultExportService = faultExportService;
    }

    @Operation(summary = "Λίστα βλαβών με σελιδοποίηση",
            description = "Επιστρέφει μία σελίδα βλαβών, ταξινομημένες από τις πιο πρόσφατες. "
                    + "Όλα τα φίλτρα είναι προαιρετικά. Το 'q' ψάχνει σε τίτλο, κωδικό και όνομα μηχανής. "
                    + "Το 'assignedToUserId' φέρνει μόνο τις βλάβες που έχουν ανατεθεί στον συγκεκριμένο χρήστη.")
    @GetMapping
    public PageResponse<FaultResponse> search(@RequestParam(required = false) FaultStatus status,
                                               @RequestParam(required = false) Long machineId,
                                               @RequestParam(required = false) Long assignedToUserId,
                                               @RequestParam(required = false) String q,
                                               @RequestParam(defaultValue = "0") int page,
                                               @RequestParam(defaultValue = "25") int size) {
        return faultService.search(status, machineId, assignedToUserId, q, page, size);
    }

    @Operation(summary = "Όλες οι βλάβες χωρίς σελιδοποίηση",
            description = "Χρήσιμο για μικρές λίστες (π.χ. οι ανοιχτές βλάβες στο Dashboard). "
                    + "Για μεγάλο όγκο δεδομένων προτιμήστε το σελιδοποιημένο GET /api/faults.")
    @GetMapping("/all")
    public List<FaultResponse> getAll(@RequestParam(required = false) FaultStatus status,
                                       @RequestParam(required = false) Long machineId) {
        return faultService.getAll(status, machineId);
    }

    @Operation(summary = "Μία βλάβη με βάση το id της")
    @GetMapping("/{id}")
    public FaultResponse getById(@PathVariable Long id) {
        return faultService.getById(id);
    }

    @Operation(summary = "Καταχώρηση νέας βλάβης",
            description = "Αν η σοβαρότητα είναι HIGH ή CRITICAL, η μηχανή περνάει αυτόματα σε "
                    + "'Εκτός λειτουργίας'. Αλλιώς σε 'Σε συντήρηση'.")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public FaultResponse create(@Valid @RequestBody FaultCreateRequest request) {
        return faultService.create(request);
    }

    @Operation(summary = "Διόρθωση βλάβης",
            description = "Αλλάζει τίτλο, περιγραφή, σοβαρότητα και μηχανή. Η κατάσταση ΔΕΝ αλλάζει "
                    + "από εδώ — γι' αυτήν υπάρχει το PATCH /api/faults/{id}/status με τους κανόνες ροής.")
    @PutMapping("/{id}")
    public FaultResponse update(@PathVariable Long id, @Valid @RequestBody FaultUpdateRequest request) {
        return faultService.update(id, request);
    }

    @Operation(summary = "Διαγραφή βλάβης",
            description = "Για εγγραφές που καταχωρήθηκαν κατά λάθος. Διαγράφει και τις ενέργειες "
                    + "συντήρησης που ανήκουν σε αυτήν. Επιτρέπεται μόνο σε MANAGER.")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        faultService.delete(id);
    }

    @Operation(summary = "Αλλαγή κατάστασης βλάβης",
            description = "Επιτρεπτή ροή: OPEN → IN_PROGRESS → RESOLVED → CLOSED. "
                    + "Μια βλάβη δεν μπορεί να κλείσει αν δεν έχει πρώτα επιλυθεί.")
    @PatchMapping("/{id}/status")
    public FaultResponse updateStatus(@PathVariable Long id, @Valid @RequestBody FaultStatusUpdateRequest request) {
        return faultService.updateStatus(id, request.getStatus());
    }

    @Operation(summary = "Εξαγωγή βλαβών σε Excel",
            description = "Κατεβάζει αρχείο .xlsx με τις βλάβες που ταιριάζουν στα ΙΔΙΑ φίλτρα "
                    + "με το GET /api/faults. Χωρίς σελιδοποίηση — το αρχείο περιέχει όλες τις "
                    + "εγγραφές που ταιριάζουν, όχι μόνο την τρέχουσα σελίδα.")
    @GetMapping("/export")
    public ResponseEntity<byte[]> export(@RequestParam(required = false) FaultStatus status,
                                          @RequestParam(required = false) Long machineId,
                                          @RequestParam(required = false) Long assignedToUserId,
                                          @RequestParam(required = false) String q) {
        byte[] file = faultExportService.export(status, machineId, assignedToUserId, q);
        String filename = "maintrack-vlaves-" + LocalDate.now() + ".xlsx";

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                // Xoris auto to header, o browser (mesw axios/CORS) DEN vlepei to
                // Content-Disposition kai den mporei na parei to onoma tou arxeiou.
                .header(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, HttpHeaders.CONTENT_DISPOSITION)
                .body(file);
    }

    @Operation(summary = "Ανάθεση βλάβης σε τεχνικό",
            description = "Ορίζει ποιος είναι υπεύθυνος για την αποκατάσταση. Στείλτε userId = null "
                    + "για να αφαιρεθεί η ανάθεση. Ένας ΤΕΧΝΙΚΟΣ μπορεί να αναθέσει βλάβη μόνο στον "
                    + "εαυτό του· ΠΡΟΪΣΤΑΜΕΝΟΣ και ΔΙΕΥΘΥΝΤΗΣ σε οποιονδήποτε ενεργό χρήστη.")
    @PatchMapping("/{id}/assignee")
    public FaultResponse assign(@PathVariable Long id, @RequestBody AssignFaultRequest request) {
        return faultService.assign(id, request.getUserId());
    }

    @Operation(summary = "Ιστορικό καταστάσεων της βλάβης",
            description = "Κάθε αλλαγή κατάστασης, με χρονική σειρά: από ποια κατάσταση, σε ποια, "
                    + "από ποιον και πότε. Η πρώτη εγγραφή είναι η δημιουργία της βλάβης.")
    @GetMapping("/{id}/history")
    public List<StatusChangeResponse> getHistory(@PathVariable Long id) {
        return faultService.getHistory(id);
    }

    @Operation(summary = "Οι ενέργειες συντήρησης μιας βλάβης")
    @GetMapping("/{id}/actions")
    public List<MaintenanceActionResponse> getActions(@PathVariable Long id) {
        return maintenanceActionService.getForFault(id);
    }

    @Operation(summary = "Καταχώρηση ενέργειας συντήρησης",
            description = "Αν η βλάβη ήταν ακόμα 'Ανοιχτή', η πρώτη ενέργεια την περνάει αυτόματα "
                    + "σε 'Σε εξέλιξη'.")
    @PostMapping("/{id}/actions")
    @ResponseStatus(HttpStatus.CREATED)
    public MaintenanceActionResponse addAction(@PathVariable Long id, @Valid @RequestBody MaintenanceActionRequest request) {
        return maintenanceActionService.create(id, request);
    }

    @Operation(summary = "Διόρθωση ενέργειας συντήρησης",
            description = "Αλλάζει περιγραφή και χρόνο διακοπής. Ο τεχνικός που την έκανε δεν αλλάζει.")
    @PutMapping("/{faultId}/actions/{actionId}")
    public MaintenanceActionResponse updateAction(@PathVariable Long faultId,
                                                   @PathVariable Long actionId,
                                                   @Valid @RequestBody MaintenanceActionRequest request) {
        return maintenanceActionService.update(faultId, actionId, request);
    }

    @Operation(summary = "Διαγραφή ενέργειας συντήρησης")
    @DeleteMapping("/{faultId}/actions/{actionId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAction(@PathVariable Long faultId, @PathVariable Long actionId) {
        maintenanceActionService.delete(faultId, actionId);
    }
}
