package com.trazabilidad.ayni.proceso.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * DTO para crear o actualizar un Proceso.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProcesoRequest {

    @NotBlank(message = "El nombre del proceso es obligatorio")
    @Size(max = 150, message = "El nombre no puede exceder 150 caracteres")
    private String nombre;

    @Size(max = 1000, message = "La descripción no puede exceder 1000 caracteres")
    private String descripcion;

    @NotBlank(message = "El área es obligatoria")
    @Size(max = 100, message = "El área no puede exceder 100 caracteres")
    private String area;

    /**
     * Lista de etapas del proceso.
     * Puede estar vacía al crear un proceso inicialmente.
     */
    @Builder.Default
    private List<EtapaRequest> etapas = new ArrayList<>();
}
