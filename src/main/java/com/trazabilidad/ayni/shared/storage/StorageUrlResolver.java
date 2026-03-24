package com.trazabilidad.ayni.shared.storage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class StorageUrlResolver {

    private final String bucketBaseUrl;

    public StorageUrlResolver(@Value("${app.storage.bucket-base-url:}") String bucketBaseUrl) {
        this.bucketBaseUrl = bucketBaseUrl != null ? bucketBaseUrl.trim() : "";
    }

    public String resolvePublicUrl(String objectKey) {
        if (objectKey == null || objectKey.isBlank()) {
            return null;
        }

        if (bucketBaseUrl.isBlank()) {
            return objectKey;
        }

        String cleanBase = bucketBaseUrl.endsWith("/")
                ? bucketBaseUrl.substring(0, bucketBaseUrl.length() - 1)
                : bucketBaseUrl;

        String cleanKey = objectKey.startsWith("/") ? objectKey.substring(1) : objectKey;
        return cleanBase + "/" + cleanKey;
    }
}
