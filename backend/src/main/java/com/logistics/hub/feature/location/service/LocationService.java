package com.logistics.hub.feature.location.service;

import com.logistics.hub.feature.location.dto.LocationDTO;
import java.util.List;

public interface LocationService {
    LocationDTO createLocation(LocationDTO locationDTO);
    LocationDTO updateLocation(Long id, LocationDTO locationDTO);
    void deleteLocation(Long id);
    LocationDTO getLocationById(Long id);
    List<LocationDTO> getAllLocations();
}
