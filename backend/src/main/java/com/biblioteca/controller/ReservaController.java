package com.biblioteca.controller;

import com.biblioteca.api.ApiResponse;
import com.biblioteca.dto.ReservaDto;
import com.biblioteca.dto.ReservaRequest;
import com.biblioteca.entity.Usuario;
import com.biblioteca.service.ReservaService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/reservas")
public class ReservaController {

    private final ReservaService reservaService;

    public ReservaController(ReservaService reservaService) {
        this.reservaService = reservaService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ReservaDto>>> listar(
            @RequestParam(required = false) String estado) {
        return ResponseEntity.ok(ApiResponse.ok(reservaService.listar(estado)));
    }

    @GetMapping("/mias")
    public ResponseEntity<ApiResponse<List<ReservaDto>>> mias(Authentication authentication) {
        Usuario usuario = (Usuario) authentication.getPrincipal();
        return ResponseEntity.ok(ApiResponse.ok(reservaService.mios(usuario.getUsername())));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ReservaDto>> crear(@RequestBody ReservaRequest request,
                                                         Authentication authentication) {
        Usuario usuario = (Usuario) authentication.getPrincipal();
        return ResponseEntity.ok(ApiResponse.ok("Reserva registrada",
                reservaService.crear(request.libroId(), request.usuarioId(), usuario.getUsername())));
    }

    @PutMapping("/{id}/cancelar")
    public ResponseEntity<ApiResponse<ReservaDto>> cancelar(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Reserva cancelada", reservaService.cancelar(id)));
    }

    @PutMapping("/{id}/cumplir")
    public ResponseEntity<ApiResponse<Void>> cumplir(@PathVariable Long id) {
        reservaService.cumplir(id);
        return ResponseEntity.ok(ApiResponse.ok("Reserva cumplida", null));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> eliminar(@PathVariable Long id) {
        reservaService.cancelar(id);
        return ResponseEntity.ok(ApiResponse.ok("Reserva cancelada", null));
    }
}
