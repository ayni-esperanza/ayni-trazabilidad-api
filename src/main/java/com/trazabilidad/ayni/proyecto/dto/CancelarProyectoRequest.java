package com.trazabilidad.ayni.proyecto.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CancelarProyectoRequest {
    @NotBlank(message = "El motivo de cancelación es obligatorio")
    private String motivo;
}
