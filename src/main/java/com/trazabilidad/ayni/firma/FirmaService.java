package com.trazabilidad.ayni.firma;

import com.trazabilidad.ayni.firma.dto.FirmaRequest;
import com.trazabilidad.ayni.firma.dto.FirmaResponse;
import com.trazabilidad.ayni.shared.exception.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class FirmaService {

    private final FirmaRepository firmaRepository;

    @Transactional(readOnly = true)
    public List<FirmaResponse> listar() {
        return firmaRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public FirmaResponse obtenerPorId(Long id) {
        return toResponse(firmaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Firma", id)));
    }

    public FirmaResponse crear(FirmaRequest request) {
        Firma entity = Firma.builder()
                .nombre(request.getNombre())
                .cargo(request.getCargo())
                .imagenBase64(request.getImagenBase64())
                .activo(true)
                .build();
        return toResponse(firmaRepository.save(entity));
    }

    public FirmaResponse actualizar(Long id, FirmaRequest request) {
        Firma firma = firmaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Firma", id));
        firma.setNombre(request.getNombre());
        firma.setCargo(request.getCargo());
        firma.setImagenBase64(request.getImagenBase64());
        return toResponse(firmaRepository.save(firma));
    }

    public void eliminar(Long id) {
        Firma firma = firmaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Firma", id));
        firmaRepository.delete(firma);
    }

    public FirmaResponse cambiarEstado(Long id, boolean activo) {
        Firma firma = firmaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Firma", id));
        firma.setActivo(activo);
        return toResponse(firmaRepository.save(firma));
    }

    private FirmaResponse toResponse(Firma firma) {
        return FirmaResponse.builder()
                .id(firma.getId())
                .nombre(firma.getNombre())
                .cargo(firma.getCargo())
                .imagenBase64(firma.getImagenBase64())
                .fechaCreacion(firma.getFechaCreacion())
                .activo(firma.getActivo())
                .usuarioId(firma.getUsuarioId())
                .build();
    }
}
