package com.trazabilidad.ayni.costo;

import com.trazabilidad.ayni.costo.dto.*;
import com.trazabilidad.ayni.proyecto.Proyecto;
import com.trazabilidad.ayni.proyecto.ProyectoLifecycleService;
import com.trazabilidad.ayni.proyecto.ProyectoRepository;
import com.trazabilidad.ayni.shared.exception.BadRequestException;
import com.trazabilidad.ayni.shared.exception.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

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
    private final CostoAdicionalCategoriaRepository costoAdicionalCategoriaRepository;
    private final CostoMaterialTipoRepository costoMaterialTipoRepository;
    private final CostoManoObraOficioRepository costoManoObraOficioRepository;
    private final ProyectoRepository proyectoRepository;
    private final ProyectoLifecycleService proyectoLifecycleService;

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

    @Transactional(readOnly = true)
    public List<String> obtenerTiposMaterial(Long proyectoId) {
        validarProyectoExiste(proyectoId);
        List<String> tiposPersistidos = costoMaterialTipoRepository.findAllByOrderByNombreAsc().stream()
                .map(CostoMaterialTipo::getNombre)
                .toList();
        List<String> tiposRelacionados = costoMaterialRepository.findDistinctTiposRelacionados();
        List<String> tiposLegacy = costoMaterialRepository.findDistinctTipos();
        return combinarNombresCatalogo(tiposPersistidos, combinarNombresCatalogo(tiposRelacionados, tiposLegacy));
    }

    @Transactional(readOnly = true)
    public List<CostoCatalogoResponse> obtenerTiposMaterialPersistidos(Long proyectoId) {
        validarProyectoExiste(proyectoId);
        return costoMaterialTipoRepository.findAllByOrderByNombreAsc().stream()
                .map(this::toCatalogoResponse)
                .toList();
    }

    public CostoCatalogoResponse registrarTipoMaterial(Long proyectoId, CostoCatalogoRequest request) {
        Proyecto proyecto = obtenerProyecto(proyectoId);
        String nombre = normalizarNombreCatalogo(request.getNombre());

        CostoMaterialTipo tipo = costoMaterialTipoRepository.findByNombreIgnoreCase(nombre)
                .orElseGet(() -> costoMaterialTipoRepository.save(CostoMaterialTipo.builder()
                        .nombre(nombre)
                        .build()));

        proyectoLifecycleService.marcarProyectoComoModificado(proyecto);
        return toCatalogoResponse(tipo);
    }

    public CostoCatalogoResponse actualizarTipoMaterial(Long proyectoId, Long tipoId, CostoCatalogoRequest request) {
        obtenerProyecto(proyectoId);
        CostoMaterialTipo tipo = costoMaterialTipoRepository.findById(tipoId)
                .orElseThrow(() -> new EntityNotFoundException("CostoMaterialTipo", tipoId));
        String nombreNuevo = normalizarNombreCatalogo(request.getNombre());

        costoMaterialTipoRepository.findByNombreIgnoreCase(nombreNuevo)
                .filter(existente -> !Objects.equals(existente.getId(), tipoId))
                .ifPresent(existente -> {
                    throw new BadRequestException("Ya existe un tipo de material con ese nombre en el sistema");
                });

        String nombreAnterior = tipo.getNombre();
        tipo.setNombre(nombreNuevo);
        tipo = costoMaterialTipoRepository.save(tipo);
        actualizarTiposMaterialRelacionados(proyectoId, tipo, nombreAnterior, nombreNuevo);
        return toCatalogoResponse(tipo);
    }

    public void eliminarTipoMaterial(Long proyectoId, Long tipoId) {
        obtenerProyecto(proyectoId);
        CostoMaterialTipo tipo = costoMaterialTipoRepository.findById(tipoId)
                .orElseThrow(() -> new EntityNotFoundException("CostoMaterialTipo", tipoId));
        if (!costoMaterialRepository.findByTipoMaterialId(tipoId).isEmpty()
                || !costoMaterialRepository.findByTipoIgnoreCase(tipo.getNombre()).isEmpty()) {
            throw new BadRequestException("No se puede eliminar el tipo de material porque está en uso");
        }
        costoMaterialTipoRepository.delete(tipo);
        proyectoLifecycleService.marcarProyectoComoModificado(proyectoId);
    }

    /**
     * Registra un nuevo costo de material.
     */
    public CostoMaterialResponse registrarMaterial(Long proyectoId, CostoMaterialRequest request) {
        Proyecto proyecto = obtenerProyecto(proyectoId);

        CostoMaterial material = CostoMapper.toMaterialEntity(request, proyecto);
        asignarTipoMaterial(material, proyecto, request);
        material = costoMaterialRepository.save(material);

        proyectoLifecycleService.marcarProyectoComoModificado(proyecto);
        return CostoMapper.toMaterialResponse(material);
    }

    /**
     * Registra múltiples costos de material en batch.
     */
    public List<CostoMaterialResponse> registrarMateriales(Long proyectoId, List<CostoMaterialRequest> requests) {
        Proyecto proyecto = obtenerProyecto(proyectoId);

        List<CostoMaterial> materiales = requests.stream()
                .map(request -> {
                    CostoMaterial material = CostoMapper.toMaterialEntity(request, proyecto);
                    asignarTipoMaterial(material, proyecto, request);
                    return material;
                })
                .toList();

        materiales = costoMaterialRepository.saveAll(materiales);

        proyectoLifecycleService.marcarProyectoComoModificado(proyecto);
        return CostoMapper.toMaterialResponseList(materiales);
    }

    /**
     * Actualiza un costo de material existente.
     */
    public CostoMaterialResponse actualizarMaterial(Long proyectoId, Long id, CostoMaterialRequest request) {
        CostoMaterial material = costoMaterialRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("CostoMaterial", id));
        if (!material.getProyecto().getId().equals(proyectoId)) {
            throw new EntityNotFoundException("CostoMaterial", id);
        }

        CostoMapper.updateMaterialEntity(material, request);
        asignarTipoMaterial(material, material.getProyecto(), request);
        material = costoMaterialRepository.save(material);

        proyectoLifecycleService.marcarProyectoComoModificado(material.getProyecto());
        return CostoMapper.toMaterialResponse(material);
    }

    /**
     * Elimina un costo de material.
     */
    public void eliminarMaterial(Long proyectoId, Long id) {
        CostoMaterial material = costoMaterialRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("CostoMaterial", id));
        if (!material.getProyecto().getId().equals(proyectoId)) {
            throw new EntityNotFoundException("CostoMaterial", id);
        }
        costoMaterialRepository.delete(material);
        proyectoLifecycleService.marcarProyectoComoModificado(proyectoId);
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

    @Transactional(readOnly = true)
    public List<String> obtenerOficiosManoObra(Long proyectoId) {
        validarProyectoExiste(proyectoId);
        List<String> oficiosPersistidos = costoManoObraOficioRepository.findAllByOrderByNombreAsc().stream()
                .map(CostoManoObraOficio::getNombre)
                .toList();
        List<String> oficiosRegistrados = costoManoObraRepository.findDistinctOficios();
        return combinarNombresCatalogo(oficiosPersistidos, oficiosRegistrados);
    }

    @Transactional(readOnly = true)
    public List<CostoCatalogoResponse> obtenerOficiosManoObraPersistidos(Long proyectoId) {
        validarProyectoExiste(proyectoId);
        return costoManoObraOficioRepository.findAllByOrderByNombreAsc().stream()
                .map(this::toCatalogoResponse)
                .toList();
    }

    public CostoCatalogoResponse registrarOficioManoObra(Long proyectoId, CostoCatalogoRequest request) {
        Proyecto proyecto = obtenerProyecto(proyectoId);
        String nombre = normalizarNombreCatalogo(request.getNombre());

        CostoManoObraOficio oficio = costoManoObraOficioRepository.findByNombreIgnoreCase(nombre)
                .orElseGet(() -> costoManoObraOficioRepository.save(CostoManoObraOficio.builder()
                        .nombre(nombre)
                        .build()));

        proyectoLifecycleService.marcarProyectoComoModificado(proyecto);
        return toCatalogoResponse(oficio);
    }

    public CostoCatalogoResponse actualizarOficioManoObra(Long proyectoId, Long oficioId, CostoCatalogoRequest request) {
        obtenerProyecto(proyectoId);
        CostoManoObraOficio oficio = costoManoObraOficioRepository.findById(oficioId)
                .orElseThrow(() -> new EntityNotFoundException("CostoManoObraOficio", oficioId));
        String nombreNuevo = normalizarNombreCatalogo(request.getNombre());

        costoManoObraOficioRepository.findByNombreIgnoreCase(nombreNuevo)
                .filter(existente -> !Objects.equals(existente.getId(), oficioId))
                .ifPresent(existente -> {
                    throw new BadRequestException("Ya existe un oficio con ese nombre en el sistema");
                });

        String nombreAnterior = oficio.getNombre();
        oficio.setNombre(nombreNuevo);
        oficio = costoManoObraOficioRepository.save(oficio);
        actualizarOficiosRelacionados(proyectoId, nombreAnterior, nombreNuevo);
        return toCatalogoResponse(oficio);
    }

    public void eliminarOficioManoObra(Long proyectoId, Long oficioId) {
        obtenerProyecto(proyectoId);
        CostoManoObraOficio oficio = costoManoObraOficioRepository.findById(oficioId)
                .orElseThrow(() -> new EntityNotFoundException("CostoManoObraOficio", oficioId));
        if (!costoManoObraRepository.findByFuncionIgnoreCase(oficio.getNombre()).isEmpty()) {
            throw new BadRequestException("No se puede eliminar el oficio porque esta en uso");
        }
        costoManoObraOficioRepository.delete(oficio);
        proyectoLifecycleService.marcarProyectoComoModificado(proyectoId);
    }

    /**
     * Registra un nuevo costo de mano de obra.
     */
    public CostoManoObraResponse registrarManoObra(Long proyectoId, CostoManoObraRequest request) {
        Proyecto proyecto = obtenerProyecto(proyectoId);

        CostoManoObra manoObra = CostoMapper.toManoObraEntity(request, proyecto);
        manoObra = costoManoObraRepository.save(manoObra);

        proyectoLifecycleService.marcarProyectoComoModificado(proyecto);
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

        proyectoLifecycleService.marcarProyectoComoModificado(proyecto);
        return CostoMapper.toManoObraResponseList(manoObras);
    }

    /**
     * Actualiza un costo de mano de obra existente.
     */
    public CostoManoObraResponse actualizarManoObra(Long proyectoId, Long id, CostoManoObraRequest request) {
        CostoManoObra manoObra = costoManoObraRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("CostoManoObra", id));
        if (!manoObra.getProyecto().getId().equals(proyectoId)) {
            throw new EntityNotFoundException("CostoManoObra", id);
        }

        CostoMapper.updateManoObraEntity(manoObra, request);
        manoObra = costoManoObraRepository.save(manoObra);

        proyectoLifecycleService.marcarProyectoComoModificado(manoObra.getProyecto());
        return CostoMapper.toManoObraResponse(manoObra);
    }

    /**
     * Elimina un costo de mano de obra.
     */
    public void eliminarManoObra(Long proyectoId, Long id) {
        CostoManoObra manoObra = costoManoObraRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("CostoManoObra", id));
        if (!manoObra.getProyecto().getId().equals(proyectoId)) {
            throw new EntityNotFoundException("CostoManoObra", id);
        }
        costoManoObraRepository.delete(manoObra);
        proyectoLifecycleService.marcarProyectoComoModificado(proyectoId);
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
        List<String> categoriasItems = costoAdicionalRepository.findDistinctCategoriasByProyectoId(proyectoId);
        List<String> categoriasPersistidas = costoAdicionalCategoriaRepository.findByProyectoIdOrderByNombreAsc(proyectoId)
                .stream()
                .map(CostoAdicionalCategoria::getNombre)
                .toList();

        return combinarNombresCatalogo(categoriasPersistidas, categoriasItems);
    }

    @Transactional(readOnly = true)
    public List<CostoAdicionalCategoriaResponse> obtenerCategoriasPersistidas(Long proyectoId) {
        validarProyectoExiste(proyectoId);
        return costoAdicionalCategoriaRepository.findByProyectoIdOrderByNombreAsc(proyectoId).stream()
                .map(categoria -> CostoAdicionalCategoriaResponse.builder()
                        .id(categoria.getId())
                        .nombre(categoria.getNombre())
                        .build())
                .toList();
    }

    public CostoAdicionalCategoriaResponse registrarCategoria(Long proyectoId, CostoAdicionalCategoriaRequest request) {
        Proyecto proyecto = obtenerProyecto(proyectoId);
        String nombre = request.getNombre().trim();

        CostoAdicionalCategoria categoria = costoAdicionalCategoriaRepository
                .findByProyectoIdAndNombreIgnoreCase(proyectoId, nombre)
                .orElseGet(() -> costoAdicionalCategoriaRepository.save(CostoAdicionalCategoria.builder()
                        .proyecto(proyecto)
                        .nombre(nombre)
                        .build()));

        proyectoLifecycleService.marcarProyectoComoModificado(proyecto);
        return CostoAdicionalCategoriaResponse.builder()
                .id(categoria.getId())
                .nombre(categoria.getNombre())
                .build();
    }

    public void eliminarCategoria(Long proyectoId, Long categoriaId) {
        CostoAdicionalCategoria categoria = costoAdicionalCategoriaRepository.findByIdAndProyectoId(categoriaId, proyectoId)
                .orElseThrow(() -> new EntityNotFoundException("CostoAdicionalCategoria", categoriaId));

        List<CostoAdicional> relacionados = costoAdicionalRepository.findByProyectoIdAndCategoria(proyectoId, categoria.getNombre());
        if (!relacionados.isEmpty()) {
            costoAdicionalRepository.deleteAll(relacionados);
        }

        costoAdicionalCategoriaRepository.delete(categoria);
        proyectoLifecycleService.marcarProyectoComoModificado(proyectoId);
    }

    /**
     * Registra un nuevo costo adicional.
     */
    public CostoAdicionalResponse registrarAdicional(Long proyectoId, CostoAdicionalRequest request) {
        Proyecto proyecto = obtenerProyecto(proyectoId);

        CostoAdicional adicional = CostoMapper.toAdicionalEntity(request, proyecto);
        adicional = costoAdicionalRepository.save(adicional);

        proyectoLifecycleService.marcarProyectoComoModificado(proyecto);
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

        proyectoLifecycleService.marcarProyectoComoModificado(proyecto);
        return CostoMapper.toAdicionalResponseList(adicionales);
    }

    /**
     * Actualiza un costo adicional existente.
     */
    public CostoAdicionalResponse actualizarAdicional(Long proyectoId, Long id, CostoAdicionalRequest request) {
        CostoAdicional adicional = costoAdicionalRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("CostoAdicional", id));
        if (!adicional.getProyecto().getId().equals(proyectoId)) {
            throw new EntityNotFoundException("CostoAdicional", id);
        }

        CostoMapper.updateAdicionalEntity(adicional, request);
        adicional = costoAdicionalRepository.save(adicional);

        proyectoLifecycleService.marcarProyectoComoModificado(adicional.getProyecto());
        return CostoMapper.toAdicionalResponse(adicional);
    }

    /**
     * Elimina un costo adicional.
     */
    public void eliminarAdicional(Long proyectoId, Long id) {
        CostoAdicional adicional = costoAdicionalRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("CostoAdicional", id));
        if (!adicional.getProyecto().getId().equals(proyectoId)) {
            throw new EntityNotFoundException("CostoAdicional", id);
        }
        costoAdicionalRepository.delete(adicional);
        proyectoLifecycleService.marcarProyectoComoModificado(proyectoId);
    }

    // ==================== Resumen ====================

    /**
     * Obtiene el resumen completo de costos de un proyecto.
     * Usa queries SUM en BD para eficiencia (no carga todas las entidades).
     */
    @Transactional(readOnly = true)
    public ResumenCostoResponse obtenerResumen(Long proyectoId) {
        Proyecto proyecto = obtenerProyecto(proyectoId);

        BigDecimal totalMateriales = costoMaterialRepository.sumCostoTotalByProyectoId(proyectoId);
        BigDecimal totalManoObra = costoManoObraRepository.sumCostoTotalByProyectoId(proyectoId);
        BigDecimal totalAdicionales = costoAdicionalRepository.sumCostoTotalByProyectoId(proyectoId);

        long cantidadMateriales = costoMaterialRepository.countByProyectoId(proyectoId);
        long cantidadManoObra = costoManoObraRepository.countByProyectoId(proyectoId);
        long cantidadAdicionales = costoAdicionalRepository.countByProyectoId(proyectoId);

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

    private CostoCatalogoResponse toCatalogoResponse(CostoMaterialTipo tipo) {
        return CostoCatalogoResponse.builder()
                .id(tipo.getId())
                .nombre(tipo.getNombre())
                .build();
    }

    private CostoCatalogoResponse toCatalogoResponse(CostoManoObraOficio oficio) {
        return CostoCatalogoResponse.builder()
                .id(oficio.getId())
                .nombre(oficio.getNombre())
                .build();
    }

    private void actualizarTiposMaterialRelacionados(Long proyectoId, CostoMaterialTipo tipoActualizado, String nombreAnterior, String nombreNuevo) {
        if (nombreAnterior == null || Objects.equals(nombreAnterior, nombreNuevo)) {
            proyectoLifecycleService.marcarProyectoComoModificado(proyectoId);
            return;
        }

        java.util.LinkedHashMap<Long, CostoMaterial> materiales = new java.util.LinkedHashMap<>();
        for (CostoMaterial material : costoMaterialRepository.findByTipoIgnoreCase(nombreAnterior)) {
            materiales.put(material.getId(), material);
        }

        for (CostoMaterial material : costoMaterialRepository.findByTipoMaterialId(tipoActualizado.getId())) {
            materiales.put(material.getId(), material);
        }

        java.util.LinkedHashSet<Long> proyectosAfectados = new java.util.LinkedHashSet<>();
        for (CostoMaterial material : materiales.values()) {
            material.setTipo(nombreNuevo);
            material.setTipoMaterial(tipoActualizado);
            if (material.getProyecto() != null && material.getProyecto().getId() != null) {
                proyectosAfectados.add(material.getProyecto().getId());
            }
        }
        if (!materiales.isEmpty()) {
            costoMaterialRepository.saveAll(materiales.values());
        }

        marcarProyectosComoModificados(proyectoId, proyectosAfectados);
    }

    private void actualizarOficiosRelacionados(Long proyectoId, String nombreAnterior, String nombreNuevo) {
        if (nombreAnterior == null || Objects.equals(nombreAnterior, nombreNuevo)) {
            proyectoLifecycleService.marcarProyectoComoModificado(proyectoId);
            return;
        }

        List<CostoManoObra> manoObras = costoManoObraRepository.findByFuncionIgnoreCase(nombreAnterior);
        java.util.LinkedHashSet<Long> proyectosAfectados = new java.util.LinkedHashSet<>();
        for (CostoManoObra manoObra : manoObras) {
            manoObra.setFuncion(nombreNuevo);
            if (manoObra.getProyecto() != null && manoObra.getProyecto().getId() != null) {
                proyectosAfectados.add(manoObra.getProyecto().getId());
            }
        }
        if (!manoObras.isEmpty()) {
            costoManoObraRepository.saveAll(manoObras);
        }

        marcarProyectosComoModificados(proyectoId, proyectosAfectados);
    }

    private String normalizarNombreCatalogo(String nombre) {
        return nombre == null ? "" : nombre.trim();
    }

    private void asignarTipoMaterial(CostoMaterial material, Proyecto proyecto, CostoMaterialRequest request) {
        CostoMaterialTipo tipoMaterial = resolverTipoMaterial(proyecto, request);
        material.setTipoMaterial(tipoMaterial);
        String nombreTipo = tipoMaterial != null ? tipoMaterial.getNombre() : normalizarNombreCatalogo(request.getTipo());
        material.setTipo(nombreTipo.isBlank() ? null : nombreTipo);
    }

    private CostoMaterialTipo resolverTipoMaterial(Proyecto proyecto, CostoMaterialRequest request) {
        if (request == null || proyecto == null || proyecto.getId() == null) {
            return null;
        }

        if (request.getTipoId() != null) {
            return costoMaterialTipoRepository.findById(request.getTipoId())
                    .orElseThrow(() -> new EntityNotFoundException("CostoMaterialTipo", request.getTipoId()));
        }

        String nombreTipo = normalizarNombreCatalogo(request.getTipo());
        if (nombreTipo.isBlank()) {
            return null;
        }

        return costoMaterialTipoRepository.findByNombreIgnoreCase(nombreTipo)
                .orElseGet(() -> costoMaterialTipoRepository.save(CostoMaterialTipo.builder()
                        .nombre(nombreTipo)
                        .build()));
    }

    private void marcarProyectosComoModificados(Long proyectoIdOrigen, java.util.LinkedHashSet<Long> proyectoIds) {
        proyectoLifecycleService.marcarProyectoComoModificado(proyectoIdOrigen);
        for (Long proyectoId : proyectoIds) {
            if (proyectoId != null && !Objects.equals(proyectoId, proyectoIdOrigen)) {
                proyectoLifecycleService.marcarProyectoComoModificado(proyectoId);
            }
        }
    }

    private List<String> combinarNombresCatalogo(List<String> persistidos, List<String> registrados) {
        return Stream.concat(persistidos.stream(), registrados.stream())
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(nombre -> !nombre.isBlank())
                .distinct()
                .sorted(String::compareToIgnoreCase)
                .toList();
    }
}
