package com.trazabilidad.ayni.solicitud.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResponsableResponse {
    private Long id;
    private String nombre;
    private String cargo;
    private String email;
}
