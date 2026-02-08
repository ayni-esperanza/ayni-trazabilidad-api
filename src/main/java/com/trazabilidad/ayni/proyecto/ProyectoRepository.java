package com.trazabilidad.ayni.proyecto;

import com.trazabilidad.ayni.shared.enums.EstadoProyecto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para la entidad Proyecto.
 * Incluye filtrado dinámico y optimización de queries con EntityGraph.
 */
public interface ProyectoRepository extends JpaRepository<Proyecto, Long>,
        JpaSpecificationExecutor<Proyecto> {

    /**
     * Busca proyectos con filtros opcionales.
     * Utiliza EntityGraph para evitar N+1 queries.
     *
     * @param search        Búsqueda en nombre de proyecto, cliente o descripción
     * @param estado        Estado del proyecto (opcional)
     * @param procesoId     ID del proceso (opcional)
     * @param responsableId ID del responsable (opcional)
     * @param pageable      Paginación
     * @return Página de proyectos
     */
    @EntityGraph(attributePaths = { "proceso", "responsable", "solicitud" })
    @Query("SELECT p FROM Proyecto p " +
            "WHERE (:search IS NULL OR " +
            "      LOWER(p.nombreProyecto) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "      LOWER(p.cliente) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "      LOWER(p.descripcion) LIKE LOWER(CONCAT('%', :search, '%'))) " +
            "  AND (:estado IS NULL OR p.estado = :estado) " +
            "  AND (:procesoId IS NULL OR p.proceso.id = :procesoId) " +
            "  AND (:responsableId IS NULL OR p.responsable.id = :responsableId)")
    Page<Proyecto> buscarConFiltros(
            @Param("search") String search,
            @Param("estado") EstadoProyecto estado,
            @Param("procesoId") Long procesoId,
            @Param("responsableId") Long responsableId,
            Pageable pageable);

    /**
     * Busca proyecto por ID de solicitud.
     */
    @EntityGraph(attributePaths = { "proceso", "responsable", "solicitud", "etapasProyecto" })
    Optional<Proyecto> findBySolicitudId(Long solicitudId);

    /**
     * Verifica si existe un proyecto para una solicitud.
     */
    boolean existsBySolicitudId(Long solicitudId);

    /**
     * Cuenta proyectos por estado.
     */
    long countByEstado(EstadoProyecto estado);

    /**
     * Busca proyectos por responsable.
     */
    @EntityGraph(attributePaths = { "proceso", "responsable", "solicitud" })
    List<Proyecto> findByResponsableId(Long responsableId);

    /**
     * Busca proyectos por proceso.
     */
    @EntityGraph(attributePaths = { "proceso", "responsable", "solicitud" })
    List<Proyecto> findByProcesoId(Long procesoId);

    /**
     * Obtiene un proyecto con todas sus relaciones cargadas.
     */
    @EntityGraph(attributePaths = { "proceso", "responsable", "solicitud", "etapasProyecto",
            "etapasProyecto.responsable" })
    Optional<Proyecto> findWithEtapasById(Long id);
}
