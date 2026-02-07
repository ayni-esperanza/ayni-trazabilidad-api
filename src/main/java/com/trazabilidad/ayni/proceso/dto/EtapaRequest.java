package com.trazabilidad.ayni.proceso.dto;

import com.trazabilidad.ayni.shared.util.Constants;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para crear o actualizar una Etapa.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EtapaRequest {

    /**
     * ID de la etapa (usado solo en actualizaciones).
     */
    private Long id;

    @NotBlank(message = "El nombre de la etapa es obligatorio")
    @Size(max = 150, message = "El nombre no puede exceder 150 caracteres")
    private String nombre;

    @Size(max = 500, message = "La descripción no puede exceder 500 caracteres")
    private String descripcion;

    @NotNull(message = "El orden es obligatorio")
    @Min(value = 1, message = "El orden debe ser mayor o igual a 1")
    private Integer orden;

    /**
     * Si no se proporciona, se usa el color por defecto.
     */
    @Builder.Default
    @Size(max = 7, message = "El color debe ser un código hexadecimal válido (#RRGGBB)")
    private String color = Constants.Defaults.DEFAULT_COLOR_ETAPA;
}
