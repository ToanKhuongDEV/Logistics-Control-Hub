package com.logistics.hub.feature.vehicle.service;

import com.logistics.hub.feature.vehicle.dto.request.VehicleRequest;
import com.logistics.hub.feature.vehicle.dto.response.VehicleResponse;
import com.logistics.hub.feature.vehicle.dto.response.VehicleStatisticsResponse;
import com.logistics.hub.feature.vehicle.enums.VehicleStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface VehicleService {
    
    Page<VehicleResponse> findAll(Pageable pageable, VehicleStatus status, String search);
    
    VehicleResponse findById(Long id);
    
    VehicleResponse create(VehicleRequest request);
    
    VehicleResponse update(Long id, VehicleRequest request);
    
    void delete(Long id);
    
    VehicleStatisticsResponse getStatistics();
}
