package com.codingfactory.maintrack.controller;

import com.codingfactory.maintrack.dto.MachineRequest;
import com.codingfactory.maintrack.dto.MachineResponse;
import com.codingfactory.maintrack.service.MachineService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Machines", description = "Διαχείριση μηχανών και εξοπλισμού")
@RestController
@RequestMapping("/api/machines")
public class MachineController {

    private final MachineService machineService;

    public MachineController(MachineService machineService) {
        this.machineService = machineService;
    }

    @Operation(summary = "Λίστα όλων των μηχανών",
            description = "Επιστρέφει όλες τις μηχανές με την τρέχουσα κατάστασή τους "
                    + "(Λειτουργική / Σε συντήρηση / Εκτός λειτουργίας). Η κατάσταση υπολογίζεται "
                    + "αυτόματα από τις ανοιχτές βλάβες της κάθε μηχανής.")
    @GetMapping
    public List<MachineResponse> getAll() {
        return machineService.getAll();
    }

    @Operation(summary = "Μία μηχανή με βάση το id της")
    @GetMapping("/{id}")
    public MachineResponse getById(@PathVariable Long id) {
        return machineService.getById(id);
    }

    @Operation(summary = "Καταχώρηση νέας μηχανής",
            description = "Ο κωδικός πρέπει να είναι μοναδικός. Επιτρέπεται σε ΠΡΟΪΣΤΑΜΕΝΟ και ΔΙΕΥΘΥΝΤΗ.")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MachineResponse create(@Valid @RequestBody MachineRequest request) {
        return machineService.create(request);
    }

    @Operation(summary = "Επεξεργασία μηχανής",
            description = "Αλλάζει κωδικό, όνομα, λειτουργική περιοχή και κατάσταση. "
                    + "Επιτρέπεται σε ΠΡΟΪΣΤΑΜΕΝΟ και ΔΙΕΥΘΥΝΤΗ.")
    @PutMapping("/{id}")
    public MachineResponse update(@PathVariable Long id, @Valid @RequestBody MachineRequest request) {
        return machineService.update(id, request);
    }

    @Operation(summary = "Διαγραφή μηχανής",
            description = "Επιτρέπεται μόνο σε ΔΙΕΥΘΥΝΤΗ και μόνο αν η μηχανή ΔΕΝ έχει καταχωρημένες "
                    + "βλάβες — αλλιώς θα χανόταν το ιστορικό συντήρησής της.")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        machineService.delete(id);
    }
}
