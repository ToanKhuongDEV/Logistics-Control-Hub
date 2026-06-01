package com.logistics.hub.feature.excel.service.impl;

import java.io.IOException;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;

import com.logistics.hub.common.constant.TemplatePath;
import com.logistics.hub.common.util.ExcelUtils;
import com.logistics.hub.feature.excel.dto.request.TemplateFileRequest;
import com.logistics.hub.feature.excel.service.TemplateService;

import lombok.RequiredArgsConstructor;


@Service
@RequiredArgsConstructor
public class TemplateServiceImpl implements TemplateService {

    @Override
    public ByteArrayResource exportTemplate(TemplateFileRequest request) throws IOException{
        String path = switch(request.type()){
            case DEPOT -> TemplatePath.DEPOT.getExportPath();
            case DRIVER -> TemplatePath.DRIVER.getExportPath();
            case ORDER -> TemplatePath.ORDER.getExportPath();
            case ROUTING -> TemplatePath.ROUTING.getExportPath();
            case VEHICLE -> TemplatePath.VEHICLE.getExportPath();
        };
        return ExcelUtils.exportTemplate(path);
    }
    
}
