package com.trazabilidad.ayni.proyecto;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrdenCompraRepository extends JpaRepository<OrdenCompra, Long> {
    List<OrdenCompra> findByProyectoId(Long proyectoId);
    Optional<OrdenCompra> findByProyectoIdAndId(Long proyectoId, Long id);
}
