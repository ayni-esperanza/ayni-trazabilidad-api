package com.trazabilidad.ayni.proyecto;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ActividadProyectoRepository extends JpaRepository<ActividadProyecto, Long> {

    @EntityGraph(attributePaths = { "adjuntos", "siguientes" })
    List<ActividadProyecto> findByProyectoId(Long proyectoId);
}
