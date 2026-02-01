package com.logistics.hub.feature.driver.service;

import com.logistics.hub.feature.driver.dto.request.DriverRequest;
import com.logistics.hub.feature.driver.dto.response.DriverResponse;

import com.logistics.hub.feature.driver.dto.response.DriverStatisticsResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface DriverService {

    Page<DriverResponse> findAll(Pageable pageable, String search);

    DriverResponse findById(Long id);

    DriverResponse create(DriverRequest request);

    DriverResponse update(Long id, DriverRequest request);

    void delete(Long id);

    List<DriverResponse> getAvailableDrivers(Long includeDriverId);

    DriverStatisticsResponse getStatistics();
}
