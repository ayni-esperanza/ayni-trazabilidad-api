package com.trazabilidad.ayni.costo;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CostoManoObraOficioRepository extends JpaRepository<CostoManoObraOficio, Long> {
    List<CostoManoObraOficio> findAllByOrderByNombreAsc();

    Optional<CostoManoObraOficio> findByNombreIgnoreCase(String nombre);
}
