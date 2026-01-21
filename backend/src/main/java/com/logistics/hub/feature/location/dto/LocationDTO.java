package com.logistics.hub.feature.location.dto;

import com.logistics.hub.feature.location.enums.LocationType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LocationDTO {
    private Long id;

    @NotBlank(message = "Tên địa điểm không được để trống")
    private String name;

    @NotNull(message = "Latitude không được để trống")
    @Min(value = -90, message = "Latitude phải từ -90 đến 90")
    @Max(value = 90, message = "Latitude phải từ -90 đến 90")
    private Double latitude;

    @NotNull(message = "Longitude không được để trống")
    @Min(value = -180, message = "Longitude phải từ -180 đến 180")
    @Max(value = 180, message = "Longitude phải từ -180 đến 180")
    private Double longitude;

    @NotNull(message = "Loại địa điểm không được để trống")
    private LocationType type;

    private String address;
}
