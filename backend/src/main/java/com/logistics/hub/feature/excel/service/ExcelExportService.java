package com.logistics.hub.feature.excel.service;

import com.logistics.hub.feature.excel.enums.ExcelFileEnum;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.time.LocalDate;

public interface ExcelExportService {

    ResponseEntity<ByteArrayResource> export(
            ExcelFileEnum type,
            String search,
            String status,
            Long depotId,
            LocalDate fromDate,
            LocalDate toDate,
            Integer maxRows
    ) throws IOException;
}
