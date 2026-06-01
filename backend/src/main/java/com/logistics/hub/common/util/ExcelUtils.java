package com.logistics.hub.common.util;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

/**
 * Utility methods for reading Excel templates and generating XLSX reports.
 *
 * <p>The report APIs are intentionally type-safe: callers define columns with
 * {@link ExcelColumn} instances and provide extractor functions instead of relying on reflection.
 * Supported cell values include strings, numbers, booleans, enums, {@link BigDecimal},
 * {@link LocalDate}, {@link LocalDateTime}, {@link Instant}, and {@link Date}.</p>
 */
public final class ExcelUtils {

    private static final String XLSX_MEDIA_TYPE =
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
    private static final String DEFAULT_SHEET_NAME = "Report";
    private static final int MAX_SHEET_NAME_LENGTH = 31;

    private ExcelUtils() {
    }

    /**
     * Horizontal alignment used when rendering report data cells.
     */
    public enum ColumnAlignment {
        LEFT,
        CENTER,
        RIGHT
    }

    /**
     * Column definition used by the generic report exporters.
     *
     * @param header visible column name written to the header row
     * @param extractor function that reads a cell value from a row object
     * @param width Excel column width measured in approximate character units
     * @param alignment horizontal alignment for the data cells in this column
     * @param <T> row object type
     */
    public record ExcelColumn<T>(
            String header,
            Function<T, ?> extractor,
            int width,
            ColumnAlignment alignment
    ) {
        public ExcelColumn {
            if (header == null || header.isBlank()) {
                throw new IllegalArgumentException("Excel column header is required");
            }
            Objects.requireNonNull(extractor, "Excel column extractor is required");
            width = width <= 0 ? 20 : width;
            alignment = alignment == null ? ColumnAlignment.LEFT : alignment;
        }
    }

    /**
     * Creates a left-aligned report column with the default width.
     *
     * @param header visible column name written to the header row
     * @param extractor function that reads a cell value from a row object
     * @param <T> row object type
     * @return column definition ready to pass to report exporters
     */
    public static <T> ExcelColumn<T> column(String header, Function<T, ?> extractor) {
        return new ExcelColumn<>(header, extractor, 20, ColumnAlignment.LEFT);
    }

    /**
     * Creates a report column with an explicit width and alignment.
     *
     * @param header visible column name written to the header row
     * @param extractor function that reads a cell value from a row object
     * @param width Excel column width measured in approximate character units;
     *              values less than or equal to zero are replaced with the default width
     * @param alignment horizontal alignment for data cells; {@code null} defaults to left alignment
     * @param <T> row object type
     * @return column definition ready to pass to report exporters
     */
    public static <T> ExcelColumn<T> column(
            String header,
            Function<T, ?> extractor,
            int width,
            ColumnAlignment alignment
    ) {
        return new ExcelColumn<>(header, extractor, width, alignment);
    }

    /**
     * Exports a template file from classpath resources.
     *
     * @param path classpath path, for example templates/export/order_export_template.xlsx
     * @return the exported template bytes
     * @throws FileNotFoundException if the template file does not exist
     * @throws IOException if an I/O error occurs while reading the file
     */
    public static ByteArrayResource exportTemplate(String path) throws IOException {
        return new ByteArrayResource(downloadResource(path));
    }

    /**
     * Reads a classpath resource into memory.
     *
     * @param path classpath path, for example templates/export/vehicle_export_template.xlsx
     * @return resource content as bytes
     * @throws FileNotFoundException if the resource does not exist
     * @throws IOException if the resource cannot be read
     */
    public static byte[] downloadResource(String path) throws IOException {
        ClassPathResource pathResource = new ClassPathResource(path);
        if (!pathResource.exists()) {
            throw new FileNotFoundException("Template not found: " + path);
        }

        try (InputStream inputStream = pathResource.getInputStream()) {
            return inputStream.readAllBytes();
        }
    }

    /**
     * Creates a new XLSX workbook and writes a single report sheet.
     *
     * <p>The first row is generated from {@code columns}; data starts at row index {@code 1}.
     * Passing {@code null} rows produces a valid workbook with only the header row.</p>
     *
     * @param sheetName desired sheet name; blank values default to {@code Report}
     * @param rows row data to export
     * @param columns ordered column definitions
     * @param <T> row object type
     * @return generated XLSX file as a byte array resource
     * @throws IOException if Apache POI cannot write the workbook
     * @throws IllegalArgumentException if no column definitions are provided
     */
    public static <T> ByteArrayResource exportReport(
            String sheetName,
            Collection<T> rows,
            List<ExcelColumn<T>> columns
    ) throws IOException {
        validateColumns(columns);

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet(safeSheetName(sheetName));
            Map<String, CellStyle> styles = createStyles(workbook);

            writeHeader(sheet.createRow(0), columns, styles.get("header"));
            writeRows(sheet, rows, columns, styles, 1);
            applyColumnWidths(sheet, columns);

            return toByteArrayResource(workbook);
        }
    }

    /**
     * Opens an XLSX template from classpath resources, rewrites the header row, clears old data rows,
     * and appends report data into the selected sheet.
     *
     * <p>If the requested sheet does not exist, the first sheet is reused and renamed. If the workbook
     * has no sheets, a new one is created. This method is useful when a report must preserve workbook
     * metadata or a pre-designed template while still using the same column mapping as
     * {@link #exportReport(String, Collection, List)}.</p>
     *
     * @param templatePath classpath path to an XLSX template
     * @param sheetName sheet to populate
     * @param headerRowIndex zero-based row index where headers should be written
     * @param firstDataRowIndex zero-based row index where data should begin
     * @param rows row data to export
     * @param columns ordered column definitions
     * @param <T> row object type
     * @return generated XLSX file as a byte array resource
     * @throws IOException if the template cannot be read or the workbook cannot be written
     * @throws IllegalArgumentException if no columns are provided or the data row starts before the header
     */
    public static <T> ByteArrayResource exportReportFromTemplate(
            String templatePath,
            String sheetName,
            int headerRowIndex,
            int firstDataRowIndex,
            Collection<T> rows,
            List<ExcelColumn<T>> columns
    ) throws IOException {
        validateColumns(columns);
        if (firstDataRowIndex <= headerRowIndex) {
            throw new IllegalArgumentException("firstDataRowIndex must be greater than headerRowIndex");
        }

        try (
                ByteArrayInputStream inputStream = new ByteArrayInputStream(downloadResource(templatePath));
                Workbook workbook = new XSSFWorkbook(inputStream)
        ) {
            Sheet sheet = workbook.getSheet(safeSheetName(sheetName));
            if (sheet == null) {
                sheet = workbook.getNumberOfSheets() > 0 ? workbook.getSheetAt(0) : workbook.createSheet(DEFAULT_SHEET_NAME);
                workbook.setSheetName(workbook.getSheetIndex(sheet), safeSheetName(sheetName));
            }

            removeRows(sheet, firstDataRowIndex, sheet.getLastRowNum());

            Map<String, CellStyle> styles = createStyles(workbook);
            Row headerRow = sheet.getRow(headerRowIndex);
            if (headerRow == null) {
                headerRow = sheet.createRow(headerRowIndex);
            }
            writeHeader(headerRow, columns, styles.get("header"));
            writeRows(sheet, rows, columns, styles, firstDataRowIndex);
            applyColumnWidths(sheet, columns);

            return toByteArrayResource(workbook);
        }
    }

    /**
     * Builds a Spring response that downloads an XLSX resource as an attachment.
     *
     * @param filename desired download filename; {@code .xlsx} is appended when missing
     * @param resource generated Excel content
     * @return response entity with content disposition, content type, and content length headers
     */
    public static ResponseEntity<ByteArrayResource> download(String filename, ByteArrayResource resource) {
        String safeFilename = (filename == null || filename.isBlank()) ? "report.xlsx" : filename;
        if (!safeFilename.toLowerCase().endsWith(".xlsx")) {
            safeFilename += ".xlsx";
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename(safeFilename).build().toString())
                .contentType(MediaType.parseMediaType(XLSX_MEDIA_TYPE))
                .contentLength(resource.contentLength())
                .body(resource);
    }

    /**
     * Serializes an Apache POI workbook into a {@link ByteArrayResource}.
     *
     * @param workbook workbook to serialize; the caller remains responsible for closing it
     * @return workbook content as a byte array resource
     * @throws IOException if Apache POI cannot write the workbook
     */
    public static ByteArrayResource toByteArrayResource(Workbook workbook) throws IOException {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            workbook.write(outputStream);
            return new ByteArrayResource(outputStream.toByteArray());
        }
    }

    private static <T> void validateColumns(List<ExcelColumn<T>> columns) {
        if (columns == null || columns.isEmpty()) {
            throw new IllegalArgumentException("At least one Excel column is required");
        }
    }

    private static <T> void writeHeader(Row row, List<ExcelColumn<T>> columns, CellStyle headerStyle) {
        for (int colIndex = 0; colIndex < columns.size(); colIndex++) {
            Cell cell = row.createCell(colIndex);
            cell.setCellValue(columns.get(colIndex).header());
            cell.setCellStyle(headerStyle);
        }
    }

    private static <T> void writeRows(
            Sheet sheet,
            Collection<T> rows,
            List<ExcelColumn<T>> columns,
            Map<String, CellStyle> styles,
            int startRowIndex
    ) {
        int rowIndex = startRowIndex;
        Collection<T> safeRows = rows == null ? List.of() : rows;

        for (T rowData : safeRows) {
            Row row = sheet.createRow(rowIndex++);
            for (int colIndex = 0; colIndex < columns.size(); colIndex++) {
                ExcelColumn<T> column = columns.get(colIndex);
                Object value = column.extractor().apply(rowData);
                Cell cell = row.createCell(colIndex);
                writeCell(cell, value);
                cell.setCellStyle(styles.get(styleKey(value, column.alignment())));
            }
        }
    }

    private static void writeCell(Cell cell, Object value) {
        if (value == null) {
            cell.setBlank();
            return;
        }

        if (value instanceof BigDecimal bigDecimal) {
            cell.setCellValue(bigDecimal.doubleValue());
        } else if (value instanceof Number number) {
            cell.setCellValue(number.doubleValue());
        } else if (value instanceof Boolean bool) {
            cell.setCellValue(bool);
        } else if (value instanceof LocalDate localDate) {
            cell.setCellValue(Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant()));
        } else if (value instanceof LocalDateTime localDateTime) {
            cell.setCellValue(Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant()));
        } else if (value instanceof Instant instant) {
            cell.setCellValue(Date.from(instant));
        } else if (value instanceof Date date) {
            cell.setCellValue(date);
        } else if (value instanceof Enum<?> enumValue) {
            cell.setCellValue(enumValue.name());
        } else {
            cell.setCellValue(String.valueOf(value));
        }
    }

    private static String styleKey(Object value, ColumnAlignment alignment) {
        String suffix = switch (alignment) {
            case CENTER -> "_center";
            case RIGHT -> "_right";
            default -> "_left";
        };

        if (value instanceof BigDecimal || value instanceof Number) {
            return "decimal" + suffix;
        }
        if (value instanceof LocalDate) {
            return "date" + suffix;
        }
        if (value instanceof LocalDateTime || value instanceof Instant || value instanceof Date) {
            return "datetime" + suffix;
        }
        if (value instanceof Boolean) {
            return "boolean" + suffix;
        }
        return "text" + suffix;
    }

    private static Map<String, CellStyle> createStyles(Workbook workbook) {
        Map<String, CellStyle> styles = new LinkedHashMap<>();
        DataFormat dataFormat = workbook.createDataFormat();

        styles.put("header", createHeaderStyle(workbook));
        for (ColumnAlignment alignment : ColumnAlignment.values()) {
            String suffix = switch (alignment) {
                case CENTER -> "_center";
                case RIGHT -> "_right";
                default -> "_left";
            };
            styles.put("text" + suffix, createDataStyle(workbook, alignment, null));
            styles.put("decimal" + suffix, createDataStyle(workbook, alignment, dataFormat.getFormat("#,##0.00")));
            styles.put("date" + suffix, createDataStyle(workbook, alignment, dataFormat.getFormat("dd/MM/yyyy")));
            styles.put("datetime" + suffix, createDataStyle(workbook, alignment, dataFormat.getFormat("dd/MM/yyyy HH:mm")));
            styles.put("boolean" + suffix, createDataStyle(workbook, alignment, null));
        }

        return styles;
    }

    private static CellStyle createHeaderStyle(Workbook workbook) {
        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());

        CellStyle style = workbook.createCellStyle();
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setWrapText(true);
        fullBorder(style);
        return style;
    }

    private static CellStyle createDataStyle(Workbook workbook, ColumnAlignment alignment, Short format) {
        CellStyle style = workbook.createCellStyle();
        style.setAlignment(toPoiAlignment(alignment));
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setWrapText(true);
        if (format != null) {
            style.setDataFormat(format);
        }
        fullBorder(style);
        return style;
    }

    private static HorizontalAlignment toPoiAlignment(ColumnAlignment alignment) {
        return switch (alignment) {
            case CENTER -> HorizontalAlignment.CENTER;
            case RIGHT -> HorizontalAlignment.RIGHT;
            default -> HorizontalAlignment.LEFT;
        };
    }

    private static void fullBorder(CellStyle cellStyle) {
        cellStyle.setBorderBottom(BorderStyle.THIN);
        cellStyle.setBorderTop(BorderStyle.THIN);
        cellStyle.setBorderLeft(BorderStyle.THIN);
        cellStyle.setBorderRight(BorderStyle.THIN);
    }

    private static <T> void applyColumnWidths(Sheet sheet, List<ExcelColumn<T>> columns) {
        for (int colIndex = 0; colIndex < columns.size(); colIndex++) {
            int width = Math.min(columns.get(colIndex).width(), 255);
            sheet.setColumnWidth(colIndex, width * 256);
        }
    }

    private static void removeRows(Sheet sheet, int startRow, int endRow) {
        for (int rowIndex = startRow; rowIndex <= endRow; rowIndex++) {
            Row row = sheet.getRow(rowIndex);
            if (row != null) {
                sheet.removeRow(row);
            }
        }
    }

    private static String safeSheetName(String sheetName) {
        String name = sheetName == null || sheetName.isBlank() ? DEFAULT_SHEET_NAME : sheetName.trim();
        name = name.replaceAll("[\\\\/?*\\[\\]:]", " ");
        return name.length() > MAX_SHEET_NAME_LENGTH ? name.substring(0, MAX_SHEET_NAME_LENGTH) : name;
    }
}
