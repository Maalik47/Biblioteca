package com.biblioteca.dto;

import com.biblioteca.entity.Ejemplar;

import java.time.LocalDateTime;

public record EjemplarDto(Long id, Long libroId, String libroTitulo, String codigo,
                          String ubicacion, String estado, LocalDateTime fechaAlta) {

    public static EjemplarDto from(Ejemplar e) {
        return new EjemplarDto(
                e.getId(),
                e.getLibro() != null ? e.getLibro().getId() : null,
                e.getLibro() != null ? e.getLibro().getTitulo() : null,
                e.getCodigo(), e.getUbicacion(), e.getEstado(), e.getFechaAlta());
    }
}
