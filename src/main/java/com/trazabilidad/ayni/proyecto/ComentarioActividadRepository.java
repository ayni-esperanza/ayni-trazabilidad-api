package com.trazabilidad.ayni.proyecto;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ComentarioActividadRepository extends JpaRepository<ComentarioActividad, Long> {
    List<ComentarioActividad> findByProyectoIdOrderByIdAsc(Long proyectoId);

    Optional<ComentarioActividad> findByIdAndProyectoId(Long id, Long proyectoId);
}
