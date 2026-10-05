package com.biblioteca.service;

import com.biblioteca.entity.Libro;
import com.biblioteca.repository.LibroRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LibroService {

    private final LibroRepository libroRepository;

    public LibroService(LibroRepository libroRepository) {
        this.libroRepository = libroRepository;
    }

    public List<Libro> listar() {
        return libroRepository.findAll();
    }

    public Libro obtener(Long id) {
        return buscar(id);
    }

    public Libro crear(Libro libro) {
        if (libro.getTitulo() == null || libro.getTitulo().isBlank()) {
            throw new RuntimeException("El título es obligatorio");
        }
        if (libro.getIsbn() != null && !libro.getIsbn().isBlank()
                && libroRepository.findByIsbn(libro.getIsbn().trim()).isPresent()) {
            throw new RuntimeException("Ya existe un libro con ese ISBN");
        }
        libro.setId(null);
        libro.setIsbn(libro.getIsbn() == null ? null : libro.getIsbn().trim());
        libro.setCantidad(libro.getCantidad() == null   ? 1 : libro.getCantidad());
        libro.setDisponibles(libro.getCantidad());
        return libroRepository.save(libro);
    }

    public Libro actualizar(Long id, Libro libro) {
        Libro existente = buscar(id);
        if (libro.getIsbn() != null && !libro.getIsbn().isBlank()) {
            String nuevoIsbn = libro.getIsbn().trim();
            libroRepository.findByIsbn(nuevoIsbn).ifPresent(other -> {
                if (!other.getId().equals(id)) {
                    throw new RuntimeException("Ya existe un libro con ese ISBN");
                }
            });
            existente.setIsbn(nuevoIsbn);
        }
        if (libro.getTitulo() != null && !libro.getTitulo().isBlank()) {
            existente.setTitulo(libro.getTitulo());
        }
        if (libro.getAutor() != null && !libro.getAutor().isBlank()) {
            existente.setAutor(libro.getAutor());
        }
        existente.setCategoria(libro.getCategoria());
        existente.setAnioPublicacion(libro.getAnioPublicacion());

        int cantidadAnterior = existente.getCantidad() == null ? 0 : existente.getCantidad();
        int cantidadNueva = libro.getCantidad() == null ? cantidadAnterior : libro.getCantidad();
        existente.setCantidad(cantidadNueva);

        int disponiblesAnterior = existente.getDisponibles() == null ? 0 : existente.getDisponibles();
        int delta = cantidadNueva - cantidadAnterior;
        existente.setDisponibles(Math.max(0, disponiblesAnterior + delta));

        return libroRepository.save(existente);
    }

    public void eliminar(Long id) {
        buscar(id);
        libroRepository.deleteById(id);
    }

    private Libro buscar(Long id) {
        return libroRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Libro no encontrado"));
    }
}