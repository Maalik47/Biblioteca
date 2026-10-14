package com.biblioteca.service;

import com.biblioteca.dto.ReservaDto;
import com.biblioteca.entity.Ejemplar;
import com.biblioteca.entity.Libro;
import com.biblioteca.entity.Reserva;
import com.biblioteca.entity.Usuario;
import com.biblioteca.exception.ConflictoException;
import com.biblioteca.exception.RecursoNoEncontradoException;
import com.biblioteca.exception.SolicitudInvalidaException;
import com.biblioteca.repository.EjemplarRepository;
import com.biblioteca.repository.LibroRepository;
import com.biblioteca.repository.ReservaRepository;
import com.biblioteca.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class ReservaService {

    public static final String PENDIENTE = "PENDIENTE";
    public static final String DISPONIBLE = "DISPONIBLE";
    public static final String CUMPLIDA = "CUMPLIDA";
    public static final String CANCELADA = "CANCELADA";

    private static final int DIAS_RETENCION = 3;

    private final ReservaRepository reservaRepository;
    private final LibroRepository libroRepository;
    private final UsuarioRepository usuarioRepository;
    private final EjemplarRepository ejemplarRepository;
    private final AuditoriaService auditoriaService;

    public ReservaService(ReservaRepository reservaRepository,
                          LibroRepository libroRepository,
                          UsuarioRepository usuarioRepository,
                          EjemplarRepository ejemplarRepository,
                          AuditoriaService auditoriaService) {
        this.reservaRepository = reservaRepository;
        this.libroRepository = libroRepository;
        this.usuarioRepository = usuarioRepository;
        this.ejemplarRepository = ejemplarRepository;
        this.auditoriaService = auditoriaService;
    }

    public List<ReservaDto> listar(String estado) {
        List<Reserva> lista = (estado == null || estado.isBlank())
                ? reservaRepository.findAll()
                : reservaRepository.findByEstadoOrderByFechaSolicitudAsc(estado.toUpperCase());
        return lista.stream().map(ReservaDto::from).toList();
    }

    public List<ReservaDto> mios(String username) {
        Usuario u = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));
        return reservaRepository.findByUsuarioIdOrderByFechaSolicitudDesc(u.getId())
                .stream().map(ReservaDto::from).toList();
    }

    public long contarPendientes() {
        return reservaRepository.countByEstado(PENDIENTE);
    }

    @Transactional
    public ReservaDto crear(Long libroId, Long usuarioId, String usernameSesion) {
        Usuario usuario = resolverUsuario(usuarioId, usernameSesion);
        if (!Boolean.TRUE.equals(usuario.getActivo())) {
            throw new ConflictoException("El lector está inactivo");
        }
        Libro libro = libroRepository.findById(libroId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Libro no encontrado"));
        if (libro.getDisponibles() != null && libro.getDisponibles() > 0) {
            throw new ConflictoException("Hay ejemplares disponibles: realiza el préstamo directamente");
        }
        if (reservaRepository.existsByUsuarioIdAndLibroIdAndEstadoIn(
                usuario.getId(), libroId, List.of(PENDIENTE, DISPONIBLE))) {
            throw new ConflictoException("Ya tienes una reserva activa de este libro");
        }
        int posicion = reservaRepository
                .findByLibroIdAndEstadoOrderByFechaSolicitudAsc(libroId, PENDIENTE).size() + 1;

        Reserva reserva = new Reserva();
        reserva.setUsuario(usuario);
        reserva.setLibro(libro);
        reserva.setEstado(PENDIENTE);
        reserva.setPosicion(posicion);
        Reserva guardada = reservaRepository.save(reserva);
        auditoriaService.registrar("RESERVA", "Reserva", guardada.getId(),
                "Reserva de '" + libro.getTitulo() + "' por " + nombre(usuario) + " (posición " + posicion + ")");
        return ReservaDto.from(guardada);
    }

    @Transactional
    public ReservaDto cancelar(Long id) {
        Reserva reserva = reservaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Reserva no encontrada"));
        if (CUMPLIDA.equals(reserva.getEstado()) || CANCELADA.equals(reserva.getEstado())) {
            throw new ConflictoException("La reserva ya está " + reserva.getEstado().toLowerCase());
        }
        reserva.setEstado(CANCELADA);
        Reserva guardada = reservaRepository.save(reserva);
        auditoriaService.registrar("CANCELAR", "Reserva", id, "Cancelación de reserva");
        return ReservaDto.from(guardada);
    }

    /**
     * Al devolverse un ejemplar, si hay lista de espera se retiene el ejemplar
     * para el primero de la cola; en caso contrario queda disponible.
     */
    @Transactional
    public void promoverSiguiente(Long libroId, Ejemplar ejemplar) {
        var siguiente = reservaRepository
                .findFirstByLibroIdAndEstadoOrderByFechaSolicitudAsc(libroId, PENDIENTE);
        if (siguiente.isPresent()) {
            Reserva reserva = siguiente.get();
            reserva.setEstado(DISPONIBLE);
            reserva.setFechaExpiracion(LocalDate.now().plusDays(DIAS_RETENCION));
            reservaRepository.save(reserva);
            ejemplar.setEstado(EjemplarService.RESERVADO);
        } else {
            ejemplar.setEstado(EjemplarService.DISPONIBLE);
        }
        ejemplarRepository.save(ejemplar);
    }

    /** Marca como cumplida la reserva del lector cuando retira el libro. */
    @Transactional
    public void cumplir(Long reservaId) {
        Reserva reserva = reservaRepository.findById(reservaId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Reserva no encontrada"));
        reserva.setEstado(CUMPLIDA);
        reservaRepository.save(reserva);
    }

    private Usuario resolverUsuario(Long usuarioId, String usernameSesion) {
        if (usuarioId != null) {
            return usuarioRepository.findById(usuarioId)
                    .orElseThrow(() -> new RecursoNoEncontradoException("Lector no encontrado"));
        }
        if (usernameSesion == null || usernameSesion.isBlank()) {
            throw new SolicitudInvalidaException("No se pudo determinar el lector");
        }
        return usuarioRepository.findByUsername(usernameSesion)
                .orElseThrow(() -> new RecursoNoEncontradoException("Lector no encontrado"));
    }

    private String nombre(Usuario u) {
        return u.getNombre() + (u.getApellido() != null && !u.getApellido().isBlank() ? " " + u.getApellido() : "");
    }
}
