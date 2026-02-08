package com.trazabilidad.ayni.costo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

/**
 * Repositorio para la entidad CostoAdicional.
 */
@Repository
public interface CostoAdicionalRepository extends JpaRepository<CostoAdicional, Long> {

    /**
     * Encuentra todos los costos adicionales de un proyecto.
     */
    List<CostoAdicional> findByProyectoId(Long proyectoId);

    /**
     * Encuentra costos adicionales por proyecto y categoría.
     */
    List<CostoAdicional> findByProyectoIdAndCategoria(Long proyectoId, String categoria);

    /**
     * Elimina todos los costos adicionales de un proyecto.
     */
    void deleteByProyectoId(Long proyectoId);

    /**
     * Suma el costo total de todos los costos adicionales de un proyecto.
     */
    @Query("SELECT COALESCE(SUM(c.monto), 0) FROM CostoAdicional c WHERE c.proyecto.id = :proyectoId")
    BigDecimal sumCostoTotalByProyectoId(@Param("proyectoId") Long proyectoId);

    /**
     * Obtiene las categorías distintas usadas en un proyecto.
     */
    @Query("SELECT DISTINCT c.categoria FROM CostoAdicional c WHERE c.proyecto.id = :proyectoId ORDER BY c.categoria")
    List<String> findDistinctCategoriasByProyectoId(@Param("proyectoId") Long proyectoId);

    /**
     * Cuenta los items de costos adicionales de un proyecto.
     */
    long countByProyectoId(Long proyectoId);
}
