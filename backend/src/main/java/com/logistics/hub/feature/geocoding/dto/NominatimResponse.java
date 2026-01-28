package com.logistics.hub.feature.geocoding.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class NominatimResponse {
    
    @JsonProperty("lat")
    private String lat;
    
    @JsonProperty("lon")
    private String lon;
    
    @JsonProperty("display_name")
    private String displayName;
}
