package com.trazabilidad.ayni.shared.storage.dto;

public record UploadObjectResponse(
        String objectKey,
        String publicUrl,
        String eTag,
        Long fileSize,
        String fileName,
        String contentType) {
}
