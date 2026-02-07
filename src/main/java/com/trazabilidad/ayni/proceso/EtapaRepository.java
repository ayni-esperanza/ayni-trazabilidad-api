package com.trazabilidad.ayni.proceso;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio para la entidad Etapa.
 * Proporciona queries para gestionar etapas dentro de un proceso.
 */
@Repository
public interface EtapaRepository extends JpaRepository<Etapa, Long> {

    /**
     * Obtiene las etapas de un proceso ordenadas por su campo 'orden'.
     *
     * @param procesoId ID del proceso
     * @return Lista de etapas ordenadas
     */
    List<Etapa> findByProcesoIdOrderByOrdenAsc(Long procesoId);

    /**
     * Obtiene solo las etapas activas de un proceso.
     *
     * @param procesoId ID del proceso
     * @return Lista de etapas activas
     */
    List<Etapa> findByProcesoIdAndActivoTrue(Long procesoId);

    /**
     * Cuenta el número de etapas de un proceso.
     *
     * @param procesoId ID del proceso
     * @return Cantidad de etapas
     */
    long countByProcesoId(Long procesoId);

    /**
     * Verifica si existe una etapa con cierto orden dentro de un proceso.
     * Útil para validar unicidad del orden al crear/actualizar etapas.
     *
     * @param procesoId ID del proceso
     * @param orden     Orden a verificar
     * @return true si existe, false en caso contrario
     */
    boolean existsByProcesoIdAndOrden(Long procesoId, Integer orden);

    /**
     * Elimina todas las etapas de un proceso.
     * Usado cuando se elimina un proceso.
     *
     * @param procesoId ID del proceso
     */
    void deleteByProcesoId(Long procesoId);

    /**
     * Busca etapas por nombre que contenga el término (case-insensitive).
     *
     * @param nombre Término de búsqueda
     * @return Lista de etapas coincidentes
     */
    List<Etapa> findByNombreContainingIgnoreCase(String nombre);
}
