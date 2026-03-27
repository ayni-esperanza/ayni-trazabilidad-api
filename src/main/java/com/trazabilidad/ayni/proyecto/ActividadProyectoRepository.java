package com.trazabilidad.ayni.proyecto;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ActividadProyectoRepository extends JpaRepository<ActividadProyecto, Long> {

    List<ActividadProyecto> findByProyectoId(Long proyectoId);

    Optional<ActividadProyecto> findByProyectoIdAndId(Long proyectoId, Long id);
}
