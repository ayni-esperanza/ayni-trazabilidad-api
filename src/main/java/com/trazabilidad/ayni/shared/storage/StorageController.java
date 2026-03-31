package com.trazabilidad.ayni.shared.storage;

import com.trazabilidad.ayni.auth.CustomUserDetails;
import com.trazabilidad.ayni.shared.storage.dto.PresignUploadRequest;
import com.trazabilidad.ayni.shared.storage.dto.PresignUploadResponse;
import com.trazabilidad.ayni.shared.storage.dto.UploadObjectResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/storage")
public class StorageController {

    private final R2PresignService r2PresignService;

    public StorageController(R2PresignService r2PresignService) {
        this.r2PresignService = r2PresignService;
    }

    @PostMapping("/presign-upload")
    public ResponseEntity<PresignUploadResponse> createPresignedUpload(
            @Valid @RequestBody PresignUploadRequest request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(r2PresignService.createPresignedUpload(request, resolveCurrentUserId()));
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UploadObjectResponse> uploadObject(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "carpeta", required = false) String carpeta,
            @RequestParam(value = "proyectoId", required = false) Long proyectoId,
            @RequestParam(value = "actividadId", required = false) Long actividadId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(r2PresignService.uploadObject(file, carpeta, proyectoId, actividadId, resolveCurrentUserId()));
    }

    private Long resolveCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getPrincipal() == null) {
            return null;
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof CustomUserDetails userDetails) {
            return userDetails.getId();
        }

        return null;
    }
}
