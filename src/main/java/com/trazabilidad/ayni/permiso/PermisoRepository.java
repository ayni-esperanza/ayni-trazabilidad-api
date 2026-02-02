package com.trazabilidad.ayni.permiso;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para la gestión de permisos.
 */
@Repository
public interface PermisoRepository extends JpaRepository<Permiso, Long> {

    /**
     * Busca un permiso por su nombre
     */
    Optional<Permiso> findByNombre(String nombre);

    /**
     * Verifica si existe un permiso con el nombre dado
     */
    boolean existsByNombre(String nombre);

    /**
     * Obtiene permisos por módulo
     */
    List<Permiso> findByModulo(String modulo);

    /**
     * Obtiene permisos de múltiples módulos
     */
    List<Permiso> findByModuloIn(List<String> modulos);

    /**
     * Busca permisos que contengan cierto texto
     */
    @Query("SELECT p FROM Permiso p WHERE " +
            "LOWER(p.nombre) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(p.descripcion) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(p.modulo) LIKE LOWER(CONCAT('%', :search, '%'))")
    List<Permiso> buscarPorTexto(@Param("search") String search);

    /**
     * Obtiene todos los módulos únicos
     */
    @Query("SELECT DISTINCT p.modulo FROM Permiso p ORDER BY p.modulo")
    List<String> obtenerModulosUnicos();

    /**
     * Cuenta permisos por módulo
     */
    Long countByModulo(String modulo);
}
