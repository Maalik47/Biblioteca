package com.biblioteca.controller;

import com.biblioteca.api.ApiResponse;
import com.biblioteca.dto.UsuarioDto;
import com.biblioteca.entity.Usuario;
import com.biblioteca.service.UsuarioService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
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
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<UsuarioDto>>> listar() {
        return ResponseEntity.ok(ApiResponse.ok(usuarioService.listar()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UsuarioDto>> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(usuarioService.obtener(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<UsuarioDto>> crear(@RequestBody Usuario usuario) {
        return ResponseEntity.ok(ApiResponse.ok("Usuario creado", usuarioService.crear(usuario)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<UsuarioDto>> actualizar(@PathVariable Long id,
                                                              @RequestBody Usuario usuario) {
        return ResponseEntity.ok(ApiResponse.ok("Usuario actualizado", usuarioService.actualizar(id, usuario)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> eliminar(@PathVariable Long id, Authentication authentication) {
        Usuario sesion = (Usuario) authentication.getPrincipal();
        usuarioService.eliminar(id, sesion.getId());
        return ResponseEntity.ok(ApiResponse.ok("Usuario eliminado", null));
    }
}