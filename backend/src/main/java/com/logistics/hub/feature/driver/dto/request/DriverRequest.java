package com.logistics.hub.feature.driver.dto.request;

import com.logistics.hub.feature.driver.constant.DriverConstant;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class DriverRequest {

    @NotBlank(message = DriverConstant.NAME_REQUIRED)
    @Size(max = 100, message = "Driver name must not exceed 100 characters")
    private String name;

    @NotBlank(message = DriverConstant.LICENSE_NUMBER_REQUIRED)
    @Size(max = 50, message = "License number must not exceed 50 characters")
    private String licenseNumber;

    @NotBlank(message = DriverConstant.PHONE_NUMBER_REQUIRED)
    @Size(max = 20, message = "Phone number must not exceed 20 characters")
    private String phoneNumber;

    @Size(max = 255, message = "Email must not exceed 255 characters")
    private String email;
}
