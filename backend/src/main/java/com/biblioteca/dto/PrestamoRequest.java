package com.biblioteca.dto;

import java.time.LocalDate;

/**
 * Peticion de prestamo. El campo estado se mantiene por compatibilidad pero
 * las transiciones de estado solo se hacen por los endpoints de accion
 * (devolver / renovar) para evitar inconsistencias.
 */
public record PrestamoRequest(Long usuarioId, Long libroId, Long ejemplarId,
                              LocalDate fechaPrestamo, LocalDate fechaLimite,
                              String observaciones, String estado) {
}
