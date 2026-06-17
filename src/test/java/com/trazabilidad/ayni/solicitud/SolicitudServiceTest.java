package com.trazabilidad.ayni.solicitud;

import com.trazabilidad.ayni.proyecto.Proyecto;
import com.trazabilidad.ayni.proyecto.ProyectoRepository;
import com.trazabilidad.ayni.proyecto.ProyectoService;
import com.trazabilidad.ayni.shared.enums.EstadoSolicitud;
import com.trazabilidad.ayni.shared.security.CurrentUserService;
import com.trazabilidad.ayni.usuario.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SolicitudServiceTest {

    @Mock
    private SolicitudRepository solicitudRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private ProyectoRepository proyectoRepository;

    @Mock
    private ProyectoService proyectoService;

    @Mock
    private CurrentUserService currentUserService;

    @InjectMocks
    private SolicitudService solicitudService;

    @BeforeEach
    void setUp() {
        when(currentUserService.isAdmin()).thenReturn(false);
        when(currentUserService.getCurrentUserId()).thenReturn(10L);
    }

    @ParameterizedTest
    @EnumSource(EstadoSolicitud.class)
    void eliminarPermiteSolicitudesEnCualquierEstado(EstadoSolicitud estado) {
        Long solicitudId = 1L;
        Solicitud solicitud = new Solicitud();
        solicitud.setId(solicitudId);
        solicitud.setEstado(estado);
        var responsable = new com.trazabilidad.ayni.usuario.Usuario();
        responsable.setId(10L);
        solicitud.setResponsable(responsable);

        when(solicitudRepository.findById(solicitudId)).thenReturn(Optional.of(solicitud));
        when(proyectoRepository.findBySolicitudId(solicitudId)).thenReturn(Optional.empty());

        assertDoesNotThrow(() -> solicitudService.eliminar(solicitudId));

        verify(solicitudRepository).delete(solicitud);
        verifyNoInteractions(proyectoService);
    }

    @Test
    void eliminarBorraPrimeroProyectoAsociadoYLuegoSolicitud() {
        Long solicitudId = 2L;
        Long proyectoId = 9L;

        Solicitud solicitud = new Solicitud();
        solicitud.setId(solicitudId);
        solicitud.setEstado(EstadoSolicitud.COMPLETADO);
        var responsable = new com.trazabilidad.ayni.usuario.Usuario();
        responsable.setId(10L);
        solicitud.setResponsable(responsable);

        Proyecto proyecto = new Proyecto();
        proyecto.setId(proyectoId);
        proyecto.setSolicitud(solicitud);

        when(solicitudRepository.findById(solicitudId)).thenReturn(Optional.of(solicitud));
        when(proyectoRepository.findBySolicitudId(solicitudId)).thenReturn(Optional.of(proyecto));

        solicitudService.eliminar(solicitudId);

        InOrder inOrder = inOrder(proyectoService, solicitudRepository);
        inOrder.verify(proyectoService).eliminarProyecto(proyectoId);
        inOrder.verify(solicitudRepository).delete(solicitud);
    }

    @Test
    void eliminarRechazaSolicitudAjenaParaUsuarioNoAdmin() {
        Long solicitudId = 3L;
        Solicitud solicitud = new Solicitud();
        solicitud.setId(solicitudId);
        solicitud.setEstado(EstadoSolicitud.EN_PROCESO);

        var creador = new com.trazabilidad.ayni.usuario.Usuario();
        creador.setId(99L);
        solicitud.setCreador(creador);

        when(solicitudRepository.findById(solicitudId)).thenReturn(Optional.of(solicitud));

        assertThrows(AccessDeniedException.class, () -> solicitudService.eliminar(solicitudId));

        verifyNoInteractions(proyectoRepository, proyectoService);
    }
}
