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
 * Repositorio para la gestión de usuarios.
 */
@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    /**
     * Busca usuario por email
     */
    Optional<Usuario> findByEmail(String email);

    /**
     * Busca usuario por username
     */
    Optional<Usuario> findByUsername(String username);

    /**
     * Busca usuario activo por username
     */
    Optional<Usuario> findByUsernameAndActivoTrue(String username);

    /**
     * Busca usuario activo por ID
     */
    Optional<Usuario> findByIdAndActivoTrue(Long id);

    /**
     * Busca usuario por email o username
     */
    Optional<Usuario> findByEmailOrUsername(String email, String username);

    /**
     * Verifica si existe un usuario con el email dado
     */
    boolean existsByEmail(String email);

    /**
     * Verifica si existe un usuario con el username dado
     */
    boolean existsByUsername(String username);

    /**
     * Busca usuarios con filtros aplicados
     * Busca en nombre, apellido, email, username y teléfono
     */
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

    /**
     * Cuenta usuarios activos
     */
    Long countByActivoTrue();

    /**
     * Cuenta usuarios por roles específicos
     */
    @Query("SELECT COUNT(DISTINCT u) FROM Usuario u JOIN u.roles r WHERE UPPER(r.nombre) IN :nombres")
    Long countByRolesNombreIn(@Param("nombres") List<String> nombres);

    /**
     * Obtiene usuarios por rol
     */
    @Query("SELECT u FROM Usuario u JOIN u.roles r WHERE r.id = :rolId")
    List<Usuario> findByRolId(@Param("rolId") Long rolId);

    /**
     * Obtiene usuarios activos
     */
    List<Usuario> findByActivoTrue();

    /**
     * Obtiene usuarios inactivos
     */
    List<Usuario> findByActivoFalse();

    /**
     * Busca usuarios por cargo
     */
    List<Usuario> findByCargo(String cargo);

    /**
     * Busca usuarios por área
     */
    List<Usuario> findByArea(String area);
}
