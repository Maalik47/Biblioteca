package com.biblioteca.dto;

import com.biblioteca.entity.Reserva;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record ReservaDto(Long id, Long usuarioId, String usuarioNombre, String usuarioCodigo,
                         Long libroId, String libroTitulo, LocalDateTime fechaSolicitud,
                         LocalDate fechaExpiracion, String estado, Integer posicion) {

    public static ReservaDto from(Reserva r) {
        String nombre = r.getUsuario().getNombre()
                + (r.getUsuario().getApellido() != null && !r.getUsuario().getApellido().isBlank()
                        ? " " + r.getUsuario().getApellido() : "");
        return new ReservaDto(
                r.getId(),
                r.getUsuario().getId(),
                nombre,
                r.getUsuario().getCodigoLector(),
                r.getLibro().getId(),
                r.getLibro().getTitulo(),
                r.getFechaSolicitud(),
                r.getFechaExpiracion(),
                r.getEstado(),
                r.getPosicion());
    }
}
