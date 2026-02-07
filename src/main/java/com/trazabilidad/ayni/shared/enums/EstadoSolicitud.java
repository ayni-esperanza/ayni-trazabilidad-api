package com.trazabilidad.ayni.shared.enums;

import com.trazabilidad.ayni.shared.exception.BadStateTransitionException;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * Enum que define los estados posibles de una Solicitud.
 * Implementa Strategy Pattern para validar transiciones de estado.
 */
@Getter
public enum EstadoSolicitud {

    PENDIENTE("Pendiente", Set.of("EN_PROCESO", "CANCELADO")),
    EN_PROCESO("En Proceso", Set.of("COMPLETADO", "CANCELADO")),
    COMPLETADO("Completado", Set.of("FINALIZADO")),
    CANCELADO("Cancelado", Set.of()),
    FINALIZADO("Finalizado", Set.of());

    private final String displayName;
    private final Set<String> transicionesPermitidas;

    EstadoSolicitud(String displayName, Set<String> transicionesPermitidas) {
        this.displayName = displayName;
        this.transicionesPermitidas = transicionesPermitidas;
    }

    /**
     * Valida si la transición al nuevo estado es permitida.
     * Implementación del Strategy Pattern para control de flujo de estados.
     *
     * @param nuevoEstado Estado al que se quiere transicionar
     * @throws BadStateTransitionException si la transición no es válida
     */
    public void validarTransicion(EstadoSolicitud nuevoEstado) {
        if (this == nuevoEstado) {
            return; // Mismo estado, no hay transición
        }

        if (!transicionesPermitidas.contains(nuevoEstado.name())) {
            throw new BadStateTransitionException(
                    "Solicitud",
                    this.name(),
                    nuevoEstado.name(),
                    getTransicionesPermitidasList());
        }
    }

    /**
     * Retorna la lista de transiciones permitidas como lista de enums.
     */
    public List<EstadoSolicitud> getTransicionesPermitidasList() {
        return transicionesPermitidas.stream()
                .map(EstadoSolicitud::valueOf)
                .toList();
    }

    /**
     * Verifica si el estado es terminal (no permite más transiciones).
     */
    public boolean esEstadoTerminal() {
        return transicionesPermitidas.isEmpty();
    }

    /**
     * Retorna todos los estados disponibles como strings.
     */
    public static List<String> getValoresDisponibles() {
        return Arrays.stream(values())
                .map(EstadoSolicitud::name)
                .toList();
    }
}
