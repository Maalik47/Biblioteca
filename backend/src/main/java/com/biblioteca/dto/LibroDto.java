package com.biblioteca.dto;

import com.biblioteca.entity.Libro;

import java.time.LocalDateTime;

public record LibroDto(Long id, String titulo, String autor, String isbn, String categoria,
                       Integer anioPublicacion, Integer cantidad, Integer disponibles,
                       String editorial, String descripcion, String ubicacion, String portadaUrl,
                       String estado, LocalDateTime fechaRegistro) {

    public static LibroDto from(Libro l) {
        return new LibroDto(
                l.getId(), l.getTitulo(), l.getAutor(), l.getIsbn(), l.getCategoria(),
                l.getAnioPublicacion(), l.getCantidad(), l.getDisponibles(),
                l.getEditorial(), l.getDescripcion(), l.getUbicacion(), l.getPortadaUrl(),
                l.getEstado(), l.getFechaRegistro());
    }
}
