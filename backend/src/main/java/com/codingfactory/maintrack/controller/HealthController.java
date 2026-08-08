package com.codingfactory.maintrack.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;

// Autos einai o "Controller" pou milisame - i porta eisodou.
// Otan kaneis GET request sto /api/health, apantaei oti i efarmogi einai zontani.
// Auto einai to proto mas endpoint, gia na epivevaiosoume oti o server trexei sosta.
@Tag(name = "Health", description = "Έλεγχος ότι ο server είναι ζωντανός")
@RestController
public class HealthController {

    @Operation(summary = "Έλεγχος λειτουργίας",
            description = "Δημόσιο endpoint — δεν απαιτεί σύνδεση. Χρήσιμο για να επιβεβαιώσετε "
                    + "ότι το backend τρέχει, πριν ψάξετε αλλού για πρόβλημα.")
    @GetMapping("/api/health")
    public Map<String, Object> health() {
        return Map.of(
                "status", "UP",
                "message", "Το backend του Maintrack λειτουργεί κανονικά",
                "timestamp", LocalDateTime.now()
        );
    }

}
