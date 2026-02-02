package com.trazabilidad.ayni.usuario.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para estadísticas de usuarios.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EstadisticasUsuariosResponse {

    /**
     * Total de usuarios en el sistema
     */
    private Long totalUsuarios;

    /**
     * Total de usuarios activos
     */
    private Long usuariosActivos;

    /**
     * Total de administradores y gerentes
     */
    private Long administradores;

    /**
     * Total de ingenieros y ayudantes
     */
    private Long ingenieros;
}
