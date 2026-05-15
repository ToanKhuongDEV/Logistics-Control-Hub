package com.logistics.hub.feature.excel.service;

import java.io.IOException;

import org.springframework.core.io.ByteArrayResource;

import com.logistics.hub.feature.excel.dto.request.TemplateFileRequest;

public interface TemplateService {
    ByteArrayResource exportTemplate(TemplateFileRequest request) throws IOException;
}
