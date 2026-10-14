package com.biblioteca.dto;

import java.util.List;

public record DashboardDto(long totalLibros,
                           long totalEjemplares,
                           long ejemplaresDisponibles,
                           long totalLectores,
                           long lectoresActivos,
                           long prestamosActivos,
                           long prestamosVencidos,
                           long reservasPendientes,
                           long prestamosDevueltos,
                           List<ConteoNombre> librosPorCategoria,
                           List<PrestamoResponse> ultimosPrestamos,
                           List<AuditoriaDto> ultimaActividad) {
}
