package com.trazabilidad.ayni.shared.enums;

import com.trazabilidad.ayni.shared.exception.BadStateTransitionException;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * Enum que define los estados posibles de una Etapa de Proyecto.
 * validar transiciones de estado.
 * Las etapas siguen un flujo secuencial donde solo se puede iniciar
 * si la etapa anterior está completada.
 */
@Getter
public enum EstadoEtapaProyecto {

    PENDIENTE("Pendiente", Set.of("EN_PROCESO", "CANCELADO")),
    EN_PROCESO("En Proceso", Set.of("COMPLETADO", "CANCELADO")),
    COMPLETADO("Completado", Set.of()),
    CANCELADO("Cancelado", Set.of());

    private final String displayName;
    private final Set<String> transicionesPermitidas;

    EstadoEtapaProyecto(String displayName, Set<String> transicionesPermitidas) {
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
    public void validarTransicion(EstadoEtapaProyecto nuevoEstado) {
        if (this == nuevoEstado) {
            return; // Mismo estado, no hay transición
        }

        if (!transicionesPermitidas.contains(nuevoEstado.name())) {
            throw new BadStateTransitionException(
                    "EtapaProyecto",
                    this.name(),
                    nuevoEstado.name(),
                    getTransicionesPermitidasList());
        }
    }

    /**
     * Retorna la lista de transiciones permitidas como lista de enums.
     */
    public List<EstadoEtapaProyecto> getTransicionesPermitidasList() {
        return transicionesPermitidas.stream()
                .map(EstadoEtapaProyecto::valueOf)
                .toList();
    }

    /**
     * Verifica si el estado es terminal (no permite más transiciones).
     */
    public boolean esEstadoTerminal() {
        return transicionesPermitidas.isEmpty();
    }

    /**
     * Verifica si la etapa puede ser iniciada (cambiar de PENDIENTE a EN_PROCESO).
     */
    public boolean puedeIniciar() {
        return this == PENDIENTE;
    }

    /**
     * Retorna todos los estados disponibles como strings.
     */
    public static List<String> getValoresDisponibles() {
        return Arrays.stream(values())
                .map(EstadoEtapaProyecto::name)
                .toList();
    }
}
