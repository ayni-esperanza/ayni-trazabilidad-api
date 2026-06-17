package com.trazabilidad.ayni.costo.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CostoCatalogoRequest {

    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;
}
