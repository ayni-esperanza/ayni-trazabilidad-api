package com.trazabilidad.ayni.costo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

/**
 * Repositorio para la entidad CostoMaterial.
 */
@Repository
public interface CostoMaterialRepository extends JpaRepository<CostoMaterial, Long> {

    /**
     * Encuentra todos los costos de material de un proyecto.
     */
    List<CostoMaterial> findByProyectoId(Long proyectoId);

    /**
     * Elimina todos los costos de material de un proyecto.
     */
    void deleteByProyectoId(Long proyectoId);

    /**
     * Suma el costo total de todos los materiales de un proyecto.
     */
    @Query("SELECT COALESCE(SUM(c.costoTotal), 0) FROM CostoMaterial c WHERE c.proyecto.id = :proyectoId")
    BigDecimal sumCostoTotalByProyectoId(@Param("proyectoId") Long proyectoId);

    /**
     * Cuenta los items de material de un proyecto.
     */
    long countByProyectoId(Long proyectoId);
}
