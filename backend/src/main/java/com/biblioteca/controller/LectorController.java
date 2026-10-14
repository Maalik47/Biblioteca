package com.biblioteca.controller;

import com.biblioteca.api.ApiResponse;
import com.biblioteca.dto.LectorRequest;
import com.biblioteca.dto.PaginaResponse;
import com.biblioteca.dto.UsuarioDto;
import com.biblioteca.service.UsuarioService;
import jakarta.validation.Valid;
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

import com.biblioteca.entity.Usuario;

@RestController
@RequestMapping("/api/lectores")
public class LectorController {

    private final UsuarioService usuarioService;

    public LectorController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PaginaResponse<UsuarioDto>>> listar(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String tipoLector,
            @RequestParam(required = false) Boolean soloAcceso,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.ok(
                usuarioService.buscar(q, null, tipoLector, soloAcceso, page, size)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UsuarioDto>> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(usuarioService.obtener(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<UsuarioDto>> crear(@Valid @RequestBody LectorRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Lector creado", usuarioService.crearLector(request)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<UsuarioDto>> actualizar(@PathVariable Long id,
                                                              @Valid @RequestBody LectorRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Lector actualizado", usuarioService.actualizarLector(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> eliminar(@PathVariable Long id, Authentication authentication) {
        Usuario sesion = (Usuario) authentication.getPrincipal();
        usuarioService.eliminar(id, sesion.getId());
        return ResponseEntity.ok(ApiResponse.ok("Lector eliminado", null));
    }
}
