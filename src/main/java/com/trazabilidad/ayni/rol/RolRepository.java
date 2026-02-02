package com.trazabilidad.ayni.rol;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para la gestión de roles.
 */
@Repository
public interface RolRepository extends JpaRepository<Rol, Long> {

    /**
     * Busca un rol por su nombre
     */
    Optional<Rol> findByNombre(String nombre);

    /**
     * Verifica si existe un rol con el nombre dado
     */
    boolean existsByNombre(String nombre);

    /**
     * Obtiene todos los roles activos
     */
    List<Rol> findByActivoTrue();

    /**
     * Obtiene roles por lista de nombres
     */
    @Query("SELECT r FROM Rol r WHERE UPPER(r.nombre) IN :nombres")
    List<Rol> findByNombresIn(@Param("nombres") List<String> nombres);

    /**
     * Busca roles que contengan cierto texto en nombre o descripción
     */
    @Query("SELECT r FROM Rol r WHERE " +
            "LOWER(r.nombre) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(r.descripcion) LIKE LOWER(CONCAT('%', :search, '%'))")
    List<Rol> buscarPorTexto(@Param("search") String search);

    /**
     * Cuenta usuarios asignados a un rol específico
     */
    @Query("SELECT COUNT(u) FROM Usuario u JOIN u.roles r WHERE r.id = :rolId")
    Long contarUsuariosPorRol(@Param("rolId") Long rolId);
}
