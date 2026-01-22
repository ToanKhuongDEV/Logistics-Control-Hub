package com.logistics.hub.common.valueobject;

import com.logistics.hub.common.constant.MessageConstant;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Location {

    @Min(value = -90, message = MessageConstant.LATITUDE_INVALID)
    @Max(value = 90, message = MessageConstant.LATITUDE_INVALID)
    private Double latitude;

    @Min(value = -180, message = MessageConstant.LONGITUDE_INVALID)
    @Max(value = 180, message = MessageConstant.LONGITUDE_INVALID)
    private Double longitude;

    // Distance calculation logic moved to com.logistics.hub.common.util.GeoUtils
}
