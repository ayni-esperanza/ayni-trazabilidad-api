package com.trazabilidad.ayni.usuario;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para la gestion de usuarios.
 */
@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByEmail(String email);

    Optional<Usuario> findByUsername(String username);

    Optional<Usuario> findByUsernameAndActivoTrue(String username);

    Optional<Usuario> findByIdAndActivoTrue(Long id);

    Optional<Usuario> findByEmailOrUsername(String email, String username);

    boolean existsByEmail(String email);

    boolean existsByUsername(String username);

    @Query("SELECT u FROM Usuario u WHERE " +
            "(:search IS NULL OR :search = '' OR " +
            "LOWER(u.nombre) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(u.apellido) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(u.username) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(u.telefono) LIKE LOWER(CONCAT('%', :search, '%'))) " +
            "AND (:rolId IS NULL OR EXISTS (SELECT r FROM u.roles r WHERE r.id = :rolId))")
    Page<Usuario> buscarConFiltros(
            @Param("search") String search,
            @Param("rolId") Long rolId,
            Pageable pageable);

    Long countByActivoTrue();

    @Query("SELECT COUNT(DISTINCT u) FROM Usuario u JOIN u.roles r WHERE UPPER(r.nombre) IN :nombres")
    Long countByRolesNombreIn(@Param("nombres") List<String> nombres);

    @Query("SELECT u FROM Usuario u JOIN u.roles r WHERE r.id = :rolId")
    List<Usuario> findByRolId(@Param("rolId") Long rolId);

    List<Usuario> findByActivoTrue();

    List<Usuario> findByActivoFalse();

    List<Usuario> findByArea(String area);
}
