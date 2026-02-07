package com.trazabilidad.ayni.proceso;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para la entidad Proceso.
 * Incluye queries derivadas y JPQL personalizadas para filtrado dinámico.
 */
@Repository
public interface ProcesoRepository extends JpaRepository<Proceso, Long>, JpaSpecificationExecutor<Proceso> {

    /**
     * Busca un proceso por su nombre exacto.
     *
     * @param nombre Nombre del proceso
     * @return Optional con el proceso si existe
     */
    Optional<Proceso> findByNombre(String nombre);

    /**
     * Verifica si existe un proceso con el nombre dado.
     *
     * @param nombre Nombre a verificar
     * @return true si existe, false en caso contrario
     */
    boolean existsByNombre(String nombre);

    /**
     * Obtiene todos los procesos activos.
     *
     * @return Lista de procesos activos
     */
    List<Proceso> findByActivoTrue();

    /**
     * Busca procesos por área.
     *
     * @param area Área a filtrar
     * @return Lista de procesos del área especificada
     */
    List<Proceso> findByArea(String area);

    /**
     * Busca procesos con filtros dinámicos usando JPQL.
     * Los parámetros null son ignorados en la búsqueda.
     *
     * @param search   Término de búsqueda (nombre o descripción)
     * @param area     Área a filtrar
     * @param activo   Estado activo/inactivo
     * @param pageable Configuración de paginación
     * @return Página de procesos que cumplen los filtros
     */
    @Query("""
            SELECT p FROM Proceso p
            WHERE (:search IS NULL OR LOWER(p.nombre) LIKE LOWER(CONCAT('%', :search, '%'))
                   OR LOWER(p.descripcion) LIKE LOWER(CONCAT('%', :search, '%')))
            AND (:area IS NULL OR p.area = :area)
            AND (:activo IS NULL OR p.activo = :activo)
            """)
    Page<Proceso> buscarConFiltros(
            @Param("search") String search,
            @Param("area") String area,
            @Param("activo") Boolean activo,
            Pageable pageable);

    /**
     * Cuenta el número de procesos activos.
     *
     * @return Cantidad de procesos activos
     */
    long countByActivoTrue();

    /**
     * Busca procesos por nombre que contenga el término de búsqueda
     * (case-insensitive).
     *
     * @param nombre Término de búsqueda
     * @return Lista de procesos coincidentes
     */
    List<Proceso> findByNombreContainingIgnoreCase(String nombre);
}
