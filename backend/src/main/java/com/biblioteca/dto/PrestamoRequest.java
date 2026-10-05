package com.biblioteca.dto;

import java.time.LocalDate;

public record PrestamoRequest(Long usuarioId, Long libroId, LocalDate fechaPrestamo,
                              LocalDate fechaLimite, String estado) {
}