package com.trazabilidad.ayni.proyecto;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProyectoResponsableHistorialRepository extends JpaRepository<ProyectoResponsableHistorial, Long> {

    List<ProyectoResponsableHistorial> findByProyectoIdOrderByFechaCambioDescIdDesc(Long proyectoId);

    Optional<ProyectoResponsableHistorial> findFirstByProyectoIdOrderByFechaCambioDescIdDesc(Long proyectoId);
}
