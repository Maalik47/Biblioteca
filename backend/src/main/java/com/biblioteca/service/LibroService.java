package com.biblioteca.service;

import com.biblioteca.dto.LibroDto;
import com.biblioteca.dto.PaginaResponse;
import com.biblioteca.entity.Libro;
import com.biblioteca.exception.ConflictoException;
import com.biblioteca.exception.RecursoNoEncontradoException;
import com.biblioteca.exception.SolicitudInvalidaException;
import com.biblioteca.repository.LibroRepository;
import com.biblioteca.repository.PrestamoRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class LibroService {

    private static final String PENDIENTE = "PENDIENTE";

    private final LibroRepository libroRepository;
    private final PrestamoRepository prestamoRepository;
    private final EjemplarService ejemplarService;
    private final AuditoriaService auditoriaService;

    public LibroService(LibroRepository libroRepository,
                        PrestamoRepository prestamoRepository,
                        EjemplarService ejemplarService,
                        AuditoriaService auditoriaService) {
        this.libroRepository = libroRepository;
        this.prestamoRepository = prestamoRepository;
        this.ejemplarService = ejemplarService;
        this.auditoriaService = auditoriaService;
    }

    public List<LibroDto> listar() {
        return libroRepository.findAll(Sort.by("titulo")).stream().map(LibroDto::from).toList();
    }

    public PaginaResponse<LibroDto> buscar(String q, String categoria, String estado,
                                           boolean soloDisponibles, int pagina, int tamano) {
        var page = libroRepository.buscar(texto(q), texto(categoria), texto(estado),
                soloDisponibles,
                PageRequest.of(Math.max(0, pagina), Math.max(1, Math.min(tamano, 50)),
                        Sort.by("titulo")));
        return PaginaResponse.desde(page, LibroDto::from);
    }

    public List<String> categorias() {
        return libroRepository.listarCategorias();
    }

    public LibroDto obtener(Long id) {
        return LibroDto.from(buscar(id));
    }

    @Transactional
    public LibroDto crear(Libro libro) {
        if (libro.getTitulo() == null || libro.getTitulo().isBlank()) {
            throw new SolicitudInvalidaException("El título es obligatorio");
        }
        if (libro.getAutor() == null || libro.getAutor().isBlank()) {
            throw new SolicitudInvalidaException("El autor es obligatorio");
        }
        if (libro.getIsbn() != null && !libro.getIsbn().isBlank()
                && libroRepository.findByIsbn(libro.getIsbn().trim()).isPresent()) {
            throw new ConflictoException("Ya existe un libro con ese ISBN");
        }
        libro.setId(null);
        libro.setIsbn(limpiar(libro.getIsbn()));
        int cantidad = libro.getCantidad() == null || libro.getCantidad() < 1 ? 1 : libro.getCantidad();
        libro.setCantidad(cantidad);
        libro.setDisponibles(cantidad);
        libro.setEstado(libro.getEstado() == null || libro.getEstado().isBlank() ? "ACTIVO" : libro.getEstado());
        Libro guardado = libroRepository.save(libro);

        ejemplarService.generar(guardado, cantidad);
        auditoriaService.registrar("CREAR", "Libro", guardado.getId(),
                "Alta de libro '" + guardado.getTitulo() + "' con " + cantidad + " ejemplar(es)");
        return LibroDto.from(guardado);
    }

    @Transactional
    public LibroDto actualizar(Long id, Libro libro) {
        Libro existente = buscar(id);

        if (libro.getIsbn() != null && !libro.getIsbn().isBlank()) {
            String nuevoIsbn = libro.getIsbn().trim();
            if (libroRepository.existsByIsbnAndIdNot(nuevoIsbn, id)) {
                throw new ConflictoException("Ya existe un libro con ese ISBN");
            }
            existente.setIsbn(nuevoIsbn);
        }
        if (libro.getTitulo() != null && !libro.getTitulo().isBlank()) {
            existente.setTitulo(libro.getTitulo());
        }
        if (libro.getAutor() != null && !libro.getAutor().isBlank()) {
            existente.setAutor(libro.getAutor());
        }
        existente.setCategoria(libro.getCategoria());
        existente.setAnioPublicacion(libro.getAnioPublicacion());
        existente.setEditorial(libro.getEditorial());
        existente.setDescripcion(libro.getDescripcion());
        existente.setPortadaUrl(libro.getPortadaUrl());
        if (libro.getUbicacion() != null && !libro.getUbicacion().isBlank()) {
            existente.setUbicacion(libro.getUbicacion());
        }
        if (libro.getEstado() != null && !libro.getEstado().isBlank()) {
            existente.setEstado(libro.getEstado());
        }

        if (libro.getCantidad() != null && libro.getCantidad() >= 1) {
            ajustarCantidad(existente, libro.getCantidad());
        }

        libroRepository.save(existente);
        sincronizarContadores(existente);
        auditoriaService.registrar("ACTUALIZAR", "Libro", id, "Edición de libro '" + existente.getTitulo() + "'");
        return LibroDto.from(existente);
    }

    @Transactional
    public void eliminar(Long id) {
        Libro libro = buscar(id);
        long activos = prestamoRepository.countByLibroIdAndEstado(id, PENDIENTE);
        if (activos > 0) {
            throw new ConflictoException("No se puede eliminar: el libro tiene préstamos activos");
        }
        String titulo = libro.getTitulo();
        ejemplarService.eliminarPorLibro(id);
        libroRepository.delete(libro);
        auditoriaService.registrar("ELIMINAR", "Libro", id, "Baja de libro '" + titulo + "'");
    }

    private void ajustarCantidad(Libro existente, int nuevaCantidad) {
        int actual = (int) ejemplarService.listarPorLibro(existente.getId()).size();
        if (nuevaCantidad > actual) {
            ejemplarService.generar(existente, nuevaCantidad - actual);
        } else if (nuevaCantidad < actual) {
            ejemplarService.eliminarDisponiblesSobrantes(existente.getId(), actual - nuevaCantidad);
        }
    }

    private void sincronizarContadores(Libro libro) {
        int total = (int) ejemplarService.listarPorLibro(libro.getId()).size();
        int disponibles = (int) ejemplarService.contarDisponibles(libro.getId());
        libro.setCantidad(total);
        libro.setDisponibles(disponibles);
        libroRepository.save(libro);
    }

    private Libro buscar(Long id) {
        return libroRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Libro no encontrado"));
    }

    private String limpiar(String valor) {
        return (valor == null || valor.isBlank()) ? null : valor.trim();
    }

    private String normalizar(String valor) {
        return (valor == null || valor.isBlank()) ? null : valor.trim();
    }

    private String texto(String valor) {
        String v = normalizar(valor);
        return v == null ? "" : v;
    }
}
