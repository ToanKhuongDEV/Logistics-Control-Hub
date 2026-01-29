package com.logistics.hub.feature.location.controller;

import com.logistics.hub.common.base.ApiResponse;
import com.logistics.hub.feature.location.constant.LocationConstant;
import com.logistics.hub.feature.location.dto.request.LocationRequest;
import com.logistics.hub.feature.location.dto.response.LocationResponse;
import com.logistics.hub.feature.location.service.LocationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.logistics.hub.common.constant.UrlConstant;

@RestController
@RequestMapping(UrlConstant.Location.PREFIX)
@RequiredArgsConstructor
public class LocationController {

    private final LocationService locationService;

    @PostMapping
    public ResponseEntity<ApiResponse<?>> create(@Valid @RequestBody LocationRequest request) {
        LocationResponse response = locationService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(201,LocationConstant.LOCATION_CREATED_SUCCESS, response));
    }

    @DeleteMapping(UrlConstant.Location.BY_ID)
    public ResponseEntity<ApiResponse<?>> delete(@PathVariable Long id) {
        locationService.delete(id);
        return ResponseEntity.ok(ApiResponse.success(LocationConstant.LOCATION_DELETED_SUCCESS, null));
    }
}
