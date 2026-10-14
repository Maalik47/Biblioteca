package com.biblioteca.repository;

import com.biblioteca.entity.Usuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByUsername(String username);

    Optional<Usuario> findByCodigoLector(String codigoLector);

    boolean existsByUsername(String username);

    boolean existsByUsernameAndIdNot(String username, Long id);

    boolean existsByCodigoLectorAndIdNot(String codigoLector, Long id);

    long countByPuedeAcceder(boolean puedeAcceder);

    long countByActivo(boolean activo);

    @Query("""
            SELECT u FROM Usuario u
            WHERE (:q IS NULL OR LOWER(u.nombre) LIKE LOWER(CONCAT('%', :q, '%'))
                   OR LOWER(COALESCE(u.apellido, '')) LIKE LOWER(CONCAT('%', :q, '%'))
                   OR LOWER(u.username) LIKE LOWER(CONCAT('%', :q, '%'))
                   OR LOWER(u.email) LIKE LOWER(CONCAT('%', :q, '%'))
                   OR LOWER(COALESCE(u.codigoLector, '')) LIKE LOWER(CONCAT('%', :q, '%')))
              AND (:rol IS NULL OR u.rol = :rol)
              AND (:tipoLector IS NULL OR u.tipoLector = :tipoLector)
              AND (:soloAcceso IS NULL OR u.puedeAcceder = :soloAcceso)
            """)
    Page<Usuario> buscar(@Param("q") String q,
                         @Param("rol") String rol,
                         @Param("tipoLector") String tipoLector,
                         @Param("soloAcceso") Boolean soloAcceso,
                         Pageable pageable);
}
