package com.biblioteca.repository;

import com.biblioteca.entity.Ejemplar;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EjemplarRepository extends JpaRepository<Ejemplar, Long> {

    List<Ejemplar> findByLibroIdOrderByCodigoAsc(Long libroId);

    List<Ejemplar> findByLibroIdAndEstadoOrderByCodigoAsc(Long libroId, String estado);

    Optional<Ejemplar> findFirstByLibroIdAndEstadoOrderByCodigoAsc(Long libroId, String estado);

    long countByLibroId(Long libroId);

    long countByLibroIdAndEstado(Long libroId, String estado);

    long countByEstado(String estado);

    boolean existsByCodigo(String codigo);

    void deleteByLibroId(Long libroId);
}
