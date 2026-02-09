package com.trazabilidad.ayni.solicitud;

import com.trazabilidad.ayni.shared.enums.EstadoSolicitud;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

/**
 * Repositorio para la entidad Solicitud.
 * Incluye filtrado dinámico con JpaSpecificationExecutor.
 */
public interface SolicitudRepository extends JpaRepository<Solicitud, Long>,
                JpaSpecificationExecutor<Solicitud> {

        /**
         * Busca solicitudes con filtros opcionales.
         * Usa SQL nativo con CAST para evitar el error "lower(bytea)" en PostgreSQL.
         *
         * @param search        Búsqueda en nombreProyecto, cliente o descripción
         * @param estado        Estado de la solicitud (opcional)
         * @param responsableId ID del responsable (opcional)
         * @param desde         Fecha desde (opcional)
         * @param hasta         Fecha hasta (opcional)
         * @param pageable      Paginación
         * @return Página de solicitudes
         */
        @Query(value = "SELECT * FROM solicitudes s " +
                        "WHERE (CAST(:search AS text) IS NULL OR " +
                        "      LOWER(s.nombre_proyecto) LIKE LOWER(CONCAT('%', CAST(:search AS text), '%')) OR " +
                        "      LOWER(s.cliente) LIKE LOWER(CONCAT('%', CAST(:search AS text), '%')) OR " +
                        "      LOWER(s.descripcion) LIKE LOWER(CONCAT('%', CAST(:search AS text), '%'))) " +
                        "  AND (CAST(:estado AS text) IS NULL OR s.estado = CAST(:estado AS text)) " +
                        "  AND (:responsableId IS NULL OR s.responsable_id = :responsableId) " +
                        "  AND (:desde IS NULL OR s.fecha_solicitud >= :desde) " +
                        "  AND (:hasta IS NULL OR s.fecha_solicitud <= :hasta)", countQuery = "SELECT COUNT(*) FROM solicitudes s "
                                        +
                                        "WHERE (CAST(:search AS text) IS NULL OR " +
                                        "      LOWER(s.nombre_proyecto) LIKE LOWER(CONCAT('%', CAST(:search AS text), '%')) OR "
                                        +
                                        "      LOWER(s.cliente) LIKE LOWER(CONCAT('%', CAST(:search AS text), '%')) OR "
                                        +
                                        "      LOWER(s.descripcion) LIKE LOWER(CONCAT('%', CAST(:search AS text), '%'))) "
                                        +
                                        "  AND (CAST(:estado AS text) IS NULL OR s.estado = CAST(:estado AS text)) " +
                                        "  AND (:responsableId IS NULL OR s.responsable_id = :responsableId) " +
                                        "  AND (:desde IS NULL OR s.fecha_solicitud >= :desde) " +
                                        "  AND (:hasta IS NULL OR s.fecha_solicitud <= :hasta)", nativeQuery = true)
        Page<Solicitud> buscarConFiltros(
                        @Param("search") String search,
                        @Param("estado") EstadoSolicitud estado,
                        @Param("responsableId") Long responsableId,
                        @Param("desde") LocalDate desde,
                        @Param("hasta") LocalDate hasta,
                        Pageable pageable);

        /**
         * Cuenta solicitudes por estado.
         */
        long countByEstado(EstadoSolicitud estado);

        /**
         * Busca solicitudes por responsable.
         */
        List<Solicitud> findByResponsableId(Long responsableId);

        /**
         * Verifica si existe una solicitud duplicada.
         * Previene duplicados con mismo nombre de proyecto, cliente y estado !=
         * CANCELADO.
         */
        boolean existsByNombreProyectoAndClienteAndEstadoNot(
                        String nombreProyecto,
                        String cliente,
                        EstadoSolicitud estado);

        /**
         * Cuenta solicitudes por responsable.
         */
        long countByResponsableId(Long responsableId);

        /**
         * Busca solicitudes por estado.
         */
        List<Solicitud> findByEstado(EstadoSolicitud estado);
}
