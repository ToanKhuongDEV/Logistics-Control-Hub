package com.logistics.hub.feature.location.controller;

import com.logistics.hub.feature.location.dto.LocationDTO;
import com.logistics.hub.feature.location.service.LocationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/locations")
@RequiredArgsConstructor
@Tag(name = "Location Management", description = "APIs for managing locations (Warehouses, Hubs, Customers)")
public class LocationController {

    private final LocationService locationService;

    @Operation(summary = "Create a new location")
    @PostMapping
    public ResponseEntity<LocationDTO> createLocation(@Valid @RequestBody LocationDTO locationDTO) {
        LocationDTO createdLocation = locationService.createLocation(locationDTO);
        return new ResponseEntity<>(createdLocation, HttpStatus.CREATED);
    }

    @Operation(summary = "Update an existing location")
    @PutMapping("/{id}")
    public ResponseEntity<LocationDTO> updateLocation(@PathVariable Long id, @Valid @RequestBody LocationDTO locationDTO) {
        LocationDTO updatedLocation = locationService.updateLocation(id, locationDTO);
        return ResponseEntity.ok(updatedLocation);
    }

    @Operation(summary = "Delete a location")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLocation(@PathVariable Long id) {
        locationService.deleteLocation(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Get location by ID")
    @GetMapping("/{id}")
    public ResponseEntity<LocationDTO> getLocationById(@PathVariable Long id) {
        return ResponseEntity.ok(locationService.getLocationById(id));
    }

    @Operation(summary = "Get all locations")
    @GetMapping
    public ResponseEntity<List<LocationDTO>> getAllLocations() {
        return ResponseEntity.ok(locationService.getAllLocations());
    }
}
