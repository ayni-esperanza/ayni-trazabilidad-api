package com.trazabilidad.ayni.shared.exception;

import lombok.Getter;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Excepción lanzada cuando se intenta realizar una transición de estado inválida.
 * Extiende BadRequestException para mantener consistencia con el manejo de errores existente.
 */
@Getter
public class BadStateTransitionException extends BadRequestException {
    
    private final String entidad;
    private final String estadoActual;
    private final String estadoIntentado;
    private final List<?> transicionesPermitidas;
    
    /**
     * Constructor con información detallada de la transición fallida.
     *
     * @param entidad Nombre de la entidad (ej: "Solicitud", "Proyecto")
     * @param estadoActual Estado actual del registro
     * @param estadoIntentado Estado al que se intentó transicionar
     * @param transicionesPermitidas Lista de estados válidos para la transición
     */
    public BadStateTransitionException(
            String entidad,
            String estadoActual,
            String estadoIntentado,
            List<?> transicionesPermitidas) {
        super(construirMensaje(entidad, estadoActual, estadoIntentado, transicionesPermitidas));
        this.entidad = entidad;
        this.estadoActual = estadoActual;
        this.estadoIntentado = estadoIntentado;
        this.transicionesPermitidas = transicionesPermitidas;
    }
    
    /**
     * Construye un mensaje descriptivo para el usuario.
     */
    private static String construirMensaje(
            String entidad,
            String estadoActual,
            String estadoIntentado,
            List<?> transicionesPermitidas) {
        
        if (transicionesPermitidas.isEmpty()) {
            return String.format(
                    "%s con estado '%s' es terminal y no permite transiciones. " +
                    "No se puede cambiar a '%s'.",
                    entidad,
                    estadoActual,
                    estadoIntentado
            );
        }
        
        String estadosPermitidos = transicionesPermitidas.stream()
                .map(Object::toString)
                .collect(Collectors.joining(", "));
        
        return String.format(
                "Transición de estado inválida para %s. " +
                "Estado actual: '%s'. Estado intentado: '%s'. " +
                "Transiciones permitidas: [%s]",
                entidad,
                estadoActual,
                estadoIntentado,
                estadosPermitidos
        );
    }
}
