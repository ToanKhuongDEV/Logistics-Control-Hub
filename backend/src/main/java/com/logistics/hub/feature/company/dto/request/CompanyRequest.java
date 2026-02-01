package com.logistics.hub.feature.company.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import com.logistics.hub.feature.company.constant.CompanyConstant;

@Data
public class CompanyRequest {

    @NotBlank(message = CompanyConstant.NAME_REQUIRED)
    private String name;

    @NotBlank(message = CompanyConstant.ADDRESS_REQUIRED)
    private String address;

    @NotBlank(message = CompanyConstant.PHONE_REQUIRED)
    @Pattern(regexp = "^[0-9]{10,11}$", message = CompanyConstant.PHONE_INVALID_FORMAT)
    private String phone;

    private String email;
    private String website;
    private String taxId;
    private String description;
}
