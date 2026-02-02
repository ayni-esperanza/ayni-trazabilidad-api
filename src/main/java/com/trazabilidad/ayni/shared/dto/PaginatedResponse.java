package com.trazabilidad.ayni.shared.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO genérico para respuestas paginadas.
 * 
 * @param <T> Tipo de contenido de la página
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaginatedResponse<T> {

    /**
     * Contenido de la página actual
     */
    private List<T> content;

    /**
     * Número total de elementos en todas las páginas
     */
    private Long totalElements;

    /**
     * Número total de páginas
     */
    private Integer totalPages;

    /**
     * Página actual (0-indexed)
     */
    private Integer page;

    /**
     * Tamaño de página (elementos por página)
     */
    private Integer size;
}
