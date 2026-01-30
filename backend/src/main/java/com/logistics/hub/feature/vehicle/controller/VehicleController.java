package com.logistics.hub.feature.vehicle.controller;

import com.logistics.hub.common.base.ApiResponse;
import com.logistics.hub.common.base.PaginatedResponse;
import com.logistics.hub.feature.vehicle.constant.VehicleConstant;
import com.logistics.hub.feature.vehicle.dto.request.VehicleRequest;
import com.logistics.hub.feature.vehicle.dto.response.VehicleResponse;
import com.logistics.hub.feature.vehicle.dto.response.VehicleStatisticsResponse;
import com.logistics.hub.feature.vehicle.enums.VehicleStatus;
import com.logistics.hub.feature.vehicle.service.VehicleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.logistics.hub.common.constant.UrlConstant;

@RestController
@RequestMapping(UrlConstant.Vehicle.PREFIX)
@RequiredArgsConstructor
public class VehicleController {

    private final VehicleService vehicleService;

    @GetMapping
    public ResponseEntity<ApiResponse<PaginatedResponse<VehicleResponse>>> findAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) VehicleStatus status,
            @RequestParam(required = false) String search
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<VehicleResponse> vehiclePage = vehicleService.findAll(pageable, status, search);
        
        PaginatedResponse<VehicleResponse> response = new PaginatedResponse<>();
        response.setData(vehiclePage.getContent());
        response.setPagination(new PaginatedResponse.PaginationInfo(
                vehiclePage.getNumber(),
                vehiclePage.getSize(),
                vehiclePage.getTotalElements(),
                vehiclePage.getTotalPages()
        ));
        
        return ResponseEntity.ok(ApiResponse.success(VehicleConstant.VEHICLES_RETRIEVED_SUCCESS, response));
    }

    @GetMapping(UrlConstant.Vehicle.BY_ID)
    public ResponseEntity<ApiResponse<?>> findById(@PathVariable Long id) {
        VehicleResponse vehicle = vehicleService.findById(id);
        return ResponseEntity.ok(ApiResponse.success(VehicleConstant.VEHICLE_RETRIEVED_SUCCESS, vehicle));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<?>> create(@Valid @RequestBody VehicleRequest request) {
        VehicleResponse createdVehicle = vehicleService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(201, VehicleConstant.VEHICLE_CREATED_SUCCESS, createdVehicle));
    }

    @PutMapping(UrlConstant.Vehicle.BY_ID)
    public ResponseEntity<ApiResponse<?>> update(@PathVariable Long id, @Valid @RequestBody VehicleRequest request) {
        VehicleResponse updatedVehicle = vehicleService.update(id, request);
        return ResponseEntity.ok(ApiResponse.success(VehicleConstant.VEHICLE_UPDATED_SUCCESS, updatedVehicle));
    }

    @DeleteMapping(UrlConstant.Vehicle.BY_ID)
    public ResponseEntity<ApiResponse<?>> delete(@PathVariable Long id) {
        vehicleService.delete(id);
        return ResponseEntity.ok(ApiResponse.success(VehicleConstant.VEHICLE_DELETED_SUCCESS, null));
    }

    @GetMapping(UrlConstant.Vehicle.STATISTICS)
    public ResponseEntity<ApiResponse<?>> getStatistics() {
        VehicleStatisticsResponse statistics = vehicleService.getStatistics();
        return ResponseEntity.ok(ApiResponse.success(VehicleConstant.VEHICLE_STATISTICS_RETRIEVED_SUCCESS, statistics));
    }
}
