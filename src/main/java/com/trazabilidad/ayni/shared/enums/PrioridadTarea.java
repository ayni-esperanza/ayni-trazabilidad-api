package com.trazabilidad.ayni.shared.enums;

import lombok.Getter;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * Enum que define las prioridades de una Tarea.
 * Incluye un campo peso para ordenamiento eficiente.
 */
@Getter
public enum PrioridadTarea {

    ALTA("Alta", 3),
    MEDIA("Media", 2),
    BAJA("Baja", 1);

    private final String displayName;
    private final int peso;

    PrioridadTarea(String displayName, int peso) {
        this.displayName = displayName;
        this.peso = peso;
    }

    /**
     * Retorna un comparador para ordenar por prioridad descendente (ALTA primero).
     */
    public static Comparator<PrioridadTarea> comparadorDescendente() {
        return Comparator.comparingInt(PrioridadTarea::getPeso).reversed();
    }

    /**
     * Retorna un comparador para ordenar por prioridad ascendente (BAJA primero).
     */
    public static Comparator<PrioridadTarea> comparadorAscendente() {
        return Comparator.comparingInt(PrioridadTarea::getPeso);
    }

    /**
     * Retorna todas las prioridades disponibles como strings.
     */
    public static List<String> getValoresDisponibles() {
        return Arrays.stream(values())
                .map(PrioridadTarea::name)
                .toList();
    }

    /**
     * Verifica si la prioridad es crítica (ALTA).
     */
    public boolean esCritica() {
        return this == ALTA;
    }
}
