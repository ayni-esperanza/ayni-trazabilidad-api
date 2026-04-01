package com.trazabilidad.ayni.firma.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FirmaRequest {

    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;

    private String cargo;

    @NotBlank(message = "La imagen de firma es obligatoria")
    private String imagenBase64;
}
