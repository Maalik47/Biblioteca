package com.biblioteca.repository;

import com.biblioteca.entity.Prestamo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

public interface PrestamoRepository extends JpaRepository<Prestamo, Long> {

    long countByEstado(String estado);

    long countByLibroId(Long libroId);

    long countByLibroIdAndEstado(Long libroId, String estado);

    boolean existsByUsuarioIdAndLibroIdAndEstado(Long usuarioId, Long libroId, String estado);

    long countByUsuarioIdAndEstado(Long usuarioId, String estado);

    long countByUsuarioIdAndEstadoIn(Long usuarioId, Collection<String> estados);

    List<Prestamo> findByUsuarioIdOrderByFechaPrestamoDesc(Long usuarioId);

    List<Prestamo> findByEstadoAndFechaLimiteBefore(String estado, LocalDate fecha);

    @Query("""
            SELECT p FROM Prestamo p
            JOIN FETCH p.usuario u
            JOIN FETCH p.libro l
            WHERE (:q IS NULL OR LOWER(l.titulo) LIKE LOWER(CONCAT('%', :q, '%'))
                   OR LOWER(u.nombre) LIKE LOWER(CONCAT('%', :q, '%'))
                   OR LOWER(COALESCE(u.apellido, '')) LIKE LOWER(CONCAT('%', :q, '%')))
              AND (:estado IS NULL OR p.estado = :estado)
              AND (:usuarioId IS NULL OR u.id = :usuarioId)
            ORDER BY p.id DESC
            """)
    Page<Prestamo> buscar(@Param("q") String q,
                          @Param("estado") String estado,
                          @Param("usuarioId") Long usuarioId,
                          Pageable pageable);

    @Query("SELECT p FROM Prestamo p JOIN FETCH p.usuario JOIN FETCH p.libro ORDER BY p.id DESC")
    List<Prestamo> listarCompleto();

    @Query("SELECT p.libro.titulo, COUNT(p) FROM Prestamo p GROUP BY p.libro.titulo ORDER BY COUNT(p) DESC")
    List<Object[]> rankingLibros(Pageable pageable);

    @Query("""
            SELECT p FROM Prestamo p
            JOIN FETCH p.usuario JOIN FETCH p.libro
            WHERE p.estado = 'PENDIENTE'
            ORDER BY p.fechaLimite ASC
            """)
    List<Prestamo> listarActivos();
}
