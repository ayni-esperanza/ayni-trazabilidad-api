package com.trazabilidad.ayni.shared.storage;

public record PreparedUploadObject(
        String fileName,
        String contentType,
        byte[] content) {

    public long size() {
        return content != null ? content.length : 0L;
    }
}
