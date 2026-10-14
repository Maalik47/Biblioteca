package com.biblioteca.controller;

import com.biblioteca.api.ApiResponse;
import com.biblioteca.dto.AuditoriaDto;
import com.biblioteca.dto.PaginaResponse;
import com.biblioteca.service.AuditoriaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auditoria")
public class AuditoriaController {

    private final AuditoriaService auditoriaService;

    public AuditoriaController(AuditoriaService auditoriaService) {
        this.auditoriaService = auditoriaService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PaginaResponse<AuditoriaDto>>> listar(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String accion,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size) {
        var pageResult = auditoriaService.buscar(q, accion, page, size);
        return ResponseEntity.ok(ApiResponse.ok(
                PaginaResponse.desde(pageResult, AuditoriaDto::from)));
    }
}
