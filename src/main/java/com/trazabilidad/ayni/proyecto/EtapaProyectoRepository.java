package com.trazabilidad.ayni.proyecto;

import com.trazabilidad.ayni.shared.enums.EstadoEtapaProyecto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Repositorio para la entidad EtapaProyecto.
 */
public interface EtapaProyectoRepository extends JpaRepository<EtapaProyecto, Long> {

    /**
     * Busca etapas de un proyecto ordenadas por orden.
     */
    List<EtapaProyecto> findByProyectoIdOrderByOrdenAsc(Long proyectoId);

    /**
     * Busca etapas de un proyecto por estado.
     */
    List<EtapaProyecto> findByProyectoIdAndEstado(Long proyectoId, EstadoEtapaProyecto estado);

    /**
     * Busca etapas por responsable.
     */
    List<EtapaProyecto> findByResponsableId(Long responsableId);

    /**
     * Cuenta etapas de un proyecto por estado.
     */
    long countByProyectoIdAndEstado(Long proyectoId, EstadoEtapaProyecto estado);

    /**
     * Cuenta total de etapas de un proyecto.
     */
    long countByProyectoId(Long proyectoId);

    /**
     * Busca una etapa específica por proyecto y orden.
     */
    @Query("SELECT e FROM EtapaProyecto e WHERE e.proyecto.id = :proyectoId AND e.orden = :orden")
    EtapaProyecto findByProyectoIdAndOrden(@Param("proyectoId") Long proyectoId, @Param("orden") Integer orden);

    /**
     * Verifica si existe una etapa con un orden específico en un proyecto.
     */
    boolean existsByProyectoIdAndOrden(Long proyectoId, Integer orden);
}
