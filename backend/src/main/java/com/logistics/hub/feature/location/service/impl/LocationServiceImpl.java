package com.logistics.hub.feature.location.service.impl;

import com.logistics.hub.feature.location.dto.LocationDTO;
import com.logistics.hub.feature.location.entity.LocationEntity;
import com.logistics.hub.feature.location.mapper.LocationMapper;
import com.logistics.hub.feature.location.repository.LocationRepository;
import com.logistics.hub.feature.location.service.LocationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class LocationServiceImpl implements LocationService {

    private final LocationRepository locationRepository;
    private final LocationMapper locationMapper;

    @Override
    public LocationDTO createLocation(LocationDTO locationDTO) {
        LocationEntity entity = locationMapper.toEntity(locationDTO);
        LocationEntity savedEntity = locationRepository.save(entity);
        return locationMapper.toDTO(savedEntity);
    }

    @Override
    public LocationDTO updateLocation(Long id, LocationDTO locationDTO) {
        LocationEntity existingEntity = locationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Location not found with id: " + id));
        
        locationMapper.updateEntityFromDTO(locationDTO, existingEntity);
        existingEntity.setId(id); // Ensure ID doesn't change
        
        LocationEntity updatedEntity = locationRepository.save(existingEntity);
        return locationMapper.toDTO(updatedEntity);
    }

    @Override
    public void deleteLocation(Long id) {
        if (!locationRepository.existsById(id)) {
            throw new RuntimeException("Location not found with id: " + id);
        }
        locationRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public LocationDTO getLocationById(Long id) {
        LocationEntity entity = locationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Location not found with id: " + id));
        return locationMapper.toDTO(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LocationDTO> getAllLocations() {
        return locationRepository.findAll().stream()
                .map(locationMapper::toDTO)
                .collect(Collectors.toList());
    }
}
