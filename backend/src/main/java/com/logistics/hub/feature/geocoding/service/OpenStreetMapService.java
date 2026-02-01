package com.logistics.hub.feature.geocoding.service;

import com.logistics.hub.common.exception.ValidationException;
import com.logistics.hub.feature.geocoding.dto.Coordinates;
import com.logistics.hub.feature.geocoding.dto.NominatimResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
@Slf4j
public class OpenStreetMapService {

    private final RestTemplate restTemplate;
    
    private static final String NOMINATIM_URL = "https://nominatim.openstreetmap.org/search";
    private static final String USER_AGENT = "LogisticsControlHub/1.0 (contact@logistics-hub.com)";

    public Coordinates geocode(String address) {
        if (address == null || address.trim().isEmpty()) {
            throw new ValidationException("Address cannot be null or empty");
        }

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Rate limiting sleep interrupted", e);
        }

        HttpHeaders headers = new HttpHeaders();
        headers.set("User-Agent", USER_AGENT);

        String url = String.format("%s?q=%s&format=json&limit=1", 
                NOMINATIM_URL, 
                address.replace(" ", "+"));

        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<NominatimResponse[]> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    NominatimResponse[].class
            );

            NominatimResponse[] results = response.getBody();
            
            if (results == null || results.length == 0) {
                log.warn("No coordinates found for address: {}", address);
                return null;
            }

            NominatimResponse firstResult = results[0];
            Double lat = Double.parseDouble(firstResult.getLat());
            Double lon = Double.parseDouble(firstResult.getLon());

            log.info("Geocoded address '{}' to coordinates: ({}, {})", address, lat, lon);
            return new Coordinates(lat, lon);

        } catch (Exception e) {
            log.error("Error geocoding address: {}", address, e);
            throw new RuntimeException("Failed to geocode address: " + address, e);
        }
    }
}
