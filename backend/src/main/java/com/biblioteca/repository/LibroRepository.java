package com.biblioteca.repository;

import com.biblioteca.entity.Libro;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface LibroRepository extends JpaRepository<Libro, Long> {

    Optional<Libro> findByIsbn(String isbn);

    boolean existsByIsbnAndIdNot(String isbn, Long id);

    long countByEstado(String estado);

    @Query("""
            SELECT l FROM Libro l
            WHERE (:q = '' OR LOWER(l.titulo) LIKE LOWER(CONCAT('%', :q, '%'))
                   OR LOWER(l.autor)   LIKE LOWER(CONCAT('%', :q, '%'))
                   OR LOWER(COALESCE(l.isbn, '')) LIKE LOWER(CONCAT('%', :q, '%')))
              AND (:categoria = '' OR l.categoria = :categoria)
              AND (:estado = '' OR l.estado = :estado)
              AND (:soloDisponibles = FALSE OR l.disponibles > 0)
            """)
    Page<Libro> buscar(@Param("q") String q,
                       @Param("categoria") String categoria,
                       @Param("estado") String estado,
                       @Param("soloDisponibles") boolean soloDisponibles,
                       Pageable pageable);

    @Query("SELECT DISTINCT l.categoria FROM Libro l WHERE l.categoria IS NOT NULL ORDER BY l.categoria")
    java.util.List<String> listarCategorias();
}
