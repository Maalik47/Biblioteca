package com.biblioteca.dto;

import java.time.LocalDate;

public record PrestamoResponse(Long id, Long usuarioId, String usuarioNombre, String usuarioCodigo,
                               Long libroId, String libroTitulo, Long ejemplarId, String ejemplarCodigo,
                               LocalDate fechaPrestamo, LocalDate fechaLimite, LocalDate fechaDevolucion,
                               String estado, boolean vencido, long diasRetraso, int renovaciones,
                               String observaciones) {
}
