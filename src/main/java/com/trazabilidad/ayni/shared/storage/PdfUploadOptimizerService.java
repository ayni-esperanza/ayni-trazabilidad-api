package com.trazabilidad.ayni.shared.storage;

import com.trazabilidad.ayni.shared.exception.BadRequestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.unit.DataSize;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
public class PdfUploadOptimizerService {

    private static final Logger log = LoggerFactory.getLogger(PdfUploadOptimizerService.class);

    private static final List<GhostscriptProfile> GHOSTSCRIPT_PROFILES = List.of(
            new GhostscriptProfile("/printer", 170, 170, 300),
            new GhostscriptProfile("/ebook", 144, 144, 260),
            new GhostscriptProfile("/screen", 110, 110, 220)
    );

    private final long maxFinalDocumentBytes;
    private final long maxPdfSourceBytes;
    private final boolean ghostscriptEnabled;
    private final String ghostscriptCommand;
    private final Duration ghostscriptTimeout;
    private final AtomicBoolean ghostscriptUnavailableLogged = new AtomicBoolean(false);

    public PdfUploadOptimizerService(
            @Value("${app.storage.upload.max-final-document-size:25MB}") DataSize maxFinalDocumentSize,
            @Value("${app.storage.upload.max-pdf-source-size:50MB}") DataSize maxPdfSourceSize,
            @Value("${app.storage.upload.pdf-optimizer.enabled:true}") boolean ghostscriptEnabled,
            @Value("${app.storage.upload.pdf-optimizer.command:}") String ghostscriptCommand,
            @Value("${app.storage.upload.pdf-optimizer.timeout-seconds:120}") long ghostscriptTimeoutSeconds) {
        this.maxFinalDocumentBytes = maxFinalDocumentSize.toBytes();
        this.maxPdfSourceBytes = maxPdfSourceSize.toBytes();
        this.ghostscriptEnabled = ghostscriptEnabled;
        this.ghostscriptCommand = ghostscriptCommand != null ? ghostscriptCommand.trim() : "";
        this.ghostscriptTimeout = Duration.ofSeconds(Math.max(30L, ghostscriptTimeoutSeconds));
    }

    public PreparedUploadObject prepareForUpload(MultipartFile file) {
        try {
            byte[] originalBytes = file.getBytes();
            String fileName = file.getOriginalFilename() != null ? file.getOriginalFilename().trim() : "archivo";
            String contentType = file.getContentType() != null ? file.getContentType().trim() : "application/octet-stream";

            if (!isPdf(fileName, contentType)) {
                validateFinalDocumentSize(originalBytes.length);
                return new PreparedUploadObject(fileName, contentType, originalBytes);
            }

            if (originalBytes.length <= maxFinalDocumentBytes) {
                return new PreparedUploadObject(fileName, "application/pdf", originalBytes);
            }

            if (originalBytes.length > maxPdfSourceBytes) {
                throw new BadRequestException(
                        "El PDF no debe superar los " + formatMb(maxPdfSourceBytes) + " antes de optimizarse");
            }

            byte[] optimized = optimizePdfWithGhostscript(fileName, originalBytes);
            if (optimized.length > maxFinalDocumentBytes) {
                throw new BadRequestException(
                        "No se pudo reducir el PDF al limite de " + formatMb(maxFinalDocumentBytes) + " sin perder demasiada calidad");
            }

            return new PreparedUploadObject(fileName, "application/pdf", optimized);
        } catch (BadRequestException ex) {
            throw ex;
        } catch (IOException ex) {
            throw new IllegalStateException("No se pudo preparar el archivo para su subida", ex);
        }
    }

    private void validateFinalDocumentSize(long bytes) {
        if (bytes > maxFinalDocumentBytes) {
            throw new BadRequestException(
                    "El archivo no debe superar los " + formatMb(maxFinalDocumentBytes));
        }
    }

    private byte[] optimizePdfWithGhostscript(String fileName, byte[] originalBytes) {
        if (!ghostscriptEnabled) {
            throw new BadRequestException(
                    "El PDF supera los " + formatMb(maxFinalDocumentBytes) + " y la optimizacion automatica esta deshabilitada");
        }

        byte[] bestCandidate = originalBytes;
        String lastError = null;

        for (GhostscriptProfile profile : GHOSTSCRIPT_PROFILES) {
            try {
                byte[] candidate = runGhostscript(fileName, originalBytes, profile);
                if (candidate.length < bestCandidate.length) {
                    bestCandidate = candidate;
                }
                if (candidate.length <= maxFinalDocumentBytes) {
                    return candidate;
                }
            } catch (IOException ex) {
                lastError = ex.getMessage();
                if (ghostscriptUnavailableLogged.compareAndSet(false, true)) {
                    log.warn("Ghostscript no esta disponible para optimizar PDFs: {}", ex.getMessage());
                }
                break;
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("La optimizacion del PDF fue interrumpida", ex);
            } catch (RuntimeException ex) {
                lastError = ex.getMessage();
                log.warn("Ghostscript no pudo optimizar el PDF con el perfil {}: {}", profile.pdfSettings(), ex.getMessage());
            }
        }

        if (bestCandidate.length < originalBytes.length) {
            return bestCandidate;
        }

        if (lastError != null && !lastError.isBlank()) {
            throw new BadRequestException(
                    "No se pudo optimizar el PDF automaticamente. Verifique la configuracion de Ghostscript");
        }

        throw new BadRequestException(
                "No se pudo reducir el PDF al limite de " + formatMb(maxFinalDocumentBytes) + " sin perder demasiada calidad");
    }

    private byte[] runGhostscript(String fileName, byte[] originalBytes, GhostscriptProfile profile)
            throws IOException, InterruptedException {
        Path tempDir = Files.createTempDirectory("ayni-gs-");
        Path inputFile = tempDir.resolve(sanitizeFileName(fileName));
        Path outputFile = tempDir.resolve("optimized.pdf");

        try {
            Files.write(inputFile, originalBytes);

            IOException launchError = null;
            for (String command : resolveGhostscriptCommands()) {
                try {
                    Process process = new ProcessBuilder(buildGhostscriptCommand(command, inputFile, outputFile, profile))
                            .redirectErrorStream(true)
                            .start();

                    boolean finished = process.waitFor(ghostscriptTimeout.toSeconds(), TimeUnit.SECONDS);
                    if (!finished) {
                        process.destroyForcibly();
                        throw new IllegalStateException("Ghostscript excedio el tiempo limite de optimizacion");
                    }

                    int exitCode = process.exitValue();
                    if (exitCode != 0) {
                        String processOutput = new String(process.getInputStream().readAllBytes());
                        throw new IllegalStateException(
                                "Ghostscript finalizo con codigo " + exitCode + (processOutput.isBlank() ? "" : ": " + processOutput.trim()));
                    }

                    if (!Files.exists(outputFile) || Files.size(outputFile) == 0) {
                        throw new IllegalStateException("Ghostscript no genero un PDF de salida valido");
                    }

                    return Files.readAllBytes(outputFile);
                } catch (IOException ex) {
                    launchError = ex;
                }
            }

            throw launchError != null ? launchError : new IOException("No se pudo iniciar Ghostscript");
        } finally {
            deleteQuietly(tempDir);
        }
    }

    private List<String> buildGhostscriptCommand(String command, Path inputFile, Path outputFile, GhostscriptProfile profile) {
        List<String> args = new ArrayList<>();
        args.add(command);
        args.add("-sDEVICE=pdfwrite");
        args.add("-dCompatibilityLevel=1.4");
        args.add("-dNOPAUSE");
        args.add("-dBATCH");
        args.add("-dSAFER");
        args.add("-dQUIET");
        args.add("-dDetectDuplicateImages=true");
        args.add("-dCompressFonts=true");
        args.add("-dSubsetFonts=true");
        args.add("-dAutoRotatePages=/None");
        args.add("-dDownsampleColorImages=true");
        args.add("-dColorImageDownsampleType=/Bicubic");
        args.add("-dColorImageResolution=" + profile.colorResolution());
        args.add("-dDownsampleGrayImages=true");
        args.add("-dGrayImageDownsampleType=/Bicubic");
        args.add("-dGrayImageResolution=" + profile.grayResolution());
        args.add("-dDownsampleMonoImages=true");
        args.add("-dMonoImageDownsampleType=/Subsample");
        args.add("-dMonoImageResolution=" + profile.monoResolution());
        args.add("-dPDFSETTINGS=" + profile.pdfSettings());
        args.add("-sOutputFile=" + outputFile.toAbsolutePath());
        args.add(inputFile.toAbsolutePath().toString());
        return args;
    }

    private List<String> resolveGhostscriptCommands() {
        if (!ghostscriptCommand.isBlank()) {
            return List.of(ghostscriptCommand);
        }

        return List.of("gswin64c.exe", "gswin64c", "gswin32c.exe", "gswin32c", "gs");
    }

    private boolean isPdf(String fileName, String contentType) {
        return "application/pdf".equalsIgnoreCase(contentType)
                || fileName.toLowerCase(Locale.ROOT).endsWith(".pdf");
    }

    private String sanitizeFileName(String fileName) {
        String sanitized = (fileName == null ? "" : fileName.trim())
                .replaceAll("\\s+", "-")
                .replaceAll("[^a-zA-Z0-9._-]", "");
        if (sanitized.isBlank()) {
            return "documento.pdf";
        }
        return sanitized.toLowerCase(Locale.ROOT).endsWith(".pdf") ? sanitized : sanitized + ".pdf";
    }

    private String formatMb(long bytes) {
        return Math.round(bytes / (1024d * 1024d)) + "MB";
    }

    private void deleteQuietly(Path path) {
        if (path == null || !Files.exists(path)) {
            return;
        }

        try (var walk = Files.walk(path)) {
            walk.sorted(Comparator.reverseOrder()).forEach(current -> {
                try {
                    Files.deleteIfExists(current);
                } catch (IOException ex) {
                    log.debug("No se pudo eliminar temporal {}", current, ex);
                }
            });
        } catch (IOException ex) {
            log.debug("No se pudo limpiar el directorio temporal {}", path, ex);
        }
    }

    private record GhostscriptProfile(
            String pdfSettings,
            int colorResolution,
            int grayResolution,
            int monoResolution) {
    }
}
