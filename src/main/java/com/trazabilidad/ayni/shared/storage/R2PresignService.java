package com.trazabilidad.ayni.shared.storage;

import com.trazabilidad.ayni.shared.exception.BadRequestException;
import com.trazabilidad.ayni.shared.storage.dto.PresignUploadRequest;
import com.trazabilidad.ayni.shared.storage.dto.PresignUploadResponse;
import com.trazabilidad.ayni.shared.storage.dto.UploadObjectResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@Service
public class R2PresignService {

    private static final DateTimeFormatter YEAR_MONTH_FORMAT = DateTimeFormatter.ofPattern("yyyyMM");

    private final String endpoint;
    private final String bucketName;
    private final String accessKeyId;
    private final String secretAccessKey;
    private final long expirationSeconds;
    private final StorageUrlResolver storageUrlResolver;

    public R2PresignService(
            @Value("${app.storage.r2.endpoint:}") String endpoint,
            @Value("${app.storage.r2.bucket-name:}") String bucketName,
            @Value("${app.storage.r2.access-key-id:}") String accessKeyId,
            @Value("${app.storage.r2.secret-access-key:}") String secretAccessKey,
            @Value("${app.storage.r2.presign-expiration-seconds:300}") long expirationSeconds,
            StorageUrlResolver storageUrlResolver) {
        this.endpoint = endpoint != null ? endpoint.trim() : "";
        this.bucketName = bucketName != null ? bucketName.trim() : "";
        this.accessKeyId = accessKeyId != null ? accessKeyId.trim() : "";
        this.secretAccessKey = secretAccessKey != null ? secretAccessKey.trim() : "";
        this.expirationSeconds = expirationSeconds;
        this.storageUrlResolver = storageUrlResolver;
    }

    public PresignUploadResponse createPresignedUpload(PresignUploadRequest request, Long userId) {
        validateConfiguration();
        validateRequest(request);

        String objectKey = buildObjectKey(request, userId);
        String normalizedContentType = request.contentType().trim();

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(objectKey)
                .contentType(normalizedContentType)
                .build();

        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(Duration.ofSeconds(expirationSeconds))
                .putObjectRequest(putObjectRequest)
                .build();

        try (S3Presigner presigner = buildPresigner()) {
            PresignedPutObjectRequest presigned = presigner.presignPutObject(presignRequest);
            Instant expiresAt = Instant.now().plusSeconds(expirationSeconds);

            return new PresignUploadResponse(
                    presigned.url().toString(),
                    "PUT",
                    Map.of("Content-Type", normalizedContentType),
                    objectKey,
                    storageUrlResolver.resolvePublicUrl(objectKey),
                    expiresAt.toString());
        }
    }

    public UploadObjectResponse uploadObject(
            MultipartFile file,
            String carpeta,
            Long proyectoId,
            Long actividadId,
            Long userId) {
        validateConfiguration();
        validateMultipart(file);

        PresignUploadRequest syntheticRequest = new PresignUploadRequest(
                file.getOriginalFilename(),
                file.getContentType(),
                carpeta,
                proyectoId,
                actividadId);
        validateRequest(syntheticRequest);

        String objectKey = buildObjectKey(syntheticRequest, userId);
        String contentType = file.getContentType().trim();

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(objectKey)
                .contentType(contentType)
                .build();

        try (S3Client s3Client = buildS3Client()) {
            PutObjectResponse putObjectResponse = s3Client.putObject(
                    putObjectRequest,
                    RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

            return new UploadObjectResponse(
                    objectKey,
                    storageUrlResolver.resolvePublicUrl(objectKey),
                    putObjectResponse.eTag());
        } catch (Exception ex) {
            throw new IllegalStateException("No se pudo subir el archivo al bucket R2", ex);
        }
    }

    private S3Presigner buildPresigner() {
        return S3Presigner.builder()
                .endpointOverride(URI.create(endpoint))
                .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKeyId, secretAccessKey)))
                .region(Region.of("auto"))
                .serviceConfiguration(S3Configuration.builder().pathStyleAccessEnabled(true).build())
                .build();
    }

    private S3Client buildS3Client() {
        return S3Client.builder()
                .endpointOverride(URI.create(endpoint))
                .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKeyId, secretAccessKey)))
                .region(Region.of("auto"))
                .serviceConfiguration(S3Configuration.builder().pathStyleAccessEnabled(true).build())
                .build();
    }

    private void validateConfiguration() {
        if (endpoint.isBlank() || bucketName.isBlank() || accessKeyId.isBlank() || secretAccessKey.isBlank()) {
            throw new IllegalStateException("Faltan variables de configuración R2 (endpoint, bucket, access key o secret key)");
        }

        if (expirationSeconds <= 0 || expirationSeconds > 3600) {
            throw new IllegalStateException("R2_PRESIGN_EXPIRATION_SECONDS debe estar entre 1 y 3600");
        }
    }

    private void validateRequest(PresignUploadRequest request) {
        String contentType = request.contentType().trim().toLowerCase(Locale.ROOT);
        if (!contentType.startsWith("image/")) {
            throw new BadRequestException("Solo se permiten archivos de imagen para subida a bucket");
        }
    }

    private void validateMultipart(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("El archivo es obligatorio");
        }
        if (file.getOriginalFilename() == null || file.getOriginalFilename().trim().isBlank()) {
            throw new BadRequestException("El nombre del archivo es obligatorio");
        }
        if (file.getContentType() == null || file.getContentType().trim().isBlank()) {
            throw new BadRequestException("El content type del archivo es obligatorio");
        }
    }

    private String buildObjectKey(PresignUploadRequest request, Long userId) {
        String baseCarpeta = sanitizeSegment(request.carpeta());
        if (baseCarpeta.isBlank()) {
            baseCarpeta = "evidencias";
        }

        String yearMonth = YEAR_MONTH_FORMAT.format(Instant.now().atOffset(ZoneOffset.UTC));
        String safeFileName = sanitizeFileName(request.fileName());
        StringBuilder scope = new StringBuilder();

        if (request.proyectoId() != null) {
            scope.append("p").append(request.proyectoId()).append('-');
        }
        if (request.actividadId() != null) {
            scope.append("a").append(request.actividadId()).append('-');
        }
        if (userId != null) {
            scope.append("u").append(userId).append('-');
        }

        StringBuilder key = new StringBuilder()
                .append(baseCarpeta)
                .append('/')
                .append(yearMonth)
                .append('/');

        if (!scope.isEmpty()) {
            key.append(scope);
        }

        key.append(UUID.randomUUID()).append('-').append(safeFileName);
        return key.toString();
    }

    private String sanitizeFileName(String fileName) {
        String trimmed = fileName != null ? fileName.trim() : "";
        if (trimmed.isBlank()) {
            return "archivo";
        }

        String sanitized = trimmed
                .replaceAll("\\s+", "-")
                .replaceAll("[^a-zA-Z0-9._-]", "");

        return sanitized.isBlank() ? "archivo" : sanitized;
    }

    private String sanitizeSegment(String segment) {
        if (segment == null) {
            return "";
        }

        return segment.trim().toLowerCase(Locale.ROOT)
                .replaceAll("\\s+", "-")
                .replaceAll("[^a-z0-9_-]", "");
    }
}
