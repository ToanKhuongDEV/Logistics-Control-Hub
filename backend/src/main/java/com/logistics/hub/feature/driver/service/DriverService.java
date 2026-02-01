package com.logistics.hub.feature.driver.service;

import com.logistics.hub.feature.driver.dto.request.DriverRequest;
import com.logistics.hub.feature.driver.dto.response.DriverResponse;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface DriverService {

    Page<DriverResponse> findAll(Pageable pageable, String search);

    DriverResponse findById(Long id);

    DriverResponse create(DriverRequest request);

    DriverResponse update(Long id, DriverRequest request);

    void delete(Long id);

    java.util.List<DriverResponse> getAvailableDrivers(Long includeDriverId);
}
