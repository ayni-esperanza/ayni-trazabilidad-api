package com.trazabilidad.ayni.costo.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CostoAdicionalCategoriaRequest {
    @NotBlank(message = "El nombre de la categoría es obligatorio")
    private String nombre;
}
