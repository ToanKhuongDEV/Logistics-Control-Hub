package com.logistics.hub.feature.driver.controller;

import com.logistics.hub.common.base.ApiResponse;
import com.logistics.hub.common.base.PaginatedResponse;
import com.logistics.hub.common.constant.UrlConstant;
import com.logistics.hub.feature.driver.constant.DriverConstant;
import com.logistics.hub.feature.driver.dto.request.DriverRequest;
import com.logistics.hub.feature.driver.dto.response.DriverResponse;

import com.logistics.hub.feature.driver.dto.response.DriverStatisticsResponse;
import com.logistics.hub.feature.driver.service.DriverService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping(UrlConstant.Driver.PREFIX)
@RequiredArgsConstructor
public class DriverController {

    private final DriverService driverService;

    @GetMapping
    public ResponseEntity<ApiResponse<PaginatedResponse<DriverResponse>>> findAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<DriverResponse> driverPage = driverService.findAll(pageable, search);

        PaginatedResponse<DriverResponse> response = new PaginatedResponse<>();
        response.setData(driverPage.getContent());
        response.setPagination(new PaginatedResponse.PaginationInfo(
                driverPage.getNumber(),
                driverPage.getSize(),
                driverPage.getTotalElements(),
                driverPage.getTotalPages()
        ));

        return ResponseEntity.ok(ApiResponse.success(DriverConstant.DRIVERS_RETRIEVED_SUCCESS, response));
    }

    @GetMapping(UrlConstant.Driver.AVAILABLE)
    public ResponseEntity<ApiResponse<List<DriverResponse>>> getAvailableDrivers(
            @RequestParam(required = false) Long includeDriverId
    ) {
        List<DriverResponse> drivers = driverService.getAvailableDrivers(includeDriverId);
        return ResponseEntity.ok(ApiResponse.success(DriverConstant.AVAILABLE_DRIVERS_RETRIEVED_SUCCESS, drivers));
    }

    @GetMapping(UrlConstant.Driver.BY_ID)
    public ResponseEntity<ApiResponse<DriverResponse>> findById(@PathVariable Long id) {
        DriverResponse driver = driverService.findById(id);
        return ResponseEntity.ok(ApiResponse.success(DriverConstant.DRIVER_RETRIEVED_SUCCESS, driver));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<DriverResponse>> create(@Valid @RequestBody DriverRequest request) {
        DriverResponse createdDriver = driverService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(201, DriverConstant.DRIVER_CREATED_SUCCESS, createdDriver));
    }

    @PutMapping(UrlConstant.Driver.BY_ID)
    public ResponseEntity<ApiResponse<DriverResponse>> update(@PathVariable Long id, @Valid @RequestBody DriverRequest request) {
        DriverResponse updatedDriver = driverService.update(id, request);
        return ResponseEntity.ok(ApiResponse.success(DriverConstant.DRIVER_UPDATED_SUCCESS, updatedDriver));
    }

    @DeleteMapping(UrlConstant.Driver.BY_ID)
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        driverService.delete(id);
        return ResponseEntity.ok(ApiResponse.success(DriverConstant.DRIVER_DELETED_SUCCESS, null));
    }

    @GetMapping(UrlConstant.Driver.STATISTICS)
    public ResponseEntity<ApiResponse<DriverStatisticsResponse>> getStatistics() {
        DriverStatisticsResponse statistics = driverService.getStatistics();
        return ResponseEntity.ok(ApiResponse.success(DriverConstant.DRIVER_STATISTICS_RETRIEVED_SUCCESS, statistics));
    }
}
