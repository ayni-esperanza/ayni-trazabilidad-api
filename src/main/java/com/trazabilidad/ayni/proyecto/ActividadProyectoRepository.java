package com.trazabilidad.ayni.proyecto;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ActividadProyectoRepository extends JpaRepository<ActividadProyecto, Long> {

    List<ActividadProyecto> findByProyectoId(Long proyectoId);

    Optional<ActividadProyecto> findByProyectoIdAndId(Long proyectoId, Long id);

    @Query("SELECT a FROM ActividadProyecto a WHERE a.responsable.id = :responsableId")
    List<ActividadProyecto> findByResponsableId(@Param("responsableId") Long responsableId);
}
