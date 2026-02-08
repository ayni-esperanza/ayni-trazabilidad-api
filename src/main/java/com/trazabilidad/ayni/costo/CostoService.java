package com.trazabilidad.ayni.costo;

import com.trazabilidad.ayni.costo.dto.*;
import com.trazabilidad.ayni.proyecto.Proyecto;
import com.trazabilidad.ayni.proyecto.ProyectoRepository;
import com.trazabilidad.ayni.shared.exception.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * Servicio para gestionar costos de proyectos.
 * Maneja los 3 tipos de costos: materiales, mano de obra y adicionales.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class CostoService {

    private final CostoMaterialRepository costoMaterialRepository;
    private final CostoManoObraRepository costoManoObraRepository;
    private final CostoAdicionalRepository costoAdicionalRepository;
    private final ProyectoRepository proyectoRepository;

    // ==================== CostoMaterial ====================

    /**
     * Obtiene todos los costos de material de un proyecto.
     */
    @Transactional(readOnly = true)
    public List<CostoMaterialResponse> obtenerMateriales(Long proyectoId) {
        validarProyectoExiste(proyectoId);
        List<CostoMaterial> materiales = costoMaterialRepository.findByProyectoId(proyectoId);
        return CostoMapper.toMaterialResponseList(materiales);
    }

    /**
     * Registra un nuevo costo de material.
     */
    public CostoMaterialResponse registrarMaterial(Long proyectoId, CostoMaterialRequest request) {
        Proyecto proyecto = obtenerProyecto(proyectoId);

        CostoMaterial material = CostoMapper.toMaterialEntity(request, proyecto);
        material = costoMaterialRepository.save(material);

        return CostoMapper.toMaterialResponse(material);
    }

    /**
     * Registra múltiples costos de material en batch.
     */
    public List<CostoMaterialResponse> registrarMateriales(Long proyectoId, List<CostoMaterialRequest> requests) {
        Proyecto proyecto = obtenerProyecto(proyectoId);

        List<CostoMaterial> materiales = requests.stream()
                .map(request -> CostoMapper.toMaterialEntity(request, proyecto))
                .toList();

        materiales = costoMaterialRepository.saveAll(materiales);

        return CostoMapper.toMaterialResponseList(materiales);
    }

    /**
     * Actualiza un costo de material existente.
     */
    public CostoMaterialResponse actualizarMaterial(Long id, CostoMaterialRequest request) {
        CostoMaterial material = costoMaterialRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("CostoMaterial", id));

        CostoMapper.updateMaterialEntity(material, request);
        material = costoMaterialRepository.save(material);

        return CostoMapper.toMaterialResponse(material);
    }

    /**
     * Elimina un costo de material.
     */
    public void eliminarMaterial(Long id) {
        if (!costoMaterialRepository.existsById(id)) {
            throw new EntityNotFoundException("CostoMaterial", id);
        }
        costoMaterialRepository.deleteById(id);
    }

    // ==================== CostoManoObra ====================

    /**
     * Obtiene todos los costos de mano de obra de un proyecto.
     */
    @Transactional(readOnly = true)
    public List<CostoManoObraResponse> obtenerManoObra(Long proyectoId) {
        validarProyectoExiste(proyectoId);
        List<CostoManoObra> manoObra = costoManoObraRepository.findByProyectoId(proyectoId);
        return CostoMapper.toManoObraResponseList(manoObra);
    }

    /**
     * Registra un nuevo costo de mano de obra.
     */
    public CostoManoObraResponse registrarManoObra(Long proyectoId, CostoManoObraRequest request) {
        Proyecto proyecto = obtenerProyecto(proyectoId);

        CostoManoObra manoObra = CostoMapper.toManoObraEntity(request, proyecto);
        manoObra = costoManoObraRepository.save(manoObra);

        return CostoMapper.toManoObraResponse(manoObra);
    }

    /**
     * Registra múltiples costos de mano de obra en batch.
     */
    public List<CostoManoObraResponse> registrarManoObras(Long proyectoId, List<CostoManoObraRequest> requests) {
        Proyecto proyecto = obtenerProyecto(proyectoId);

        List<CostoManoObra> manoObras = requests.stream()
                .map(request -> CostoMapper.toManoObraEntity(request, proyecto))
                .toList();

        manoObras = costoManoObraRepository.saveAll(manoObras);

        return CostoMapper.toManoObraResponseList(manoObras);
    }

    /**
     * Actualiza un costo de mano de obra existente.
     */
    public CostoManoObraResponse actualizarManoObra(Long id, CostoManoObraRequest request) {
        CostoManoObra manoObra = costoManoObraRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("CostoManoObra", id));

        CostoMapper.updateManoObraEntity(manoObra, request);
        manoObra = costoManoObraRepository.save(manoObra);

        return CostoMapper.toManoObraResponse(manoObra);
    }

    /**
     * Elimina un costo de mano de obra.
     */
    public void eliminarManoObra(Long id) {
        if (!costoManoObraRepository.existsById(id)) {
            throw new EntityNotFoundException("CostoManoObra", id);
        }
        costoManoObraRepository.deleteById(id);
    }

    // ==================== CostoAdicional ====================

    /**
     * Obtiene todos los costos adicionales de un proyecto.
     */
    @Transactional(readOnly = true)
    public List<CostoAdicionalResponse> obtenerAdicionales(Long proyectoId) {
        validarProyectoExiste(proyectoId);
        List<CostoAdicional> adicionales = costoAdicionalRepository.findByProyectoId(proyectoId);
        return CostoMapper.toAdicionalResponseList(adicionales);
    }

    /**
     * Obtiene categorías únicas de costos adicionales de un proyecto.
     */
    @Transactional(readOnly = true)
    public List<String> obtenerCategorias(Long proyectoId) {
        validarProyectoExiste(proyectoId);
        return costoAdicionalRepository.findDistinctCategoriasByProyectoId(proyectoId);
    }

    /**
     * Registra un nuevo costo adicional.
     */
    public CostoAdicionalResponse registrarAdicional(Long proyectoId, CostoAdicionalRequest request) {
        Proyecto proyecto = obtenerProyecto(proyectoId);

        CostoAdicional adicional = CostoMapper.toAdicionalEntity(request, proyecto);
        adicional = costoAdicionalRepository.save(adicional);

        return CostoMapper.toAdicionalResponse(adicional);
    }

    /**
     * Registra múltiples costos adicionales en batch.
     */
    public List<CostoAdicionalResponse> registrarAdicionales(Long proyectoId, List<CostoAdicionalRequest> requests) {
        Proyecto proyecto = obtenerProyecto(proyectoId);

        List<CostoAdicional> adicionales = requests.stream()
                .map(request -> CostoMapper.toAdicionalEntity(request, proyecto))
                .toList();

        adicionales = costoAdicionalRepository.saveAll(adicionales);

        return CostoMapper.toAdicionalResponseList(adicionales);
    }

    /**
     * Actualiza un costo adicional existente.
     */
    public CostoAdicionalResponse actualizarAdicional(Long id, CostoAdicionalRequest request) {
        CostoAdicional adicional = costoAdicionalRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("CostoAdicional", id));

        CostoMapper.updateAdicionalEntity(adicional, request);
        adicional = costoAdicionalRepository.save(adicional);

        return CostoMapper.toAdicionalResponse(adicional);
    }

    /**
     * Elimina un costo adicional.
     */
    public void eliminarAdicional(Long id) {
        if (!costoAdicionalRepository.existsById(id)) {
            throw new EntityNotFoundException("CostoAdicional", id);
        }
        costoAdicionalRepository.deleteById(id);
    }

    // ==================== Resumen ====================

    /**
     * Obtiene el resumen completo de costos de un proyecto.
     * Usa queries SUM en BD para eficiencia (no carga todas las entidades).
     */
    @Transactional(readOnly = true)
    public ResumenCostoResponse obtenerResumen(Long proyectoId) {
        Proyecto proyecto = obtenerProyecto(proyectoId);

        // Sumar costos con queries directas
        BigDecimal totalMateriales = costoMaterialRepository.sumCostoTotalByProyectoId(proyectoId);
        BigDecimal totalManoObra = costoManoObraRepository.sumCostoTotalByProyectoId(proyectoId);
        BigDecimal totalAdicionales = costoAdicionalRepository.sumCostoTotalByProyectoId(proyectoId);

        // Contar items
        long cantidadMateriales = costoMaterialRepository.countByProyectoId(proyectoId);
        long cantidadManoObra = costoManoObraRepository.countByProyectoId(proyectoId);
        long cantidadAdicionales = costoAdicionalRepository.countByProyectoId(proyectoId);

        // Calcular totales
        BigDecimal costoTotal = totalMateriales.add(totalManoObra).add(totalAdicionales);
        BigDecimal presupuesto = proyecto.getCosto();
        BigDecimal diferencia = presupuesto.subtract(costoTotal);

        return ResumenCostoResponse.builder()
                .totalMateriales(totalMateriales)
                .totalManoObra(totalManoObra)
                .totalAdicionales(totalAdicionales)
                .costoTotalProyecto(costoTotal)
                .presupuestoOriginal(presupuesto)
                .diferencia(diferencia)
                .cantidadItemsMateriales((int) cantidadMateriales)
                .cantidadItemsManoObra((int) cantidadManoObra)
                .cantidadItemsAdicionales((int) cantidadAdicionales)
                .proyectoId(proyecto.getId())
                .proyectoNombre(proyecto.getNombreProyecto())
                .build();
    }

    // ==================== Helpers ====================

    private void validarProyectoExiste(Long proyectoId) {
        if (!proyectoRepository.existsById(proyectoId)) {
            throw new EntityNotFoundException("Proyecto", proyectoId);
        }
    }

    private Proyecto obtenerProyecto(Long proyectoId) {
        return proyectoRepository.findById(proyectoId)
                .orElseThrow(() -> new EntityNotFoundException("Proyecto", proyectoId));
    }
}
