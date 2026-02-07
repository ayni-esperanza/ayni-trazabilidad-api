package com.trazabilidad.ayni.proceso;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.trazabilidad.ayni.shared.util.Auditable;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Entidad que representa un Proceso de negocio.
 * Un proceso es una plantilla que contiene etapas ordenadas
 * que se usarán para generar proyectos.
 */
@Entity
@Table(name = "procesos", indexes = {
        @Index(name = "idx_proceso_nombre", columnList = "nombre", unique = true),
        @Index(name = "idx_proceso_area", columnList = "area"),
        @Index(name = "idx_proceso_activo", columnList = "activo")
})
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Proceso extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre del proceso es obligatorio")
    @Size(max = 150, message = "El nombre no puede exceder 150 caracteres")
    @Column(nullable = false, unique = true, length = 150)
    private String nombre;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Size(max = 100, message = "El área no puede exceder 100 caracteres")
    @Column(length = 100)
    private String area;

    @Builder.Default
    @Column(nullable = false)
    private Boolean activo = true;

    @OneToMany(mappedBy = "proceso", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonManagedReference
    @Builder.Default
    private List<Etapa> etapas = new ArrayList<>();

    /**
     * Helper method para obtener las etapas ordenadas por su campo 'orden'.
     * Usa Comparator.comparingInt() para eficiencia O(n log n).
     *
     * @return Lista de etapas ordenadas ascendentemente por orden
     */
    public List<Etapa> getEtapasOrdenadas() {
        return etapas.stream()
                .sorted(Comparator.comparingInt(Etapa::getOrden))
                .toList();
    }

    /**
     * Helper method para agregar una etapa al proceso.
     * Mantiene la relación bidireccional.
     *
     * @param etapa Etapa a agregar
     */
    public void agregarEtapa(Etapa etapa) {
        etapas.add(etapa);
        etapa.setProceso(this);
    }

    /**
     * Helper method para remover una etapa del proceso.
     * Mantiene la relación bidireccional.
     *
     * @param etapa Etapa a remover
     */
    public void removerEtapa(Etapa etapa) {
        etapas.remove(etapa);
        etapa.setProceso(null);
    }

    /**
     * Helper method para verificar si el proceso tiene etapas.
     *
     * @return true si tiene etapas, false en caso contrario
     */
    public boolean tieneEtapas() {
        return etapas != null && !etapas.isEmpty();
    }

    /**
     * Helper method para contar el número de etapas activas.
     *
     * @return Cantidad de etapas activas
     */
    public long contarEtapasActivas() {
        return etapas.stream()
                .filter(Etapa::getActivo)
                .count();
    }
}
