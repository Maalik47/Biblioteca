package com.biblioteca.controller;

import com.biblioteca.api.ApiResponse;
import com.biblioteca.dto.PrestamoRequest;
import com.biblioteca.dto.PrestamoResponse;
import com.biblioteca.service.PrestamoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/prestamos")
public class PrestamoController {

    private final PrestamoService prestamoService;

    public PrestamoController(PrestamoService prestamoService) {
        this.prestamoService = prestamoService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<PrestamoResponse>>> listar() {
        return ResponseEntity.ok(ApiResponse.ok(prestamoService.listar()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PrestamoResponse>> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(prestamoService.obtener(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<PrestamoResponse>> crear(@RequestBody PrestamoRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Préstamo registrado", prestamoService.crear(request)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<PrestamoResponse>> actualizar(@PathVariable Long id,
                                                                    @RequestBody PrestamoRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Préstamo actualizado", prestamoService.actualizar(id, request)));
    }

    @PutMapping("/{id}/devolver")
    public ResponseEntity<ApiResponse<PrestamoResponse>> devolver(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Préstamo devuelto", prestamoService.devolver(id)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> eliminar(@PathVariable Long id) {
        prestamoService.eliminar(id);
        return ResponseEntity.ok(ApiResponse.ok("Préstamo eliminado", null));
    }
}