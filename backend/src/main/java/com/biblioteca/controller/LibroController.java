package com.biblioteca.controller;

import com.biblioteca.api.ApiResponse;
import com.biblioteca.dto.EjemplarDto;
import com.biblioteca.dto.LibroDto;
import com.biblioteca.dto.PaginaResponse;
import com.biblioteca.entity.Libro;
import com.biblioteca.repository.LibroRepository;
import com.biblioteca.service.EjemplarService;
import com.biblioteca.service.LibroService;
import org.springframework.http.ResponseEntity;
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
@RequestMapping("/api/libros")
public class LibroController {

    private final LibroService libroService;
    private final EjemplarService ejemplarService;
    private final LibroRepository libroRepository;

    public LibroController(LibroService libroService,
                           EjemplarService ejemplarService,
                           LibroRepository libroRepository) {
        this.libroService = libroService;
        this.ejemplarService = ejemplarService;
        this.libroRepository = libroRepository;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<LibroDto>>> listar() {
        return ResponseEntity.ok(ApiResponse.ok(libroService.listar()));
    }

    @GetMapping("/buscar")
    public ResponseEntity<ApiResponse<PaginaResponse<LibroDto>>> buscar(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String categoria,
            @RequestParam(required = false) String estado,
            @RequestParam(defaultValue = "false") boolean soloDisponibles,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.ok(
                libroService.buscar(q, categoria, estado, soloDisponibles, page, size)));
    }

    @GetMapping("/categorias")
    public ResponseEntity<ApiResponse<List<String>>> categorias() {
        return ResponseEntity.ok(ApiResponse.ok(libroService.categorias()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<LibroDto>> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(libroService.obtener(id)));
    }

    @GetMapping("/{id}/ejemplares")
    public ResponseEntity<ApiResponse<List<EjemplarDto>>> ejemplares(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(ejemplarService.listarPorLibro(id)));
    }

    @PostMapping("/{id}/ejemplares")
    public ResponseEntity<ApiResponse<EjemplarDto>> agregarEjemplar(@PathVariable Long id,
                                                                    @RequestParam(required = false) String ubicacion) {
        Libro libro = libroRepository.findById(id).orElse(null);
        return ResponseEntity.ok(ApiResponse.ok("Ejemplar creado",
                ejemplarService.crear(id, libro, ubicacion)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<LibroDto>> crear(@RequestBody Libro libro) {
        return ResponseEntity.ok(ApiResponse.ok("Libro creado", libroService.crear(libro)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<LibroDto>> actualizar(@PathVariable Long id,
                                                            @RequestBody Libro libro) {
        return ResponseEntity.ok(ApiResponse.ok("Libro actualizado", libroService.actualizar(id, libro)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> eliminar(@PathVariable Long id) {
        libroService.eliminar(id);
        return ResponseEntity.ok(ApiResponse.ok("Libro eliminado", null));
    }
}
