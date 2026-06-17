package com.trazabilidad.ayni.proyecto;

import com.trazabilidad.ayni.proyecto.dto.ActividadProyectoRequest;
import com.trazabilidad.ayni.shared.security.CurrentUserService;
import com.trazabilidad.ayni.usuario.Usuario;
import com.trazabilidad.ayni.usuario.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ActividadProyectoServiceTest {

    @Mock
    private ProyectoRepository proyectoRepository;

    @Mock
    private ActividadProyectoRepository actividadProyectoRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private ComentarioActividadRepository comentarioActividadRepository;

    @Mock
    private CurrentUserService currentUserService;

    @Mock
    private ProyectoLifecycleService proyectoLifecycleService;

    @InjectMocks
    private ActividadProyectoService actividadProyectoService;

    @BeforeEach
    void setUp() {
        when(currentUserService.isAdmin()).thenReturn(false);
        when(currentUserService.getCurrentUserId()).thenReturn(10L);
    }

    @Test
    void actualizarRechazaActividadAjenaParaUsuarioNoAdmin() {
        Long proyectoId = 1L;
        Long actividadId = 2L;
        ActividadProyecto actividad = actividadConCreador(actividadId, 99L);

        when(actividadProyectoRepository.findByProyectoIdAndId(proyectoId, actividadId)).thenReturn(Optional.of(actividad));

        ActividadProyectoRequest request = ActividadProyectoRequest.builder()
                .nombre("Nueva actividad")
                .tipo("tarea")
                .build();

        assertThrows(AccessDeniedException.class, () -> actividadProyectoService.actualizar(proyectoId, actividadId, request));

        verifyNoInteractions(usuarioRepository);
    }

    @Test
    void sincronizarPermiteActividadesAjenasSinCambios() {
        Long proyectoId = 7L;
        ActividadProyecto actividad = actividadConCreador(5L, 99L);

        when(proyectoRepository.existsById(proyectoId)).thenReturn(true);
        when(actividadProyectoRepository.findByProyectoId(proyectoId)).thenReturn(List.of(actividad));
        when(actividadProyectoRepository.saveAll(List.of(actividad))).thenReturn(List.of(actividad));

        ActividadProyectoRequest request = ActividadProyectoRequest.builder()
                .id(actividad.getId())
                .nombre(actividad.getNombre())
                .tipo(actividad.getTipo())
                .estadoActividad(actividad.getEstadoActividad())
                .fechaCambioEstado(actividad.getFechaCambioEstado().toString())
                .responsableId(actividad.getResponsable().getId())
                .responsableNombre(actividad.getResponsableNombre())
                .fechaInicio(actividad.getFechaInicio().toString())
                .fechaFin(actividad.getFechaFin().toString())
                .descripcion(actividad.getDescripcion())
                .siguientesIds(List.of())
                .adjuntos(List.of())
                .build();

        assertDoesNotThrow(() -> actividadProyectoService.sincronizar(proyectoId, List.of(request)));

        verify(actividadProyectoRepository).saveAll(List.of(actividad));
        verifyNoInteractions(usuarioRepository);
    }

    @Test
    void sincronizarRechazaCambiosSobreActividadAjena() {
        Long proyectoId = 8L;
        ActividadProyecto actividad = actividadConCreador(6L, 99L);

        when(proyectoRepository.existsById(proyectoId)).thenReturn(true);
        when(actividadProyectoRepository.findByProyectoId(proyectoId)).thenReturn(List.of(actividad));

        ActividadProyectoRequest request = ActividadProyectoRequest.builder()
                .id(actividad.getId())
                .nombre("Actividad modificada")
                .tipo(actividad.getTipo())
                .estadoActividad(actividad.getEstadoActividad())
                .fechaCambioEstado(actividad.getFechaCambioEstado().toString())
                .responsableId(actividad.getResponsable().getId())
                .responsableNombre(actividad.getResponsableNombre())
                .fechaInicio(actividad.getFechaInicio().toString())
                .fechaFin(actividad.getFechaFin().toString())
                .descripcion(actividad.getDescripcion())
                .siguientesIds(List.of())
                .adjuntos(List.of())
                .build();

        assertThrows(AccessDeniedException.class, () -> actividadProyectoService.sincronizar(proyectoId, List.of(request)));
    }

    private ActividadProyecto actividadConCreador(Long actividadId, Long creadorId) {
        Usuario creador = new Usuario();
        creador.setId(creadorId);
        creador.setNombre("Creador");
        creador.setApellido("Prueba");

        Usuario responsable = new Usuario();
        responsable.setId(44L);
        responsable.setNombre("Responsable");
        responsable.setApellido("Actual");

        return ActividadProyecto.builder()
                .id(actividadId)
                .nombre("Actividad base")
                .tipo("tarea")
                .estadoActividad("Pendiente")
                .fechaCambioEstado(LocalDateTime.of(2026, 6, 15, 9, 30))
                .responsable(responsable)
                .responsableNombre("Responsable Actual")
                .creador(creador)
                .fechaRegistro(LocalDateTime.of(2026, 6, 15, 9, 0))
                .fechaInicio(LocalDate.of(2026, 6, 15))
                .fechaFin(LocalDate.of(2026, 6, 20))
                .descripcion("Descripcion original")
                .adjuntos(new ArrayList<>())
                .siguientes(new ArrayList<>())
                .build();
    }
}
