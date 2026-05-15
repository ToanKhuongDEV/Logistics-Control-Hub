package com.logistics.hub.feature.excel.dto.request;

import com.logistics.hub.feature.excel.constant.ExcelConstant;
import com.logistics.hub.feature.excel.enums.ExcelFileEnum;

import jakarta.validation.constraints.NotBlank;

public record TemplateFileRequest (
    @NotBlank(message = ExcelConstant.TYPE_REQUIRED)
    ExcelFileEnum type
) {}
