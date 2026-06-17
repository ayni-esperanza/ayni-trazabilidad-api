package com.trazabilidad.ayni.proyecto;

import com.trazabilidad.ayni.proyecto.dto.ProyectoResponse;
import com.trazabilidad.ayni.shared.enums.EstadoProyecto;
import com.trazabilidad.ayni.shared.enums.EstadoSolicitud;
import com.trazabilidad.ayni.solicitud.Solicitud;
import com.trazabilidad.ayni.usuario.Usuario;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ProyectoMapperTest {

    @Test
    void toResponseUsaFechasDeSolicitudCuandoExisteRelacion() {
        Usuario responsable = usuario(7L, "Ana", "Luna");

        Solicitud solicitud = Solicitud.builder()
                .id(11L)
                .nombreProyecto("Proyecto base")
                .cliente("Cliente")
                .costo(BigDecimal.valueOf(1500))
                .responsable(responsable)
                .descripcion("Descripcion")
                .fechaSolicitud(LocalDate.of(2026, 6, 3))
                .estado(EstadoSolicitud.EN_PROCESO)
                .areas(new ArrayList<>())
                .build();
        solicitud.setFechaCreacion(LocalDateTime.of(2026, 6, 3, 8, 15));
        solicitud.setFechaActualizacion(LocalDateTime.of(2026, 6, 5, 14, 45));

        Proyecto proyecto = Proyecto.builder()
                .id(21L)
                .nombreProyecto("Proyecto base")
                .cliente("Cliente")
                .costo(BigDecimal.valueOf(1500))
                .descripcion("Descripcion")
                .fechaRegistro(LocalDate.of(2026, 6, 10))
                .fechaInicio(LocalDate.of(2026, 6, 4))
                .fechaFinalizacion(LocalDate.of(2026, 6, 30))
                .estado(EstadoProyecto.COMPLETADO)
                .solicitud(solicitud)
                .responsable(responsable)
                .responsableNombre(responsable.getNombreCompleto())
                .ordenesCompra(new ArrayList<>())
                .actividades(new ArrayList<>())
                .comentariosAdicionalesActividad(new ArrayList<>())
                .areas(new ArrayList<>())
                .build();
        proyecto.setFechaCreacion(LocalDateTime.of(2026, 6, 10, 9, 0));
        proyecto.setFechaActualizacion(LocalDateTime.of(2026, 6, 12, 16, 30));

        ProyectoResponse response = ProyectoMapper.toResponse(proyecto);

        assertEquals(LocalDate.of(2026, 6, 3), response.getFechaRegistro());
        assertEquals(LocalDateTime.of(2026, 6, 3, 8, 15), response.getFechaCreacion());
        assertEquals(LocalDateTime.of(2026, 6, 5, 14, 45), response.getFechaActualizacion());
    }

    @Test
    void toResponseConservaFechasDelProyectoCuandoNoHaySolicitud() {
        Usuario responsable = usuario(8L, "Luis", "Prado");

        Proyecto proyecto = Proyecto.builder()
                .id(22L)
                .nombreProyecto("Proyecto independiente")
                .cliente("Cliente 2")
                .costo(BigDecimal.valueOf(900))
                .descripcion("Descripcion")
                .fechaRegistro(LocalDate.of(2026, 6, 8))
                .fechaInicio(LocalDate.of(2026, 6, 8))
                .fechaFinalizacion(LocalDate.of(2026, 6, 28))
                .estado(EstadoProyecto.EN_PROCESO)
                .responsable(responsable)
                .responsableNombre(responsable.getNombreCompleto())
                .ordenesCompra(new ArrayList<>())
                .actividades(new ArrayList<>())
                .comentariosAdicionalesActividad(new ArrayList<>())
                .areas(new ArrayList<>())
                .build();
        proyecto.setFechaCreacion(LocalDateTime.of(2026, 6, 8, 10, 0));
        proyecto.setFechaActualizacion(LocalDateTime.of(2026, 6, 9, 11, 30));

        ProyectoResponse response = ProyectoMapper.toResponse(proyecto);

        assertEquals(LocalDate.of(2026, 6, 8), response.getFechaRegistro());
        assertEquals(LocalDateTime.of(2026, 6, 8, 10, 0), response.getFechaCreacion());
        assertEquals(LocalDateTime.of(2026, 6, 9, 11, 30), response.getFechaActualizacion());
    }

    private Usuario usuario(Long id, String nombre, String apellido) {
        Usuario usuario = new Usuario();
        usuario.setId(id);
        usuario.setNombre(nombre);
        usuario.setApellido(apellido);
        return usuario;
    }
}
