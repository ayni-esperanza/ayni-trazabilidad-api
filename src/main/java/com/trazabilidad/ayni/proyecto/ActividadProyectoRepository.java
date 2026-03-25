package com.trazabilidad.ayni.proyecto;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ActividadProyectoRepository extends JpaRepository<ActividadProyecto, Long> {

    @EntityGraph(attributePaths = { "adjuntos", "siguientes" })
    List<ActividadProyecto> findByProyectoId(Long proyectoId);

    @EntityGraph(attributePaths = { "adjuntos", "siguientes" })
    Optional<ActividadProyecto> findByProyectoIdAndId(Long proyectoId, Long id);
}
