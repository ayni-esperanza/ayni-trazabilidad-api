package com.trazabilidad.ayni.costo;

import com.trazabilidad.ayni.costo.dto.CostoCatalogoRequest;
import com.trazabilidad.ayni.proyecto.Proyecto;
import com.trazabilidad.ayni.proyecto.ProyectoLifecycleService;
import com.trazabilidad.ayni.proyecto.ProyectoRepository;
import com.trazabilidad.ayni.shared.exception.BadRequestException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CostoServiceTest {

    @Mock
    private CostoMaterialRepository costoMaterialRepository;

    @Mock
    private CostoManoObraRepository costoManoObraRepository;

    @Mock
    private CostoAdicionalRepository costoAdicionalRepository;

    @Mock
    private CostoAdicionalCategoriaRepository costoAdicionalCategoriaRepository;

    @Mock
    private CostoMaterialTipoRepository costoMaterialTipoRepository;

    @Mock
    private CostoManoObraOficioRepository costoManoObraOficioRepository;

    @Mock
    private ProyectoRepository proyectoRepository;

    @Mock
    private ProyectoLifecycleService proyectoLifecycleService;

    @InjectMocks
    private CostoService costoService;

    @Test
    void registrarTipoMaterialReutilizaCatalogoGlobalExistente() {
        Proyecto proyecto = proyecto(1L);
        CostoMaterialTipo existente = CostoMaterialTipo.builder()
                .id(5L)
                .nombre("Tipo A")
                .build();

        when(proyectoRepository.findById(1L)).thenReturn(Optional.of(proyecto));
        when(costoMaterialTipoRepository.findByNombreIgnoreCase("Tipo A")).thenReturn(Optional.of(existente));

        var response = costoService.registrarTipoMaterial(1L, new CostoCatalogoRequest("Tipo A"));

        assertEquals(5L, response.getId());
        assertEquals("Tipo A", response.getNombre());
        verify(costoMaterialTipoRepository, never()).save(any(CostoMaterialTipo.class));
        verify(proyectoLifecycleService).marcarProyectoComoModificado(proyecto);
    }

    @Test
    void actualizarTipoMaterialPropagaCambioATodosLosProyectosRelacionados() {
        Proyecto proyectoOrigen = proyecto(1L);
        CostoMaterialTipo tipo = CostoMaterialTipo.builder()
                .id(10L)
                .nombre("Tipo Antiguo")
                .build();

        CostoMaterial materialProyectoUno = material(100L, 1L, "Tipo Antiguo", tipo);
        CostoMaterial materialProyectoDos = material(101L, 2L, "Tipo Antiguo", null);

        when(proyectoRepository.findById(1L)).thenReturn(Optional.of(proyectoOrigen));
        when(costoMaterialTipoRepository.findById(10L)).thenReturn(Optional.of(tipo));
        when(costoMaterialTipoRepository.findByNombreIgnoreCase("Tipo Nuevo")).thenReturn(Optional.empty());
        when(costoMaterialTipoRepository.save(tipo)).thenReturn(tipo);
        when(costoMaterialRepository.findByTipoIgnoreCase("Tipo Antiguo")).thenReturn(List.of(materialProyectoUno, materialProyectoDos));
        when(costoMaterialRepository.findByTipoMaterialId(10L)).thenReturn(List.of(materialProyectoUno));

        var response = costoService.actualizarTipoMaterial(1L, 10L, new CostoCatalogoRequest("Tipo Nuevo"));

        assertEquals("Tipo Nuevo", response.getNombre());
        assertEquals("Tipo Nuevo", materialProyectoUno.getTipo());
        assertEquals("Tipo Nuevo", materialProyectoDos.getTipo());
        assertEquals(tipo, materialProyectoUno.getTipoMaterial());
        assertEquals(tipo, materialProyectoDos.getTipoMaterial());
        verify(costoMaterialRepository).saveAll(any());
        verify(proyectoLifecycleService).marcarProyectoComoModificado(1L);
        verify(proyectoLifecycleService).marcarProyectoComoModificado(2L);
    }

    @Test
    void eliminarOficioGlobalEnUsoLanzaError() {
        Proyecto proyecto = proyecto(1L);
        CostoManoObraOficio oficio = CostoManoObraOficio.builder()
                .id(7L)
                .nombre("Soldador")
                .build();

        when(proyectoRepository.findById(1L)).thenReturn(Optional.of(proyecto));
        when(costoManoObraOficioRepository.findById(7L)).thenReturn(Optional.of(oficio));
        when(costoManoObraRepository.findByFuncionIgnoreCase("Soldador"))
                .thenReturn(List.of(CostoManoObra.builder()
                        .id(9L)
                        .trabajador("Juan")
                        .funcion("Soldador")
                        .costoHora(BigDecimal.ONE)
                        .proyecto(proyecto)
                        .build()));

        assertThrows(BadRequestException.class, () -> costoService.eliminarOficioManoObra(1L, 7L));
    }

    private Proyecto proyecto(Long id) {
        Proyecto proyecto = new Proyecto();
        proyecto.setId(id);
        return proyecto;
    }

    private CostoMaterial material(Long id, Long proyectoId, String tipoNombre, CostoMaterialTipo tipoMaterial) {
        return CostoMaterial.builder()
                .id(id)
                .material("Material")
                .tipo(tipoNombre)
                .tipoMaterial(tipoMaterial)
                .cantidad(BigDecimal.ONE)
                .costoUnitario(BigDecimal.ONE)
                .proyecto(proyecto(proyectoId))
                .build();
    }
}
