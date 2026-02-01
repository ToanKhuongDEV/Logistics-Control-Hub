package com.logistics.hub.feature.driver.service.impl;


import com.logistics.hub.common.exception.ResourceNotFoundException;
import com.logistics.hub.common.exception.ValidationException;
import com.logistics.hub.feature.driver.constant.DriverConstant;
import com.logistics.hub.feature.driver.dto.request.DriverRequest;
import com.logistics.hub.feature.driver.dto.response.DriverResponse;
import com.logistics.hub.feature.driver.entity.DriverEntity;

import com.logistics.hub.feature.driver.mapper.DriverMapper;
import com.logistics.hub.feature.driver.repository.DriverRepository;
import com.logistics.hub.feature.driver.service.DriverService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DriverServiceImpl implements DriverService {

    private final DriverRepository driverRepository;
    private final DriverMapper driverMapper;

    @Override
    @Transactional(readOnly = true)
    public Page<DriverResponse> findAll(Pageable pageable, String search) {
        return driverRepository.findBySearch(search, pageable)
                .map(driverMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public DriverResponse findById(Long id) {
        DriverEntity driver = driverRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(DriverConstant.DRIVER_NOT_FOUND));
        return driverMapper.toResponse(driver);
    }

    @Override
    @Transactional
    public DriverResponse create(DriverRequest request) {
        validateDriverRequest(request, null);
        DriverEntity driver = driverMapper.toEntity(request);
        DriverEntity savedDriver = driverRepository.save(driver);
        return driverMapper.toResponse(savedDriver);
    }

    @Override
    @Transactional
    public DriverResponse update(Long id, DriverRequest request) {
        DriverEntity driver = driverRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(DriverConstant.DRIVER_NOT_FOUND));
        
        validateDriverRequest(request, id);
        
        driverMapper.updateEntityFromRequest(request, driver);
        DriverEntity updatedDriver = driverRepository.save(driver);
        return driverMapper.toResponse(updatedDriver);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!driverRepository.existsById(id)) {
            throw new ResourceNotFoundException(DriverConstant.DRIVER_NOT_FOUND);
        }
        driverRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public java.util.List<DriverResponse> getAvailableDrivers(Long includeDriverId) {
        return driverRepository.findAvailableDrivers(includeDriverId)
                .stream()
                .map(driverMapper::toResponse)
                .toList();
    }

    private void validateDriverRequest(DriverRequest request, Long id) {
        boolean licenseExists = (id == null) ? 
                driverRepository.existsByLicenseNumber(request.getLicenseNumber()) : 
                driverRepository.existsByLicenseNumberAndIdNot(request.getLicenseNumber(), id);
                
        if (licenseExists) {
            throw new ValidationException(DriverConstant.LICENSE_NUMBER_EXISTS);
        }

        boolean phoneExists = (id == null) ? 
                driverRepository.existsByPhoneNumber(request.getPhoneNumber()) : 
                driverRepository.existsByPhoneNumberAndIdNot(request.getPhoneNumber(), id);
                
        if (phoneExists) {
            throw new ValidationException(DriverConstant.PHONE_NUMBER_EXISTS);
        }
    }


}
