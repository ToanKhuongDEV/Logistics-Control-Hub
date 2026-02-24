package com.logistics.hub.feature.driver.dto.request;

import com.logistics.hub.feature.driver.constant.DriverConstant;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class DriverRequest {

    @NotBlank(message = DriverConstant.NAME_REQUIRED)
    @Size(max = 100, message = DriverConstant.NAME_LENGTH_EXCEEDED)
    private String name;

    @NotBlank(message = DriverConstant.LICENSE_NUMBER_REQUIRED)
    @jakarta.validation.constraints.Pattern(regexp = DriverConstant.LICENSE_NUMBER_REGEX, message = DriverConstant.LICENSE_INVALID_FORMAT)
    private String licenseNumber;

    @NotBlank(message = DriverConstant.PHONE_NUMBER_REQUIRED)
    @Size(max = 20, message = DriverConstant.PHONE_LENGTH_EXCEEDED)
    @jakarta.validation.constraints.Pattern(regexp = "^\\d{10}$", message = DriverConstant.PHONE_INVALID_FORMAT)
    private String phoneNumber;

    @Size(max = 255, message = DriverConstant.EMAIL_LENGTH_EXCEEDED)
    @jakarta.validation.constraints.Email(message = DriverConstant.EMAIL_INVALID_FORMAT)
    private String email;
}
