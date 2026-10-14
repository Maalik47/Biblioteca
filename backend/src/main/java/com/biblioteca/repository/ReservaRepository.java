package com.biblioteca.repository;

import com.biblioteca.entity.Reserva;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ReservaRepository extends JpaRepository<Reserva, Long> {

    List<Reserva> findByEstadoOrderByFechaSolicitudAsc(String estado);

    List<Reserva> findByLibroIdAndEstadoOrderByFechaSolicitudAsc(Long libroId, String estado);

    Optional<Reserva> findFirstByLibroIdAndEstadoOrderByFechaSolicitudAsc(Long libroId, String estado);

    List<Reserva> findByUsuarioIdOrderByFechaSolicitudDesc(Long usuarioId);

    long countByEstado(String estado);

    boolean existsByUsuarioIdAndLibroIdAndEstadoIn(Long usuarioId, Long libroId, Collection<String> estados);
}
