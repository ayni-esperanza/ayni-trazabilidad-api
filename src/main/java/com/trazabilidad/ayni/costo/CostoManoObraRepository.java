package com.trazabilidad.ayni.costo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

/**
 * Repositorio para la entidad CostoManoObra.
 */
@Repository
public interface CostoManoObraRepository extends JpaRepository<CostoManoObra, Long> {

    /**
     * Encuentra todos los costos de mano de obra de un proyecto.
     */
    List<CostoManoObra> findByProyectoId(Long proyectoId);

    /**
     * Elimina todos los costos de mano de obra de un proyecto.
     */
    void deleteByProyectoId(Long proyectoId);

    /**
     * Suma el costo total de toda la mano de obra de un proyecto.
     */
    @Query("SELECT COALESCE(SUM(c.costoTotal), 0) FROM CostoManoObra c WHERE c.proyecto.id = :proyectoId")
    BigDecimal sumCostoTotalByProyectoId(@Param("proyectoId") Long proyectoId);

    /**
     * Cuenta los items de mano de obra de un proyecto.
     */
    long countByProyectoId(Long proyectoId);
}
