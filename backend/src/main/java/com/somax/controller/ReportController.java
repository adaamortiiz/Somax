package com.somax.controller;

import com.somax.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/report")
@RequiredArgsConstructor
public class ReportController {
    private final ReportService reportService;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/csv")
    public ResponseEntity<byte[]> exportCsv() {
        byte[] content = reportService.exportCsv();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=ocupacion.csv")
                .contentType(MediaType.TEXT_PLAIN).body(content);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/pdf")
    public ResponseEntity<byte[]> exportPdf() {
        byte[] content = reportService.exportPdf();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=ocupacion.pdf")
                .contentType(MediaType.APPLICATION_PDF).body(content);
    }
}
