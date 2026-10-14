package com.biblioteca.service;

import com.biblioteca.dto.EjemplarDto;
import com.biblioteca.entity.Ejemplar;
import com.biblioteca.entity.Libro;
import com.biblioteca.exception.ConflictoException;
import com.biblioteca.exception.RecursoNoEncontradoException;
import com.biblioteca.exception.SolicitudInvalidaException;
import com.biblioteca.repository.EjemplarRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class EjemplarService {

    public static final String DISPONIBLE = "DISPONIBLE";
    public static final String PRESTADO = "PRESTADO";
    public static final String RESERVADO = "RESERVADO";

    private final EjemplarRepository ejemplarRepository;

    public EjemplarService(EjemplarRepository ejemplarRepository) {
        this.ejemplarRepository = ejemplarRepository;
    }

    public List<EjemplarDto> listarPorLibro(Long libroId) {
        return ejemplarRepository.findByLibroIdOrderByCodigoAsc(libroId)
                .stream().map(EjemplarDto::from).toList();
    }

    public long contarDisponibles(Long libroId) {
        return ejemplarRepository.countByLibroIdAndEstado(libroId, DISPONIBLE);
    }

    /**
     * Genera automaticamente {@code cantidad} ejemplares DISPONIBLES para un libro
     * y devuelve el numero de ejemplares creados.
     */
    @Transactional
    public int generar(Libro libro, int cantidad) {
        int creados = 0;
        for (int i = 1; i <= cantidad; i++) {
            Ejemplar e = new Ejemplar();
            e.setLibro(libro);
            e.setUbicacion(libro.getUbicacion());
            e.setEstado(DISPONIBLE);
            e.setCodigo(siguienteCodigo(libro.getId(), i));
            ejemplarRepository.save(e);
            creados++;
        }
        return creados;
    }

    @Transactional
    public void eliminarDisponiblesSobrantes(Long libroId, int cuantos) {
        List<Ejemplar> disponibles = ejemplarRepository.findByLibroIdAndEstadoOrderByCodigoAsc(libroId, DISPONIBLE);
        if (disponibles.size() < cuantos) {
            throw new ConflictoException(
                    "No se puede reducir la cantidad: hay ejemplares prestados o reservados");
        }
        for (int i = 0; i < cuantos; i++) {
            ejemplarRepository.delete(disponibles.get(disponibles.size() - 1 - i));
        }
    }

    @Transactional
    public EjemplarDto crear(Long libroId, Libro libro, String ubicacion) {
        if (libro == null) {
            throw new RecursoNoEncontradoException("Libro no encontrado");
        }
        Ejemplar e = new Ejemplar();
        e.setLibro(libro);
        e.setUbicacion(ubicacion != null && !ubicacion.isBlank() ? ubicacion : libro.getUbicacion());
        e.setEstado(DISPONIBLE);
        e.setCodigo(siguienteCodigo(libroId, (int) ejemplarRepository.countByLibroId(libroId) + 1));
        return EjemplarDto.from(ejemplarRepository.save(e));
    }

    @Transactional
    public EjemplarDto cambiarEstado(Long id, String estado) {
        Ejemplar e = buscar(id);
        if (estado == null || estado.isBlank()) {
            throw new SolicitudInvalidaException("El estado es obligatorio");
        }
        if (PRESTADO.equals(e.getEstado()) && !PRESTADO.equals(estado)) {
            throw new ConflictoException("No se puede cambiar el estado de un ejemplar prestado. Devuélvelo primero.");
        }
        e.setEstado(estado.toUpperCase());
        return EjemplarDto.from(ejemplarRepository.save(e));
    }

    @Transactional
    public void eliminar(Long id) {
        Ejemplar e = buscar(id);
        if (!DISPONIBLE.equals(e.getEstado())) {
            throw new ConflictoException("Solo se pueden eliminar ejemplares disponibles");
        }
        ejemplarRepository.delete(e);
    }

    @Transactional
    public void eliminarPorLibro(Long libroId) {
        ejemplarRepository.deleteByLibroId(libroId);
    }

    public Ejemplar buscar(Long id) {
        return ejemplarRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Ejemplar no encontrado"));
    }

    private String siguienteCodigo(Long libroId, int intento) {
        int n = intento;
        String codigo = String.format("LIB-%d-EJ-%02d", libroId, n);
        while (ejemplarRepository.existsByCodigo(codigo)) {
            n++;
            codigo = String.format("LIB-%d-EJ-%02d", libroId, n);
        }
        return codigo;
    }
}
