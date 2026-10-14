package com.biblioteca.service;

import com.biblioteca.dto.PaginaResponse;
import com.biblioteca.dto.PrestamoRequest;
import com.biblioteca.dto.PrestamoResponse;
import com.biblioteca.entity.Ejemplar;
import com.biblioteca.entity.Libro;
import com.biblioteca.entity.Prestamo;
import com.biblioteca.entity.Usuario;
import com.biblioteca.exception.ConflictoException;
import com.biblioteca.exception.RecursoNoEncontradoException;
import com.biblioteca.exception.SolicitudInvalidaException;
import com.biblioteca.repository.EjemplarRepository;
import com.biblioteca.repository.LibroRepository;
import com.biblioteca.repository.PrestamoRepository;
import com.biblioteca.repository.UsuarioRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class PrestamoService {

    public static final String PENDIENTE = "PENDIENTE";
    public static final String DEVUELTO = "DEVUELTO";
    private static final int DIAS_PRESTAMO = 15;
    private static final int MAX_RENOVACIONES = 2;
    private static final int MAX_PRESTAMOS_ACTIVOS = 3;

    private final PrestamoRepository prestamoRepository;
    private final UsuarioRepository usuarioRepository;
    private final LibroRepository libroRepository;
    private final EjemplarRepository ejemplarRepository;
    private final EjemplarService ejemplarService;
    private final ReservaService reservaService;
    private final AuditoriaService auditoriaService;

    public PrestamoService(PrestamoRepository prestamoRepository,
                           UsuarioRepository usuarioRepository,
                           LibroRepository libroRepository,
                           EjemplarRepository ejemplarRepository,
                           EjemplarService ejemplarService,
                           ReservaService reservaService,
                           AuditoriaService auditoriaService) {
        this.prestamoRepository = prestamoRepository;
        this.usuarioRepository = usuarioRepository;
        this.libroRepository = libroRepository;
        this.ejemplarRepository = ejemplarRepository;
        this.ejemplarService = ejemplarService;
        this.reservaService = reservaService;
        this.auditoriaService = auditoriaService;
    }

    public List<PrestamoResponse> listar() {
        return prestamoRepository.listarCompleto().stream().map(this::toDto).toList();
    }

    public PaginaResponse<PrestamoResponse> buscar(String q, String estado, Long usuarioId,
                                                   int pagina, int tamano) {
        var page = prestamoRepository.buscar(normalizar(q), normalizar(estado), usuarioId,
                PageRequest.of(Math.max(0, pagina), Math.max(1, Math.min(tamano, 50))));
        return PaginaResponse.desde(page, this::toDto);
    }

    public PrestamoResponse obtener(Long id) {
        return toDto(buscar(id));
    }

    public List<PrestamoResponse> mios(String username) {
        Usuario u = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));
        return prestamoRepository.findByUsuarioIdOrderByFechaPrestamoDesc(u.getId())
                .stream().map(this::toDto).toList();
    }

    public List<PrestamoResponse> vencidos() {
        return prestamoRepository.listarActivos().stream()
                .map(this::toDto)
                .filter(PrestamoResponse::vencido)
                .toList();
    }

    @Transactional
    public PrestamoResponse crear(PrestamoRequest request) {
        if (request.usuarioId() == null || request.libroId() == null) {
            throw new SolicitudInvalidaException("Debes indicar el lector y el libro");
        }
        Usuario usuario = usuarioRepository.findById(request.usuarioId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Lector no encontrado"));
        if (!Boolean.TRUE.equals(usuario.getActivo())) {
            throw new ConflictoException("El lector está inactivo");
        }
        if (usuario.getSuspendidoHasta() != null && usuario.getSuspendidoHasta().isAfter(LocalDate.now())) {
            throw new ConflictoException("El lector está suspendido hasta " + usuario.getSuspendidoHasta());
        }
        if (prestamoRepository.countByUsuarioIdAndEstado(usuario.getId(), PENDIENTE) >= MAX_PRESTAMOS_ACTIVOS) {
            throw new ConflictoException("El lector alcanzó el límite de " + MAX_PRESTAMOS_ACTIVOS + " préstamos activos");
        }
        Libro libro = libroRepository.findById(request.libroId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Libro no encontrado"));
        if (prestamoRepository.existsByUsuarioIdAndLibroIdAndEstado(usuario.getId(), libro.getId(), PENDIENTE)) {
            throw new ConflictoException("El lector ya tiene un préstamo activo de este libro");
        }

        Ejemplar ejemplar = seleccionarEjemplar(libro, request.ejemplarId());
        ejemplar.setEstado(EjemplarService.PRESTADO);
        ejemplarRepository.save(ejemplar);

        Prestamo prestamo = new Prestamo();
        prestamo.setUsuario(usuario);
        prestamo.setLibro(libro);
        prestamo.setEjemplar(ejemplar);
        prestamo.setFechaPrestamo(request.fechaPrestamo() != null ? request.fechaPrestamo() : LocalDate.now());
        prestamo.setFechaLimite(request.fechaLimite() != null ? request.fechaLimite() : LocalDate.now().plusDays(DIAS_PRESTAMO));
        prestamo.setObservaciones(request.observaciones());
        prestamo.setEstado(PENDIENTE);
        Prestamo guardado = prestamoRepository.save(prestamo);

        sincronizarDisponibles(libro);
        auditoriaService.registrar("PRESTAMO", "Prestamo", guardado.getId(),
                "Préstamo de '" + libro.getTitulo() + "' a " + nombre(usuario) + " (ejemplar " + ejemplar.getCodigo() + ")");
        return toDto(guardado);
    }

    @Transactional
    public PrestamoResponse actualizar(Long id, PrestamoRequest request) {
        Prestamo prestamo = buscar(id);
        if (request.estado() != null && !request.estado().isBlank()
                && !request.estado().equalsIgnoreCase(prestamo.getEstado())) {
            throw new SolicitudInvalidaException(
                    "El estado no se modifica directamente; usa devolver o renovar");
        }
        if (request.fechaLimite() != null) {
            prestamo.setFechaLimite(request.fechaLimite());
        }
        if (request.observaciones() != null) {
            prestamo.setObservaciones(request.observaciones());
        }
        return toDto(prestamoRepository.save(prestamo));
    }

    @Transactional
    public PrestamoResponse devolver(Long id) {
        Prestamo prestamo = buscar(id);
        if (DEVUELTO.equals(prestamo.getEstado())) {
            throw new ConflictoException("El préstamo ya fue devuelto");
        }
        prestamo.setEstado(DEVUELTO);
        prestamo.setFechaDevolucion(LocalDate.now());

        if (prestamo.getEjemplar() != null) {
            reservaService.promoverSiguiente(prestamo.getLibro().getId(), prestamo.getEjemplar());
        }
        prestamoRepository.save(prestamo);
        sincronizarDisponibles(prestamo.getLibro());
        auditoriaService.registrar("DEVOLUCION", "Prestamo", id,
                "Devolución de '" + prestamo.getLibro().getTitulo() + "' por " + nombre(prestamo.getUsuario()));
        return toDto(prestamo);
    }

    @Transactional
    public PrestamoResponse renovar(Long id) {
        Prestamo prestamo = buscar(id);
        if (DEVUELTO.equals(prestamo.getEstado())) {
            throw new ConflictoException("No se puede renovar un préstamo ya devuelto");
        }
        if (prestamo.getFechaLimite().isBefore(LocalDate.now())) {
            throw new ConflictoException("No se puede renovar un préstamo vencido");
        }
        if (prestamo.getRenovaciones() != null && prestamo.getRenovaciones() >= MAX_RENOVACIONES) {
            throw new ConflictoException("Se alcanzó el máximo de " + MAX_RENOVACIONES + " renovaciones");
        }
        if (prestamo.getLibro().getDisponibles() != null && prestamo.getLibro().getDisponibles() <= 0) {
            throw new ConflictoException("No se puede renovar: hay lista de espera para este libro");
        }
        prestamo.setFechaLimite(prestamo.getFechaLimite().plusDays(DIAS_PRESTAMO));
        prestamo.setRenovaciones((prestamo.getRenovaciones() == null ? 0 : prestamo.getRenovaciones()) + 1);
        Prestamo guardado = prestamoRepository.save(prestamo);
        auditoriaService.registrar("RENOVACION", "Prestamo", id,
                "Renovación de '" + prestamo.getLibro().getTitulo() + "' (nueva fecha " + guardado.getFechaLimite() + ")");
        return toDto(guardado);
    }

    @Transactional
    public void eliminar(Long id) {
        Prestamo prestamo = buscar(id);
        if (PENDIENTE.equals(prestamo.getEstado()) && prestamo.getEjemplar() != null) {
            Ejemplar e = prestamo.getEjemplar();
            e.setEstado(EjemplarService.DISPONIBLE);
            ejemplarRepository.save(e);
            sincronizarDisponibles(prestamo.getLibro());
        }
        prestamoRepository.delete(prestamo);
        auditoriaService.registrar("ELIMINAR", "Prestamo", id, "Eliminación de préstamo");
    }

    private Ejemplar seleccionarEjemplar(Libro libro, Long ejemplarId) {
        if (ejemplarId != null) {
            Ejemplar e = ejemplarRepository.findById(ejemplarId)
                    .orElseThrow(() -> new RecursoNoEncontradoException("Ejemplar no encontrado"));
            if (!e.getLibro().getId().equals(libro.getId())) {
                throw new SolicitudInvalidaException("El ejemplar no pertenece al libro indicado");
            }
            if (!EjemplarService.DISPONIBLE.equals(e.getEstado())) {
                throw new ConflictoException("El ejemplar seleccionado no está disponible");
            }
            return e;
        }
        return ejemplarRepository.findFirstByLibroIdAndEstadoOrderByCodigoAsc(libro.getId(), EjemplarService.DISPONIBLE)
                .orElseThrow(() -> new ConflictoException(
                        "No hay ejemplares disponibles. Puedes registrar una reserva para este libro"));
    }

    private void sincronizarDisponibles(Libro libro) {
        libro.setDisponibles((int) ejemplarService.contarDisponibles(libro.getId()));
        libroRepository.save(libro);
    }

    private Prestamo buscar(Long id) {
        return prestamoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Préstamo no encontrado"));
    }

    private PrestamoResponse toDto(Prestamo p) {
        boolean vencido = PENDIENTE.equals(p.getEstado())
                && p.getFechaLimite() != null
                && p.getFechaLimite().isBefore(LocalDate.now());
        long diasRetraso = vencido
                ? java.time.temporal.ChronoUnit.DAYS.between(p.getFechaLimite(), LocalDate.now())
                : 0;
        return new PrestamoResponse(
                p.getId(),
                p.getUsuario().getId(),
                nombre(p.getUsuario()),
                p.getUsuario().getCodigoLector(),
                p.getLibro().getId(),
                p.getLibro().getTitulo(),
                p.getEjemplar() != null ? p.getEjemplar().getId() : null,
                p.getEjemplar() != null ? p.getEjemplar().getCodigo() : null,
                p.getFechaPrestamo(),
                p.getFechaLimite(),
                p.getFechaDevolucion(),
                p.getEstado(),
                vencido,
                diasRetraso,
                p.getRenovaciones() == null ? 0 : p.getRenovaciones(),
                p.getObservaciones());
    }

    private String nombre(Usuario u) {
        return u.getNombre() + (u.getApellido() != null && !u.getApellido().isBlank() ? " " + u.getApellido() : "");
    }

    private String normalizar(String valor) {
        return (valor == null || valor.isBlank()) ? null : valor.trim();
    }
}
