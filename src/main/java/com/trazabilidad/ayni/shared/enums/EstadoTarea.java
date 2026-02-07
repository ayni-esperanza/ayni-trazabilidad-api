package com.trazabilidad.ayni.shared.enums;

import com.trazabilidad.ayni.shared.exception.BadStateTransitionException;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * Enum que define los estados posibles de una Tarea.
 * validar transiciones de estado.
 * CON_RETRASO se detecta automáticamente cuando fechaFin < now.
 */
@Getter
public enum EstadoTarea {

    PENDIENTE("Pendiente", Set.of("EN_PROGRESO", "BLOQUEADA", "CANCELADA")),
    EN_PROGRESO("En Progreso", Set.of("COMPLETADA", "BLOQUEADA", "CANCELADA", "CON_RETRASO")),
    COMPLETADA("Completada", Set.of()),
    BLOQUEADA("Bloqueada", Set.of("PENDIENTE", "EN_PROGRESO", "CANCELADA")),
    CON_RETRASO("Con Retraso", Set.of("COMPLETADA", "BLOQUEADA", "CANCELADA")),
    CANCELADA("Cancelada", Set.of());

    private final String displayName;
    private final Set<String> transicionesPermitidas;

    EstadoTarea(String displayName, Set<String> transicionesPermitidas) {
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
    public void validarTransicion(EstadoTarea nuevoEstado) {
        if (this == nuevoEstado) {
            return; // Mismo estado, no hay transición
        }

        if (!transicionesPermitidas.contains(nuevoEstado.name())) {
            throw new BadStateTransitionException(
                    "Tarea",
                    this.name(),
                    nuevoEstado.name(),
                    getTransicionesPermitidasList());
        }
    }

    /**
     * Retorna la lista de transiciones permitidas como lista de enums.
     */
    public List<EstadoTarea> getTransicionesPermitidasList() {
        return transicionesPermitidas.stream()
                .map(EstadoTarea::valueOf)
                .toList();
    }

    /**
     * Verifica si el estado es terminal (no permite más transiciones).
     */
    public boolean esEstadoTerminal() {
        return transicionesPermitidas.isEmpty();
    }

    /**
     * Verifica si la tarea está activa (ni completada ni cancelada).
     */
    public boolean estaActiva() {
        return this != COMPLETADA && this != CANCELADA;
    }

    /**
     * Retorna todos los estados disponibles como strings.
     */
    public static List<String> getValoresDisponibles() {
        return Arrays.stream(values())
                .map(EstadoTarea::name)
                .toList();
    }
}
