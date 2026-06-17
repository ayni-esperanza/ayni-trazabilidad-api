package com.trazabilidad.ayni.shared.storage;

import com.trazabilidad.ayni.shared.exception.BadRequestException;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ooxml.POIXMLProperties;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.unit.DataSize;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@Service
public class SpreadsheetUploadOptimizerService {

    private static final String XLSX_MIME = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
    private static final String XLS_MIME = "application/vnd.ms-excel";

    private final long maxFinalDocumentBytes;
    private final long maxSpreadsheetSourceBytes;

    public SpreadsheetUploadOptimizerService(
            @Value("${app.storage.upload.max-final-document-size:25MB}") DataSize maxFinalDocumentSize,
            @Value("${app.storage.upload.max-spreadsheet-source-size:50MB}") DataSize maxSpreadsheetSourceSize) {
        this.maxFinalDocumentBytes = maxFinalDocumentSize.toBytes();
        this.maxSpreadsheetSourceBytes = maxSpreadsheetSourceSize.toBytes();
    }

    public boolean supports(String fileName, String contentType) {
        String normalizedType = normalizeContentType(contentType);
        String extension = getExtension(fileName);
        return XLSX_MIME.equals(normalizedType)
                || XLS_MIME.equals(normalizedType)
                || "xlsx".equals(extension)
                || "xls".equals(extension);
    }

    public PreparedUploadObject prepareForUpload(MultipartFile file) {
        try {
            byte[] originalBytes = file.getBytes();
            String fileName = file.getOriginalFilename() != null ? file.getOriginalFilename().trim() : "archivo.xlsx";
            String contentType = file.getContentType() != null ? file.getContentType().trim() : XLSX_MIME;

            if (originalBytes.length <= maxFinalDocumentBytes && isXlsx(fileName, contentType)) {
                return new PreparedUploadObject(fileName, XLSX_MIME, originalBytes);
            }

            if (originalBytes.length > maxSpreadsheetSourceBytes) {
                throw new BadRequestException(
                        "El archivo Excel no debe superar los " + formatMb(maxSpreadsheetSourceBytes) + " antes de optimizarse");
            }

            PreparedUploadObject optimized = optimizeSpreadsheet(fileName, contentType, originalBytes);
            if (optimized.size() > maxFinalDocumentBytes) {
                throw new BadRequestException(
                        "No se pudo reducir el Excel al limite de " + formatMb(maxFinalDocumentBytes) + " con optimizacion segura");
            }

            return optimized;
        } catch (BadRequestException ex) {
            throw ex;
        } catch (IOException ex) {
            throw new IllegalStateException("No se pudo optimizar el archivo Excel", ex);
        }
    }

    private PreparedUploadObject optimizeSpreadsheet(String fileName, String contentType, byte[] originalBytes) throws IOException {
        try (Workbook sourceWorkbook = WorkbookFactory.create(new ByteArrayInputStream(originalBytes))) {
            XSSFWorkbook normalizedWorkbook;
            boolean closeNormalizedWorkbook = false;

            if (sourceWorkbook instanceof XSSFWorkbook xssfWorkbook) {
                clearMetadata(xssfWorkbook);
                normalizedWorkbook = xssfWorkbook;
            } else if (sourceWorkbook instanceof HSSFWorkbook hssfWorkbook) {
                normalizedWorkbook = convertToXlsx(hssfWorkbook);
                clearMetadata(normalizedWorkbook);
                closeNormalizedWorkbook = true;
            } else {
                throw new BadRequestException("Formato de Excel no soportado para optimizacion segura");
            }

            try {
                byte[] optimizedBytes = writeWorkbook(normalizedWorkbook);
                String normalizedFileName = ensureXlsxExtension(fileName);
                return new PreparedUploadObject(normalizedFileName, XLSX_MIME, optimizedBytes);
            } finally {
                if (closeNormalizedWorkbook) {
                    normalizedWorkbook.close();
                }
            }
        }
    }

    private byte[] writeWorkbook(XSSFWorkbook workbook) throws IOException {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }

    private XSSFWorkbook convertToXlsx(HSSFWorkbook sourceWorkbook) {
        XSSFWorkbook targetWorkbook = new XSSFWorkbook();
        Map<CellStyle, CellStyle> styleCache = new HashMap<>();

        for (int sheetIndex = 0; sheetIndex < sourceWorkbook.getNumberOfSheets(); sheetIndex++) {
            Sheet sourceSheet = sourceWorkbook.getSheetAt(sheetIndex);
            Sheet targetSheet = targetWorkbook.createSheet(sourceSheet.getSheetName());
            copySheet(sourceWorkbook, targetWorkbook, sourceSheet, targetSheet, styleCache);
        }

        while (targetWorkbook.getNumberOfSheets() > sourceWorkbook.getNumberOfSheets()) {
            targetWorkbook.removeSheetAt(targetWorkbook.getNumberOfSheets() - 1);
        }

        return targetWorkbook;
    }

    private void copySheet(
            Workbook sourceWorkbook,
            Workbook targetWorkbook,
            Sheet sourceSheet,
            Sheet targetSheet,
            Map<CellStyle, CellStyle> styleCache) {
        targetSheet.setDisplayGridlines(sourceSheet.isDisplayGridlines());
        targetSheet.setPrintGridlines(sourceSheet.isPrintGridlines());
        targetSheet.setHorizontallyCenter(sourceSheet.getHorizontallyCenter());
        targetSheet.setVerticallyCenter(sourceSheet.getVerticallyCenter());
        targetSheet.setDefaultColumnWidth(sourceSheet.getDefaultColumnWidth());
        targetSheet.setDefaultRowHeight(sourceSheet.getDefaultRowHeight());

        if (sourceSheet.getMargin(Sheet.LeftMargin) > 0) {
            targetSheet.setMargin(Sheet.LeftMargin, sourceSheet.getMargin(Sheet.LeftMargin));
            targetSheet.setMargin(Sheet.RightMargin, sourceSheet.getMargin(Sheet.RightMargin));
            targetSheet.setMargin(Sheet.TopMargin, sourceSheet.getMargin(Sheet.TopMargin));
            targetSheet.setMargin(Sheet.BottomMargin, sourceSheet.getMargin(Sheet.BottomMargin));
        }

        for (int i = sourceSheet.getFirstRowNum(); i <= sourceSheet.getLastRowNum(); i++) {
            Row sourceRow = sourceSheet.getRow(i);
            if (sourceRow == null) {
                continue;
            }

            Row targetRow = targetSheet.createRow(i);
            targetRow.setHeight(sourceRow.getHeight());

            for (int j = sourceRow.getFirstCellNum(); j < sourceRow.getLastCellNum(); j++) {
                if (j < 0) {
                    continue;
                }

                Cell sourceCell = sourceRow.getCell(j);
                if (sourceCell == null) {
                    continue;
                }

                Cell targetCell = targetRow.createCell(j, sourceCell.getCellType());
                copyCellValue(sourceCell, targetCell);
                copyCellComment(sourceCell, targetCell, targetWorkbook);
                copyCellHyperlink(sourceCell, targetCell);
                copyCellStyle(sourceWorkbook, targetWorkbook, sourceCell, targetCell, styleCache);
            }
        }

        int maxColumnNum = 0;
        for (Row row : sourceSheet) {
            if (row != null) {
                maxColumnNum = Math.max(maxColumnNum, row.getLastCellNum());
            }
        }

        for (int i = 0; i <= maxColumnNum; i++) {
            targetSheet.setColumnWidth(i, sourceSheet.getColumnWidth(i));
            targetSheet.setColumnHidden(i, sourceSheet.isColumnHidden(i));
        }

        for (int i = 0; i < sourceSheet.getNumMergedRegions(); i++) {
            CellRangeAddress mergedRegion = sourceSheet.getMergedRegion(i);
            targetSheet.addMergedRegion(mergedRegion.copy());
        }
    }

    private void copyCellValue(Cell sourceCell, Cell targetCell) {
        switch (sourceCell.getCellType()) {
            case STRING -> targetCell.setCellValue(sourceCell.getRichStringCellValue());
            case NUMERIC -> {
                if (DateUtil.isCellDateFormatted(sourceCell)) {
                    targetCell.setCellValue(sourceCell.getDateCellValue());
                } else {
                    targetCell.setCellValue(sourceCell.getNumericCellValue());
                }
            }
            case BOOLEAN -> targetCell.setCellValue(sourceCell.getBooleanCellValue());
            case FORMULA -> targetCell.setCellFormula(sourceCell.getCellFormula());
            case ERROR -> targetCell.setCellErrorValue(sourceCell.getErrorCellValue());
            case BLANK -> targetCell.setBlank();
            default -> targetCell.setCellValue(sourceCell.toString());
        }
    }

    private void copyCellComment(Cell sourceCell, Cell targetCell, Workbook targetWorkbook) {
        if (sourceCell.getCellComment() == null || targetCell.getSheet() == null) {
            return;
        }

        Drawing<?> drawing = targetCell.getSheet().createDrawingPatriarch();
        CreationHelper creationHelper = targetWorkbook.getCreationHelper();
        ClientAnchor anchor = creationHelper.createClientAnchor();
        anchor.setCol1(targetCell.getColumnIndex());
        anchor.setCol2(targetCell.getColumnIndex() + 3);
        anchor.setRow1(targetCell.getRowIndex());
        anchor.setRow2(targetCell.getRowIndex() + 2);

        Comment newComment = drawing.createCellComment(anchor);
        newComment.setString(sourceCell.getCellComment().getString());
        newComment.setAuthor(sourceCell.getCellComment().getAuthor());
        targetCell.setCellComment(newComment);
    }

    private void copyCellHyperlink(Cell sourceCell, Cell targetCell) {
        if (sourceCell.getHyperlink() != null) {
            targetCell.setHyperlink(sourceCell.getHyperlink());
        }
    }

    private void copyCellStyle(
            Workbook sourceWorkbook,
            Workbook targetWorkbook,
            Cell sourceCell,
            Cell targetCell,
            Map<CellStyle, CellStyle> styleCache) {
        CellStyle sourceStyle = sourceCell.getCellStyle();
        if (sourceStyle == null) {
            return;
        }

        CellStyle cachedStyle = styleCache.get(sourceStyle);
        if (cachedStyle == null) {
            CellStyle newStyle = targetWorkbook.createCellStyle();
            newStyle.cloneStyleFrom(sourceStyle);
            int sourceFontIndex = sourceStyle.getFontIndexAsInt();
            if (sourceFontIndex >= 0) {
                Font sourceFont = sourceWorkbook.getFontAt(sourceFontIndex);
                Font targetFont = findOrCreateFont(targetWorkbook, sourceFont);
                newStyle.setFont(targetFont);
            }
            styleCache.put(sourceStyle, newStyle);
            cachedStyle = newStyle;
        }

        targetCell.setCellStyle(cachedStyle);
    }

    private Font findOrCreateFont(Workbook targetWorkbook, Font sourceFont) {
        for (short i = 0; i < targetWorkbook.getNumberOfFonts(); i++) {
            Font targetFont = targetWorkbook.getFontAt(i);
            if (fontsEqual(targetFont, sourceFont)) {
                return targetFont;
            }
        }

        Font targetFont = targetWorkbook.createFont();
        targetFont.setBold(sourceFont.getBold());
        targetFont.setItalic(sourceFont.getItalic());
        targetFont.setFontHeight(sourceFont.getFontHeight());
        targetFont.setFontName(sourceFont.getFontName());
        targetFont.setColor(sourceFont.getColor());
        targetFont.setStrikeout(sourceFont.getStrikeout());
        targetFont.setTypeOffset(sourceFont.getTypeOffset());
        targetFont.setUnderline(sourceFont.getUnderline());
        targetFont.setCharSet(sourceFont.getCharSet());
        return targetFont;
    }

    private boolean fontsEqual(Font first, Font second) {
        return first.getBold() == second.getBold()
                && first.getItalic() == second.getItalic()
                && first.getFontHeight() == second.getFontHeight()
                && first.getColor() == second.getColor()
                && first.getStrikeout() == second.getStrikeout()
                && first.getTypeOffset() == second.getTypeOffset()
                && first.getUnderline() == second.getUnderline()
                && first.getCharSet() == second.getCharSet()
                && String.valueOf(first.getFontName()).equals(String.valueOf(second.getFontName()));
    }

    private void clearMetadata(XSSFWorkbook workbook) {
        POIXMLProperties properties = workbook.getProperties();
        if (properties == null) {
            return;
        }

        properties.getCoreProperties().setCreator("");
        properties.getCoreProperties().setTitle("");
        properties.getCoreProperties().setSubjectProperty("");
        properties.getCoreProperties().setDescription("");
        properties.getCoreProperties().setKeywords("");
        properties.getCoreProperties().setCategory("");
        properties.getCoreProperties().setLastModifiedByUser("");
        properties.getCoreProperties().setRevision("");
    }

    private boolean isXlsx(String fileName, String contentType) {
        return XLSX_MIME.equals(normalizeContentType(contentType))
                || "xlsx".equals(getExtension(fileName));
    }

    private String ensureXlsxExtension(String fileName) {
        String trimmed = fileName != null ? fileName.trim() : "";
        if (trimmed.isBlank()) {
            return "documento.xlsx";
        }

        String sanitized = trimmed.replaceAll("[\\\\/:*?\"<>|]", "-");
        if (sanitized.toLowerCase(Locale.ROOT).endsWith(".xlsx")) {
            return sanitized;
        }

        int dotIndex = sanitized.lastIndexOf('.');
        String baseName = dotIndex > 0 ? sanitized.substring(0, dotIndex) : sanitized;
        return baseName + ".xlsx";
    }

    private String normalizeContentType(String contentType) {
        return contentType == null ? "" : contentType.trim().toLowerCase(Locale.ROOT);
    }

    private String getExtension(String fileName) {
        if (fileName == null || fileName.isBlank()) {
            return "";
        }

        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex < 0 || dotIndex == fileName.length() - 1) {
            return "";
        }

        return fileName.substring(dotIndex + 1).trim().toLowerCase(Locale.ROOT);
    }

    private String formatMb(long bytes) {
        return Math.round(bytes / (1024d * 1024d)) + "MB";
    }
}
