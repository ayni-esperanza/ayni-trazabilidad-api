package com.trazabilidad.ayni.shared.storage.dto;

import jakarta.validation.constraints.NotBlank;

public record PresignUploadRequest(
        @NotBlank(message = "El nombre del archivo es obligatorio") String fileName,
        @NotBlank(message = "El content type es obligatorio") String contentType,
        String carpeta,
        Long proyectoId,
        Long actividadId) {
}
