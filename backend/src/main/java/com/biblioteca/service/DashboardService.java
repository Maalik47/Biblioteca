package com.biblioteca.service;

import com.biblioteca.dto.AuditoriaDto;
import com.biblioteca.dto.ConteoNombre;
import com.biblioteca.dto.DashboardDto;
import com.biblioteca.entity.Libro;
import com.biblioteca.repository.EjemplarRepository;
import com.biblioteca.repository.LibroRepository;
import com.biblioteca.repository.PrestamoRepository;
import com.biblioteca.repository.ReservaRepository;
import com.biblioteca.repository.UsuarioRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    private final LibroRepository libroRepository;
    private final EjemplarRepository ejemplarRepository;
    private final UsuarioRepository usuarioRepository;
    private final PrestamoRepository prestamoRepository;
    private final ReservaRepository reservaRepository;
    private final PrestamoService prestamoService;
    private final AuditoriaService auditoriaService;

    public DashboardService(LibroRepository libroRepository,
                            EjemplarRepository ejemplarRepository,
                            UsuarioRepository usuarioRepository,
                            PrestamoRepository prestamoRepository,
                            ReservaRepository reservaRepository,
                            PrestamoService prestamoService,
                            AuditoriaService auditoriaService) {
        this.libroRepository = libroRepository;
        this.ejemplarRepository = ejemplarRepository;
        this.usuarioRepository = usuarioRepository;
        this.prestamoRepository = prestamoRepository;
        this.reservaRepository = reservaRepository;
        this.prestamoService = prestamoService;
        this.auditoriaService = auditoriaService;
    }

    public DashboardDto obtener() {
        long vencidos = prestamoRepository
                .findByEstadoAndFechaLimiteBefore(PrestamoService.PENDIENTE, LocalDate.now()).size();

        List<ConteoNombre> porCategoria = libroRepository.findAll().stream()
                .collect(Collectors.groupingBy(
                        l -> l.getCategoria() == null || l.getCategoria().isBlank() ? "Sin categoría" : l.getCategoria(),
                        Collectors.counting()))
                .entrySet().stream()
                .map(e -> new ConteoNombre(e.getKey(), e.getValue()))
                .sorted((a, b) -> Long.compare(b.valor(), a.valor()))
                .toList();

        List<AuditoriaDto> actividad = auditoriaService.ultimas().stream().map(AuditoriaDto::from).toList();

        return new DashboardDto(
                libroRepository.count(),
                ejemplarRepository.count(),
                ejemplarRepository.countByEstado(EjemplarService.DISPONIBLE),
                usuarioRepository.count(),
                usuarioRepository.countByActivo(true),
                prestamoRepository.countByEstado(PrestamoService.PENDIENTE),
                vencidos,
                reservaRepository.countByEstado(ReservaService.PENDIENTE),
                prestamoRepository.countByEstado(PrestamoService.DEVUELTO),
                porCategoria,
                prestamoService.listar().stream().limit(5).toList(),
                actividad);
    }
}
