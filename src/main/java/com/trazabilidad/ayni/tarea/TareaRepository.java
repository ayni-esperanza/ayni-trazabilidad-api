package com.trazabilidad.ayni.tarea;

import com.trazabilidad.ayni.shared.enums.EstadoTarea;
import com.trazabilidad.ayni.shared.enums.PrioridadTarea;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio para la entidad Tarea.
 * Incluye consultas complejas con joins a EtapaProyecto y Proyecto.
 */
@Repository
public interface TareaRepository extends JpaRepository<Tarea, Long>, JpaSpecificationExecutor<Tarea> {

    /**
     * Búsqueda avanzada con filtros opcionales y joins.
     */
    @Query("""
            SELECT t FROM Tarea t
            LEFT JOIN FETCH t.etapaProyecto ep
            LEFT JOIN FETCH ep.proyecto p
            LEFT JOIN FETCH t.responsable r
            WHERE (:search IS NULL OR LOWER(t.titulo) LIKE LOWER(CONCAT('%', :search, '%')))
            AND (:estado IS NULL OR t.estado = :estado)
            AND (:prioridad IS NULL OR t.prioridad = :prioridad)
            AND (:responsableId IS NULL OR t.responsable.id = :responsableId)
            AND (:proyectoId IS NULL OR ep.proyecto.id = :proyectoId)
            """)
    Page<Tarea> buscarConFiltros(
            @Param("search") String search,
            @Param("estado") EstadoTarea estado,
            @Param("prioridad") PrioridadTarea prioridad,
            @Param("responsableId") Long responsableId,
            @Param("proyectoId") Long proyectoId,
            Pageable pageable);

    /**
     * Encuentra todas las tareas de una etapa de proyecto, ordenadas por fecha de
     * inicio.
     */
    List<Tarea> findByEtapaProyectoIdOrderByFechaInicioAsc(Long etapaProyectoId);

    /**
     * Encuentra todas las tareas de un responsable.
     */
    List<Tarea> findByResponsableId(Long responsableId);

    /**
     * Encuentra tareas de un responsable con un estado específico.
     */
    List<Tarea> findByResponsableIdAndEstado(Long responsableId, EstadoTarea estado);

    /**
     * Cuenta tareas por etapa de proyecto y estado.
     */
    long countByEtapaProyectoIdAndEstado(Long etapaProyectoId, EstadoTarea estado);

    /**
     * Cuenta tareas por estado.
     */
    long countByEstado(EstadoTarea estado);

    /**
     * Encuentra tareas retrasadas (fecha fin pasada y no completadas/canceladas).
     */
    @Query("""
            SELECT t FROM Tarea t
            LEFT JOIN FETCH t.etapaProyecto ep
            LEFT JOIN FETCH ep.proyecto p
            LEFT JOIN FETCH t.responsable r
            WHERE t.fechaFin < CURRENT_DATE
            AND t.estado NOT IN ('COMPLETADA', 'CANCELADA')
            ORDER BY t.fechaFin ASC
            """)
    List<Tarea> findTareasRetrasadas();

    /**
     * Encuentra todas las tareas de un proyecto (a través de etapas).
     */
    @Query("""
            SELECT t FROM Tarea t
            LEFT JOIN FETCH t.etapaProyecto ep
            LEFT JOIN FETCH t.responsable r
            WHERE ep.proyecto.id = :proyectoId
            ORDER BY ep.orden ASC, t.fechaInicio ASC
            """)
    List<Tarea> findTareasPorProyecto(@Param("proyectoId") Long proyectoId);

    /**
     * Cuenta tareas de un proyecto por estado.
     */
    @Query("""
            SELECT COUNT(t) FROM Tarea t
            WHERE t.etapaProyecto.proyecto.id = :proyectoId
            AND t.estado = :estado
            """)
    long countByProyectoIdAndEstado(@Param("proyectoId") Long proyectoId, @Param("estado") EstadoTarea estado);

    /**
     * Cuenta total de tareas de un proyecto.
     */
    @Query("""
            SELECT COUNT(t) FROM Tarea t
            WHERE t.etapaProyecto.proyecto.id = :proyectoId
            """)
    long countByProyectoId(@Param("proyectoId") Long proyectoId);

    /**
     * Cuenta tareas por prioridad.
     */
    @Query("""
            SELECT t.prioridad, COUNT(t) FROM Tarea t
            GROUP BY t.prioridad
            """)
    List<Object[]> countByPrioridad();

    /**
     * Calcula el promedio de porcentaje de avance de todas las tareas.
     */
    @Query("SELECT AVG(t.porcentajeAvance) FROM Tarea t")
    Double calcularPromedioPorcentaje();

    /**
     * Encuentra tareas por responsable con joins optimizados.
     */
    @Query("""
            SELECT t FROM Tarea t
            LEFT JOIN FETCH t.etapaProyecto ep
            LEFT JOIN FETCH ep.proyecto p
            WHERE t.responsable.id = :usuarioId
            ORDER BY t.fechaFin ASC NULLS LAST
            """)
    List<Tarea> findTareasPorUsuario(@Param("usuarioId") Long usuarioId);
}
