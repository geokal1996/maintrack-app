package com.codingfactory.maintrack.controller;

import com.codingfactory.maintrack.dto.*;
import com.codingfactory.maintrack.model.FaultStatus;
import com.codingfactory.maintrack.service.FaultService;
import com.codingfactory.maintrack.service.MaintenanceActionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Faults", description = "Katagrafi kai diaxeirisi vlavon")
@RestController
@RequestMapping("/api/faults")
public class FaultController {

    private final FaultService faultService;
    private final MaintenanceActionService maintenanceActionService;

    public FaultController(FaultService faultService, MaintenanceActionService maintenanceActionService) {
        this.faultService = faultService;
        this.maintenanceActionService = maintenanceActionService;
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
