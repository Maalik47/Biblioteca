package com.biblioteca.controller;

import com.biblioteca.api.ApiResponse;
import com.biblioteca.dto.EjemplarDto;
import com.biblioteca.service.EjemplarService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ejemplares")
public class EjemplarController {

    private final EjemplarService ejemplarService;

    public EjemplarController(EjemplarService ejemplarService) {
        this.ejemplarService = ejemplarService;
    }

    @PutMapping("/{id}/estado")
    public ResponseEntity<ApiResponse<EjemplarDto>> cambiarEstado(@PathVariable Long id,
                                                                  @RequestParam String estado) {
        return ResponseEntity.ok(ApiResponse.ok("Estado actualizado",
                ejemplarService.cambiarEstado(id, estado)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> eliminar(@PathVariable Long id) {
        ejemplarService.eliminar(id);
        return ResponseEntity.ok(ApiResponse.ok("Ejemplar eliminado", null));
    }
}
