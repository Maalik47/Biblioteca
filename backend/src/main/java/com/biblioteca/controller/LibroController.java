package com.biblioteca.controller;

import com.biblioteca.api.ApiResponse;
import com.biblioteca.entity.Libro;
import com.biblioteca.service.LibroService;
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
@RequestMapping("/api/libros")
public class LibroController {

    private final LibroService libroService;

    public LibroController(LibroService libroService) {
        this.libroService = libroService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Libro>>> listar() {
        return ResponseEntity.ok(ApiResponse.ok(libroService.listar()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Libro>> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(libroService.obtener(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Libro>> crear(@RequestBody Libro libro) {
        return ResponseEntity.ok(ApiResponse.ok("Libro creado", libroService.crear(libro)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Libro>> actualizar(@PathVariable Long id,
                                                         @RequestBody Libro libro) {
        return ResponseEntity.ok(ApiResponse.ok("Libro actualizado", libroService.actualizar(id, libro)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> eliminar(@PathVariable Long id) {
        libroService.eliminar(id);
        return ResponseEntity.ok(ApiResponse.ok("Libro eliminado", null));
    }
}