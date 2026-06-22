package com.trazabilidad.ayni.proyecto;

import com.trazabilidad.ayni.costo.CostoAdicionalCategoriaRepository;
import com.trazabilidad.ayni.proyecto.dto.ProyectoResponse;
import com.trazabilidad.ayni.proyecto.dto.ProyectoUpdateRequest;
import com.trazabilidad.ayni.shared.enums.EstadoProyecto;
import com.trazabilidad.ayni.shared.storage.StorageUrlResolver;
import com.trazabilidad.ayni.solicitud.SolicitudRepository;
import com.trazabilidad.ayni.usuario.Usuario;
import com.trazabilidad.ayni.usuario.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProyectoServiceTest {

    @Mock
    private ProyectoRepository proyectoRepository;

    @Mock
    private ProyectoResponsableHistorialRepository proyectoResponsableHistorialRepository;

    @Mock
    private SolicitudRepository solicitudRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private CostoAdicionalCategoriaRepository costoAdicionalCategoriaRepository;

    @Mock
    private StorageUrlResolver storageUrlResolver;

    @Mock
    private ProyectoLifecycleService proyectoLifecycleService;

    @InjectMocks
    private ProyectoService proyectoService;

    @Test
    void actualizarRegistraHistorialCuandoCambiaResponsable() {
        Usuario responsableActual = usuario(1L, "Ana", "Luna");
        Usuario responsableNuevo = usuario(2L, "Luis", "Prado");
        Proyecto proyecto = proyecto(responsableActual);

        when(proyectoRepository.findById(10L)).thenReturn(Optional.of(proyecto));
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(responsableNuevo));
        when(proyectoResponsableHistorialRepository.findFirstByProyectoIdOrderByFechaCambioDescIdDesc(10L))
                .thenReturn(Optional.empty());
        when(proyectoRepository.save(any(Proyecto.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ProyectoResponse response = proyectoService.actualizar(10L, ProyectoUpdateRequest.builder()
                .responsableId(2L)
                .build());

        ArgumentCaptor<ProyectoResponsableHistorial> captor = ArgumentCaptor.forClass(ProyectoResponsableHistorial.class);
        verify(proyectoResponsableHistorialRepository).save(captor.capture());
        verify(proyectoLifecycleService).prepararProyectoParaModificacion(proyecto);

        ProyectoResponsableHistorial historial = captor.getValue();
        assertEquals(10L, historial.getProyecto().getId());
        assertEquals(1L, historial.getResponsableAnteriorId());
        assertEquals("Ana Luna", historial.getResponsableAnteriorNombre());
        assertEquals(2L, historial.getResponsableNuevoId());
        assertEquals("Luis Prado", historial.getResponsableNuevoNombre());

        assertEquals(2L, proyecto.getResponsable().getId());
        assertEquals("Luis Prado", proyecto.getResponsableNombre());
        assertEquals(1L, proyecto.getResponsableAnteriorId());
        assertEquals("Ana Luna", proyecto.getResponsableAnteriorNombre());

        assertEquals(2L, response.getResponsableId());
        assertEquals("Luis Prado", response.getResponsableNombre());
        assertEquals(1L, response.getResponsableAnteriorId());
        assertEquals("Ana Luna", response.getResponsableAnteriorNombre());
    }

    @Test
    void actualizarNoRegistraHistorialDuplicadoSiLlegaDobleSolicitudInmediata() {
        Usuario responsableActual = usuario(1L, "Ana", "Luna");
        Usuario responsableNuevo = usuario(2L, "Luis", "Prado");
        Proyecto proyecto = proyecto(responsableActual);

        when(proyectoRepository.findById(10L)).thenReturn(Optional.of(proyecto));
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(responsableNuevo));
        when(proyectoResponsableHistorialRepository.findFirstByProyectoIdOrderByFechaCambioDescIdDesc(10L))
                .thenReturn(Optional.of(ProyectoResponsableHistorial.builder()
                        .id(99L)
                        .proyecto(proyecto)
                        .responsableAnteriorId(1L)
                        .responsableAnteriorNombre("Ana Luna")
                        .responsableNuevoId(2L)
                        .responsableNuevoNombre("Luis Prado")
                        .fechaCambio(LocalDateTime.now().minusSeconds(1))
                        .build()));
        when(proyectoRepository.save(any(Proyecto.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ProyectoResponse response = proyectoService.actualizar(10L, ProyectoUpdateRequest.builder()
                .responsableId(2L)
                .build());

        verify(proyectoResponsableHistorialRepository, never()).save(any(ProyectoResponsableHistorial.class));
        verify(proyectoLifecycleService).prepararProyectoParaModificacion(proyecto);

        assertEquals(2L, response.getResponsableId());
        assertEquals("Luis Prado", response.getResponsableNombre());
        assertEquals(1L, response.getResponsableAnteriorId());
        assertEquals("Ana Luna", response.getResponsableAnteriorNombre());
    }

    @Test
    void actualizarNoRegistraHistorialSiResponsableNoCambia() {
        Usuario responsableActual = usuario(1L, "Ana", "Luna");
        Proyecto proyecto = proyecto(responsableActual);

        when(proyectoRepository.findById(10L)).thenReturn(Optional.of(proyecto));
        when(proyectoRepository.save(any(Proyecto.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ProyectoResponse response = proyectoService.actualizar(10L, ProyectoUpdateRequest.builder()
                .responsableId(1L)
                .build());

        verify(proyectoResponsableHistorialRepository, never()).save(any(ProyectoResponsableHistorial.class));
        verify(usuarioRepository, never()).findById(1L);
        verify(proyectoLifecycleService).prepararProyectoParaModificacion(proyecto);

        assertEquals(1L, response.getResponsableId());
        assertEquals("Ana Luna", response.getResponsableNombre());
        assertNull(response.getResponsableAnteriorId());
        assertNull(response.getResponsableAnteriorNombre());
    }

    private Proyecto proyecto(Usuario responsable) {
        return Proyecto.builder()
                .id(10L)
                .nombreProyecto("Proyecto demo")
                .cliente("Cliente")
                .costo(BigDecimal.valueOf(1500))
                .descripcion("Descripcion")
                .fechaRegistro(LocalDate.of(2026, 6, 10))
                .fechaInicio(LocalDate.of(2026, 6, 10))
                .fechaFinalizacion(LocalDate.of(2026, 7, 10))
                .estado(EstadoProyecto.EN_PROCESO)
                .responsable(responsable)
                .responsableNombre(responsable.getNombreCompleto())
                .ordenesCompra(new ArrayList<>())
                .actividades(new ArrayList<>())
                .comentariosAdicionalesActividad(new ArrayList<>())
                .areas(new ArrayList<>())
                .build();
    }

    private Usuario usuario(Long id, String nombre, String apellido) {
        Usuario usuario = new Usuario();
        usuario.setId(id);
        usuario.setNombre(nombre);
        usuario.setApellido(apellido);
        return usuario;
    }
}
