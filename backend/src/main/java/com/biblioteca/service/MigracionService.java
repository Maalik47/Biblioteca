package com.biblioteca.service;

import com.biblioteca.entity.Ejemplar;
import com.biblioteca.entity.Libro;
import com.biblioteca.entity.Prestamo;
import com.biblioteca.entity.Usuario;
import com.biblioteca.repository.EjemplarRepository;
import com.biblioteca.repository.LibroRepository;
import com.biblioteca.repository.PrestamoRepository;
import com.biblioteca.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Normaliza datos existentes tras una actualizacion de esquema aditiva:
 * rellena columnas nuevas nulas y crea los ejemplares que falten, de forma
 * que el catalogo antiguo quede coherente con el nuevo modelo.
 */
@Service
public class MigracionService {

    private static final Logger log = LoggerFactory.getLogger(MigracionService.class);

    private final LibroRepository libroRepository;
    private final UsuarioRepository usuarioRepository;
    private final PrestamoRepository prestamoRepository;
    private final EjemplarRepository ejemplarRepository;
    private final EjemplarService ejemplarService;

    public MigracionService(LibroRepository libroRepository,
                            UsuarioRepository usuarioRepository,
                            PrestamoRepository prestamoRepository,
                            EjemplarRepository ejemplarRepository,
                            EjemplarService ejemplarService) {
        this.libroRepository = libroRepository;
        this.usuarioRepository = usuarioRepository;
        this.prestamoRepository = prestamoRepository;
        this.ejemplarRepository = ejemplarRepository;
        this.ejemplarService = ejemplarService;
    }

    @Transactional
    public void normalizar() {
        boolean cambios = false;

        for (Libro l : libroRepository.findAll()) {
            boolean c = false;
            if (l.getEstado() == null || l.getEstado().isBlank()) {
                l.setEstado("ACTIVO");
                c = true;
            }
            if (l.getCantidad() == null || l.getCantidad() < 1) {
                l.setCantidad(1);
                c = true;
            }
            if (c) {
                libroRepository.save(l);
                cambios = true;
            }
        }

        for (Usuario u : usuarioRepository.findAll()) {
            boolean c = false;
            if (u.getPuedeAcceder() == null) {
                u.setPuedeAcceder(true);
                c = true;
            }
            if (u.getTipoLector() == null || u.getTipoLector().isBlank()) {
                u.setTipoLector("GENERAL");
                c = true;
            }
            if (u.getActivo() == null) {
                u.setActivo(true);
                c = true;
            }
            if (c) {
                usuarioRepository.save(u);
                cambios = true;
            }
        }

        // 1) Generar los ejemplares que falten para cada libro.
        for (Libro l : libroRepository.findAll()) {
            int total = (int) ejemplarRepository.countByLibroId(l.getId());
            int cantidad = l.getCantidad() == null ? 0 : l.getCantidad();
            if (total < cantidad) {
                ejemplarService.generar(l, cantidad - total);
                cambios = true;
            }
        }

        // 2) Asignar un ejemplar a los prestamos activos que aun no lo tengan.
        for (Prestamo p : prestamoRepository.listarActivos()) {
            if (p.getRenovaciones() == null) {
                p.setRenovaciones(0);
                prestamoRepository.save(p);
                cambios = true;
            }
            if (p.getEjemplar() == null) {
                Ejemplar e = ejemplarRepository
                        .findFirstByLibroIdAndEstadoOrderByCodigoAsc(p.getLibro().getId(), EjemplarService.DISPONIBLE)
                        .orElse(null);
                if (e != null) {
                    e.setEstado(EjemplarService.PRESTADO);
                    ejemplarRepository.save(e);
                    p.setEjemplar(e);
                    prestamoRepository.save(p);
                    cambios = true;
                }
            }
        }

        // 3) Recalcular la disponibilidad de cada libro.
        for (Libro l : libroRepository.findAll()) {
            int disponibles = (int) ejemplarRepository.countByLibroIdAndEstado(l.getId(), EjemplarService.DISPONIBLE);
            if (l.getDisponibles() == null || l.getDisponibles() != disponibles) {
                l.setDisponibles(disponibles);
                libroRepository.save(l);
                cambios = true;
            }
        }

        if (cambios) {
            log.info("Datos normalizados tras actualizacion de esquema.");
        }
    }
}
