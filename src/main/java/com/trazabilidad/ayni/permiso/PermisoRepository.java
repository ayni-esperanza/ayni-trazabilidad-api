package com.trazabilidad.ayni.permiso;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para la gestiÃ³n de permisos.
 */
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
     * Obtiene permisos por mÃ³dulo
     */
    List<Permiso> findByModulo(String modulo);

    /**
     * Obtiene permisos de mÃºltiples mÃ³dulos
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
     * Obtiene todos los mÃ³dulos Ãºnicos
     */
    @Query("SELECT DISTINCT p.modulo FROM Permiso p ORDER BY p.modulo")
    List<String> obtenerModulosUnicos();

    /**
     * Cuenta permisos por mÃ³dulo
     */
    Long countByModulo(String modulo);
}
