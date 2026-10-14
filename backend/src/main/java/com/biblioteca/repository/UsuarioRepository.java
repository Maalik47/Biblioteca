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
            WHERE (:q = '' OR LOWER(u.nombre) LIKE LOWER(CONCAT('%', :q, '%'))
                   OR LOWER(COALESCE(u.apellido, '')) LIKE LOWER(CONCAT('%', :q, '%'))
                   OR LOWER(u.username) LIKE LOWER(CONCAT('%', :q, '%'))
                   OR LOWER(u.email) LIKE LOWER(CONCAT('%', :q, '%'))
                   OR LOWER(COALESCE(u.codigoLector, '')) LIKE LOWER(CONCAT('%', :q, '%')))
              AND (:rol = '' OR u.rol = :rol)
              AND (:tipoLector = '' OR u.tipoLector = :tipoLector)
              AND (:soloAcceso = FALSE OR u.puedeAcceder = TRUE)
            """)
    Page<Usuario> buscar(@Param("q") String q,
                         @Param("rol") String rol,
                         @Param("tipoLector") String tipoLector,
                         @Param("soloAcceso") boolean soloAcceso,
                         Pageable pageable);
}
