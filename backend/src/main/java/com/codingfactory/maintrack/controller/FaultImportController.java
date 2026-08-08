package com.codingfactory.maintrack.controller;

import com.codingfactory.maintrack.dto.ColumnMappingRequest;
import com.codingfactory.maintrack.dto.ImportPreviewResponse;
import com.codingfactory.maintrack.dto.ImportResultResponse;
import com.codingfactory.maintrack.dto.MachineMatchResponse;
import com.codingfactory.maintrack.exception.ResourceNotFoundException;
import com.codingfactory.maintrack.model.User;
import com.codingfactory.maintrack.repository.UserRepository;
import com.codingfactory.maintrack.service.FaultImportService;
import com.fasterxml.jackson.databind.ObjectMapper;
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
    private final ObjectMapper objectMapper;

    public FaultImportController(FaultImportService faultImportService, UserRepository userRepository,
                                  ObjectMapper objectMapper) {
        this.faultImportService = faultImportService;
        this.userRepository = userRepository;
        this.objectMapper = objectMapper;
    }

    // 1o vima: "ti exei mesa auto to arxeio;" - epistrefei tis stiles tou, ena deigma
    // ton protwn grammon kai mia protasi antistoixisis. Den apothikevei TIPOTA.
    @PostMapping(value = "/inspect", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ImportPreviewResponse inspect(@RequestParam("file") MultipartFile file) {
        return faultImportService.inspect(file);
    }

    // Endiameso vima: "poies mihanes anaferei to arxeio kai se poies dikes mas
    // antistoixoun;" - epistrefei protaseis pou o xristis epivevaionei i allazei.
    // DEN grafei tipota sti vasi.
    @PostMapping(value = "/match-machines", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public MachineMatchResponse matchMachines(@RequestParam("file") MultipartFile file,
                                               @RequestParam("machineColumn") int machineColumn) {
        return faultImportService.matchMachines(file, machineColumn);
    }

    // 2o vima: i eisagogi. To "mapping" einai PROAIRETIKO - an dothei, i antistoixisi
    // ton stilon erxetai apo ton xristi· an leipei, prospathoume na anagnorisoume
    // moni mas ti morfi (diko mas ypodeigma i SAP IW29).
    // To dikaioma (SUPERVISOR/MANAGER) orizetai sto SecurityConfig.
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ImportResultResponse importFaults(@RequestParam("file") MultipartFile file,
                                              @RequestParam(value = "mapping", required = false) String mappingJson) {
        ColumnMappingRequest mapping = null;
        if (mappingJson != null && !mappingJson.isBlank()) {
            try {
                mapping = objectMapper.readValue(mappingJson, ColumnMappingRequest.class);
            } catch (Exception e) {
                throw new IllegalArgumentException("Η αντιστοίχιση στηλών δεν είναι έγκυρη");
            }
        }
        return faultImportService.importFromExcel(file, getCurrentUser(), mapping);
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
