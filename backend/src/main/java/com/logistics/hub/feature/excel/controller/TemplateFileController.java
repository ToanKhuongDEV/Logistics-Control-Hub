package com.logistics.hub.feature.excel.controller;

import java.io.IOException;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.logistics.hub.common.base.ApiResponse;
import com.logistics.hub.common.constant.UrlConstant;
import com.logistics.hub.feature.excel.constant.ExcelConstant;
import com.logistics.hub.feature.excel.dto.request.TemplateFileRequest;
import com.logistics.hub.feature.excel.service.TemplateService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping(UrlConstant.Excel.TEMPLATE)
@RequiredArgsConstructor
public class TemplateFileController {
    
    private final TemplateService templateService;

    @GetMapping
    public ResponseEntity<ApiResponse<ByteArrayResource>> getTemplate( 
        @ModelAttribute TemplateFileRequest request
    ) throws IOException{
        ByteArrayResource response = templateService.exportTemplate(request);
        return ResponseEntity.ok().body(ApiResponse.success(ExcelConstant.GET_TEMPLATE_SUCCESS,response));
    }
}
