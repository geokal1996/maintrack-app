package com.codingfactory.maintrack.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;

// Autos einai o "Controller" pou milisame - i porta eisodou.
// Otan kaneis GET request sto /api/health, apantaei oti i efarmogi einai zontani.
// Auto einai to proto mas endpoint, gia na epivevaiosoume oti o server trexei sosta.
@RestController
public class HealthController {

    @GetMapping("/api/health")
    public Map<String, Object> health() {
        return Map.of(
                "status", "UP",
                "message", "Maintrack backend trexei kanonika",
                "timestamp", LocalDateTime.now()
        );
    }

}
