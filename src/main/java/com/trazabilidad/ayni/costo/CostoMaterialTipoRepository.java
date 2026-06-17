package com.trazabilidad.ayni.costo;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CostoMaterialTipoRepository extends JpaRepository<CostoMaterialTipo, Long> {
    List<CostoMaterialTipo> findAllByOrderByNombreAsc();

    Optional<CostoMaterialTipo> findByNombreIgnoreCase(String nombre);
}
