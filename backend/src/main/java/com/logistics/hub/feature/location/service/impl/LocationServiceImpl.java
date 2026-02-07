package com.logistics.hub.feature.location.service.impl;

import com.logistics.hub.feature.geocoding.dto.Coordinates;
import com.logistics.hub.feature.geocoding.service.OpenStreetMapService;
import com.logistics.hub.feature.location.constant.LocationConstant;
import com.logistics.hub.feature.location.dto.request.LocationRequest;
import com.logistics.hub.feature.location.entity.LocationEntity;
import com.logistics.hub.feature.location.mapper.LocationMapper;
import com.logistics.hub.feature.location.repository.LocationRepository;
import com.logistics.hub.feature.location.service.LocationService;
import com.logistics.hub.common.exception.GeocodingException;
import com.logistics.hub.common.exception.ResourceNotFoundException;
import com.logistics.hub.feature.location.dto.response.LocationResponse;
import com.logistics.hub.feature.depot.repository.DepotRepository;
import com.logistics.hub.feature.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class LocationServiceImpl implements LocationService {

    private final LocationRepository locationRepository;
    private final LocationMapper locationMapper;
    private final OpenStreetMapService openStreetMapService;
    private final DepotRepository depotRepository;
    private final OrderRepository orderRepository;

    @Override
    public LocationEntity getOrCreateLocation(LocationRequest request) {
        String fullAddress = String.format(
                "%s, %s, %s",
                request.getStreet(),
                request.getCity(),
                request.getCountry());

        Coordinates coords = openStreetMapService.geocode(fullAddress);
        if (coords == null) {
            throw new GeocodingException(LocationConstant.GEOCODE_ERROR + fullAddress);
        }

        double latitude = Math.round(coords.getLatitude() * 1000000.0) / 1000000.0;
        double longitude = Math.round(coords.getLongitude() * 1000000.0) / 1000000.0;

        Optional<LocationEntity> existing = locationRepository.findByLatitudeAndLongitude(latitude, longitude);

        if (existing.isPresent()) {
            return existing.get();
        }

        LocationEntity loc = new LocationEntity();
        loc.setStreet(request.getStreet());
        loc.setCity(request.getCity());
        loc.setCountry(request.getCountry());
        loc.setLatitude(latitude);
        loc.setLongitude(longitude);

        try {
            return locationRepository.save(loc);
        } catch (DataIntegrityViolationException ex) {
            return locationRepository
                    .findByLatitudeAndLongitude(latitude, longitude)
                    .orElseThrow(() -> new IllegalStateException(
                            LocationConstant.LOCATION_RETRIEVAL_ERROR, ex));
        }
    }

    @Override
    public LocationResponse create(LocationRequest request) {
        LocationEntity entity = getOrCreateLocation(request);
        return locationMapper.toResponse(entity);
    }

    @Override
    public void delete(Long id) {
        if (!locationRepository.existsById(id)) {
            throw new ResourceNotFoundException(LocationConstant.LOCATION_NOT_FOUND + id);
        }

        if (depotRepository.existsByLocationId(id)) {
            throw new com.logistics.hub.common.exception.ValidationException(LocationConstant.LOCATION_IN_USE_BY_DEPOT);
        }

        if (orderRepository.existsByDeliveryLocationId(id)) {
            throw new com.logistics.hub.common.exception.ValidationException(
                    LocationConstant.LOCATION_IN_USE_BY_ORDERS);
        }

        try {
            locationRepository.deleteById(id);
        } catch (DataIntegrityViolationException ex) {
            throw new com.logistics.hub.common.exception.ValidationException(
                    LocationConstant.LOCATION_IN_USE_BY_DEPOT);
        }
    }
}
