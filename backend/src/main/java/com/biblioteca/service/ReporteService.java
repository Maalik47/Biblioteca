package com.biblioteca.service;

import com.biblioteca.dto.ConteoNombre;
import com.biblioteca.entity.Libro;
import com.biblioteca.entity.Prestamo;
import com.biblioteca.repository.LibroRepository;
import com.biblioteca.repository.PrestamoRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ReporteService {

    private final LibroRepository libroRepository;
    private final PrestamoRepository prestamoRepository;

    public ReporteService(LibroRepository libroRepository, PrestamoRepository prestamoRepository) {
        this.libroRepository = libroRepository;
        this.prestamoRepository = prestamoRepository;
    }

    public Map<String, Object> resumen() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("librosMasPrestados", rankingLibros());
        Map<String, Long> porEstado = new LinkedHashMap<>();
        porEstado.put("activos", prestamoRepository.countByEstado(PrestamoService.PENDIENTE));
        porEstado.put("devueltos", prestamoRepository.countByEstado(PrestamoService.DEVUELTO));
        data.put("prestamosPorEstado", porEstado);
        data.put("librosPorCategoria", librosPorCategoria());
        return data;
    }

    public List<ConteoNombre> rankingLibros() {
        return prestamoRepository.rankingLibros(PageRequest.of(0, 10)).stream()
                .map(row -> new ConteoNombre((String) row[0], ((Number) row[1]).longValue()))
                .toList();
    }

    public List<ConteoNombre> librosPorCategoria() {
        return libroRepository.findAll().stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        l -> l.getCategoria() == null || l.getCategoria().isBlank() ? "Sin categoría" : l.getCategoria(),
                        java.util.stream.Collectors.counting()))
                .entrySet().stream()
                .map(e -> new ConteoNombre(e.getKey(), e.getValue()))
                .sorted((a, b) -> Long.compare(b.valor(), a.valor()))
                .toList();
    }

    public String librosCsv() {
        StringBuilder sb = new StringBuilder();
        sb.append("ID;Titulo;Autor;ISBN;Categoria;Anio;Editorial;Cantidad;Disponibles;Estado\n");
        for (Libro l : libroRepository.findAll()) {
            sb.append(campo(l.getId())).append(';')
                    .append(campo(l.getTitulo())).append(';')
                    .append(campo(l.getAutor())).append(';')
                    .append(campo(l.getIsbn())).append(';')
                    .append(campo(l.getCategoria())).append(';')
                    .append(campo(l.getAnioPublicacion())).append(';')
                    .append(campo(l.getEditorial())).append(';')
                    .append(campo(l.getCantidad())).append(';')
                    .append(campo(l.getDisponibles())).append(';')
                    .append(campo(l.getEstado())).append('\n');
        }
        return sb.toString();
    }

    public String prestamosCsv() {
        StringBuilder sb = new StringBuilder();
        sb.append("ID;Lector;Codigo;Libro;Ejemplar;FechaPrestamo;FechaLimite;FechaDevolucion;Estado;Renovaciones\n");
        for (Prestamo p : prestamoRepository.listarCompleto()) {
            String lector = p.getUsuario().getNombre()
                    + (p.getUsuario().getApellido() != null ? " " + p.getUsuario().getApellido() : "");
            sb.append(campo(p.getId())).append(';')
                    .append(campo(lector)).append(';')
                    .append(campo(p.getUsuario().getCodigoLector())).append(';')
                    .append(campo(p.getLibro().getTitulo())).append(';')
                    .append(campo(p.getEjemplar() != null ? p.getEjemplar().getCodigo() : "")).append(';')
                    .append(campo(p.getFechaPrestamo())).append(';')
                    .append(campo(p.getFechaLimite())).append(';')
                    .append(campo(p.getFechaDevolucion())).append(';')
                    .append(campo(p.getEstado())).append(';')
                    .append(campo(p.getRenovaciones())).append('\n');
        }
        return sb.toString();
    }

    private String campo(Object valor) {
        if (valor == null) return "";
        String s = valor.toString().replace(";", ",");
        return s;
    }
}
