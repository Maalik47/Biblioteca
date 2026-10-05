package com.biblioteca.dto;

import java.time.LocalDate;

public record PrestamoResponse(Long id, Long usuarioId, String usuarioNombre, Long libroId,
                               String libroTitulo, LocalDate fechaPrestamo, LocalDate fechaLimite,
                               LocalDate fechaDevolucion, String estado) {
}