package com.logistics.hub.feature.order.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * Request DTO for creating/updating orders
 * Matches OrderEntity fields
 */
@Data
public class OrderRequest {
    
    @NotBlank(message = "Order code is required")
    private String code;

    @NotNull(message = "Delivery location ID is required")
    private Long deliveryLocationId;

    private Integer weightKg;

    private BigDecimal volumeM3;

    private String status; // CREATED, ASSIGNED, IN_TRANSIT, DELIVERED, CANCELLED
}
