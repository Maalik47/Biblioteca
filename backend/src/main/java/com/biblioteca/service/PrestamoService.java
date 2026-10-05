package com.biblioteca.service;

import com.biblioteca.dto.PrestamoRequest;
import com.biblioteca.dto.PrestamoResponse;
import com.biblioteca.entity.Libro;
import com.biblioteca.entity.Prestamo;
import com.biblioteca.entity.Usuario;
import com.biblioteca.repository.LibroRepository;
import com.biblioteca.repository.PrestamoRepository;
import com.biblioteca.repository.UsuarioRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class PrestamoService {

    private final PrestamoRepository prestamoRepository;
    private final UsuarioRepository usuarioRepository;
    private final LibroRepository libroRepository;

    public PrestamoService(PrestamoRepository prestamoRepository,
                           UsuarioRepository usuarioRepository,
                           LibroRepository libroRepository) {
        this.prestamoRepository = prestamoRepository;
        this.usuarioRepository = usuarioRepository;
        this.libroRepository = libroRepository;
    }

    public List<PrestamoResponse> listar() {
        return prestamoRepository.findAll(Sort.by(Sort.Direction.DESC, "id"))
                .stream().map(this::toDto).toList();
    }

    public PrestamoResponse obtener(Long id) {
        return toDto(buscar(id));
    }

    @Transactional
    public PrestamoResponse crear(PrestamoRequest request) {
        Usuario usuario = usuarioRepository.findById(request.usuarioId())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        if (!Boolean.TRUE.equals(usuario.getActivo())) {
            throw new RuntimeException("El usuario está inactivo");
        }
        Libro libro = libroRepository.findById(request.libroId())
                .orElseThrow(() -> new RuntimeException("Libro no encontrado"));
        if (libro.getDisponibles() == null || libro.getDisponibles() <= 0) {
            throw new RuntimeException("No hay ejemplares disponibles de este libro");
        }

        libro.setDisponibles(libro.getDisponibles() - 1);
        libroRepository.save(libro);

        Prestamo prestamo = new Prestamo();
        prestamo.setUsuario(usuario);
        prestamo.setLibro(libro);
        prestamo.setFechaPrestamo(request.fechaPrestamo() != null ? request.fechaPrestamo() : LocalDate.now());
        prestamo.setFechaLimite(request.fechaLimite() != null ? request.fechaLimite() : LocalDate.now().plusDays(15));
        prestamo.setEstado("PENDIENTE");
        return toDto(prestamoRepository.save(prestamo));
    }

    @Transactional
    public PrestamoResponse actualizar(Long id, PrestamoRequest request) {
        Prestamo prestamo = buscar(id);
        if (request.fechaPrestamo() != null) {
            prestamo.setFechaPrestamo(request.fechaPrestamo());
        }
        if (request.fechaLimite() != null) {
            prestamo.setFechaLimite(request.fechaLimite());
        }

        String estadoAnterior = prestamo.getEstado();
        String estadoNuevo = request.estado();
        if (estadoNuevo != null && !estadoNuevo.equals(estadoAnterior)) {
            if ("DEVUELTO".equals(estadoNuevo)) {
                prestamo.setFechaDevolucion(LocalDate.now());
                Libro libro = prestamo.getLibro();
                libro.setDisponibles(libro.getDisponibles() + 1);
                libroRepository.save(libro);
            } else if ("PENDIENTE".equals(estadoNuevo) && "DEVUELTO".equals(estadoAnterior)) {
                Libro libro = prestamo.getLibro();
                if (libro.getDisponibles() == null || libro.getDisponibles() <= 0) {
                    throw new RuntimeException("No hay ejemplares disponibles de este libro");
                }
                libro.setDisponibles(libro.getDisponibles() - 1);
                libroRepository.save(libro);
                prestamo.setFechaDevolucion(null);
            }
            prestamo.setEstado(estadoNuevo);
        }
        return toDto(prestamoRepository.save(prestamo));
    }

    @Transactional
    public PrestamoResponse devolver(Long id) {
        Prestamo prestamo = buscar(id);
        if ("DEVUELTO".equals(prestamo.getEstado())) {
            throw new RuntimeException("El préstamo ya fue devuelto");
        }
        return actualizar(id, new PrestamoRequest(null, null, null, null, "DEVUELTO"));
    }

    @Transactional
    public void eliminar(Long id) {
        Prestamo prestamo = buscar(id);
        if ("PENDIENTE".equals(prestamo.getEstado())) {
            Libro libro = prestamo.getLibro();
            libro.setDisponibles(libro.getDisponibles() + 1);
            libroRepository.save(libro);
        }
        prestamoRepository.delete(prestamo);
    }

    private Prestamo buscar(Long id) {
        return prestamoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Préstamo no encontrado"));
    }

    private PrestamoResponse toDto(Prestamo prestamo) {
        Usuario usuario = prestamo.getUsuario();
        String nombreUsuario = usuario.getNombre()
                + (usuario.getApellido() != null && !usuario.getApellido().isBlank()
                        ? " " + usuario.getApellido() : "");
        return new PrestamoResponse(
                prestamo.getId(),
                usuario.getId(),
                nombreUsuario,
                prestamo.getLibro().getId(),
                prestamo.getLibro().getTitulo(),
                prestamo.getFechaPrestamo(),
                prestamo.getFechaLimite(),
                prestamo.getFechaDevolucion(),
                prestamo.getEstado());
    }
}