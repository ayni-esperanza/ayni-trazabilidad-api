package com.trazabilidad.ayni.proyecto;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrdenCompraRepository extends JpaRepository<OrdenCompra, Long> {
    List<OrdenCompra> findByProyectoId(Long proyectoId);
}
