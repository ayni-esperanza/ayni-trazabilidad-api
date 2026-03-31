package com.trazabilidad.ayni.shared.storage.dto;

import java.util.Map;

public record PresignUploadResponse(
        String uploadUrl,
        String method,
        Map<String, String> headers,
        String objectKey,
        String publicUrl,
        String expiresAt) {
}
