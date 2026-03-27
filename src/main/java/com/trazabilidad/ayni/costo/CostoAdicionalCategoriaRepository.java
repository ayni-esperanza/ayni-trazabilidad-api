package com.trazabilidad.ayni.costo;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CostoAdicionalCategoriaRepository extends JpaRepository<CostoAdicionalCategoria, Long> {
    List<CostoAdicionalCategoria> findByProyectoIdOrderByNombreAsc(Long proyectoId);

    Optional<CostoAdicionalCategoria> findByProyectoIdAndNombreIgnoreCase(Long proyectoId, String nombre);

    Optional<CostoAdicionalCategoria> findByIdAndProyectoId(Long id, Long proyectoId);

    void deleteByProyectoId(Long proyectoId);
}
