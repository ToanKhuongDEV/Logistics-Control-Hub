package com.logistics.hub.feature.excel.controller;

import com.logistics.hub.common.constant.UrlConstant;
import com.logistics.hub.feature.excel.enums.ExcelFileEnum;
import com.logistics.hub.feature.excel.service.ExcelExportService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.time.LocalDate;

@RestController
@RequestMapping(UrlConstant.Excel.EXPORT)
@RequiredArgsConstructor
public class ExcelExportController {

    private final ExcelExportService excelExportService;

    @GetMapping
    public ResponseEntity<ByteArrayResource> export(
            @RequestParam ExcelFileEnum type,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long depotId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(defaultValue = "100") Integer maxRows
    ) throws IOException {
        return excelExportService.export(type, search, status, depotId, fromDate, toDate, maxRows);
    }
}
