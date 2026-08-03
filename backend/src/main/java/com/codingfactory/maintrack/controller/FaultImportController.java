package com.codingfactory.maintrack.controller;

import com.codingfactory.maintrack.dto.ImportResultResponse;
import com.codingfactory.maintrack.exception.ResourceNotFoundException;
import com.codingfactory.maintrack.model.User;
import com.codingfactory.maintrack.repository.UserRepository;
import com.codingfactory.maintrack.service.FaultImportService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "Fault Import", description = "Mazikí eisagogí vlavón apó arxeío Excel")
@RestController
@RequestMapping("/api/faults/import")
public class FaultImportController {

    private final FaultImportService faultImportService;
    private final UserRepository userRepository;

    public FaultImportController(FaultImportService faultImportService, UserRepository userRepository) {
        this.faultImportService = faultImportService;
        this.userRepository = userRepository;
    }

    // Anevasma arxeiou. To dikaioma (SUPERVISOR/MANAGER) orizetai sto SecurityConfig.
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ImportResultResponse importFaults(@RequestParam("file") MultipartFile file) {
        return faultImportService.importFromExcel(file, getCurrentUser());
    }

    // Katevasma tou ypodeigmatos, gia na kserei o xristis ti stiles perimenoume.
    @GetMapping("/template")
    public ResponseEntity<ByteArrayResource> downloadTemplate() {
        byte[] content = faultImportService.buildTemplate();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"maintrack-import-template.xlsx\"")
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(new ByteArrayResource(content));
    }

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new ResourceNotFoundException("Den vrethike o syndedemenos xristis"));
    }
}
