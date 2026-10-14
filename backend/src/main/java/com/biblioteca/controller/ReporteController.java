package com.biblioteca.controller;

import com.biblioteca.api.ApiResponse;
import com.biblioteca.service.ReporteService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/reportes")
public class ReporteController {

    private final ReporteService reporteService;

    public ReporteController(ReporteService reporteService) {
        this.reporteService = reporteService;
    }

    @GetMapping("/resumen")
    public ResponseEntity<ApiResponse<Map<String, Object>>> resumen() {
        return ResponseEntity.ok(ApiResponse.ok(reporteService.resumen()));
    }

    @GetMapping(value = "/libros.csv", produces = "text/csv;charset=UTF-8")
    public ResponseEntity<byte[]> librosCsv() {
        return csv("libros.csv", reporteService.librosCsv());
    }

    @GetMapping(value = "/prestamos.csv", produces = "text/csv;charset=UTF-8")
    public ResponseEntity<byte[]> prestamosCsv() {
        return csv("prestamos.csv", reporteService.prestamosCsv());
    }

    private ResponseEntity<byte[]> csv(String nombre, String contenido) {
        byte[] body = ("\uFEFF" + contenido).getBytes(java.nio.charset.StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + nombre + "\"")
                .contentType(MediaType.parseMediaType("text/csv;charset=UTF-8"))
                .body(body);
    }
}
