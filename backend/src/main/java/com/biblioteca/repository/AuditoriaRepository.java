package com.biblioteca.repository;

import com.biblioteca.entity.Auditoria;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AuditoriaRepository extends JpaRepository<Auditoria, Long> {

    List<Auditoria> findTop15ByOrderByFechaDesc();

    @Query("""
            SELECT a FROM Auditoria a
            WHERE (:q IS NULL OR LOWER(a.usuario) LIKE LOWER(CONCAT('%', :q, '%'))
                   OR LOWER(a.accion) LIKE LOWER(CONCAT('%', :q, '%'))
                   OR LOWER(COALESCE(a.entidad, '')) LIKE LOWER(CONCAT('%', :q, '%')))
              AND (:accion IS NULL OR a.accion = :accion)
            ORDER BY a.fecha DESC
            """)
    Page<Auditoria> buscar(@Param("q") String q, @Param("accion") String accion, Pageable pageable);
}
