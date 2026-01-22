package com.logistics.hub.feature.order.controller;

import com.logistics.hub.common.constant.UrlConstant;
import com.logistics.hub.feature.order.dto.request.DeliveryOrderRequest;
import com.logistics.hub.feature.order.dto.response.DeliveryOrderResponse;
import com.logistics.hub.feature.order.service.DeliveryOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Tag(name = "Order Management", description = "APIs for managing delivery orders")
public class DeliveryOrderController {

    private final DeliveryOrderService deliveryOrderService;

    @PostMapping(UrlConstant.Order.ORDER_COMMON)
    @Operation(summary = "Create new order", description = "Creates a new delivery order")
    public ResponseEntity<DeliveryOrderResponse> createOrder(@Valid @RequestBody DeliveryOrderRequest request) {
        DeliveryOrderResponse response = deliveryOrderService.createOrder(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping(UrlConstant.Order.ORDER_ID)
    @Operation(summary = "Get order by ID", description = "Retrieves a delivery order by its unique ID")
    public ResponseEntity<DeliveryOrderResponse> getOrderById(@PathVariable Long id) {
        DeliveryOrderResponse response = deliveryOrderService.getOrderById(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping(UrlConstant.Order.ORDER_ID)
    @Operation(summary = "Update order", description = "Updates an existing delivery order")
    public ResponseEntity<DeliveryOrderResponse> updateOrder(
            @PathVariable Long id,
            @Valid @RequestBody DeliveryOrderRequest request) {
        DeliveryOrderResponse response = deliveryOrderService.updateOrder(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping(UrlConstant.Order.ORDER_ID)
    @Operation(summary = "Delete order", description = "Deletes a delivery order by ID")
    public ResponseEntity<Void> deleteOrder(@PathVariable Long id) {
        deliveryOrderService.deleteOrder(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping(UrlConstant.Order.ORDER_COMMON)
    @Operation(summary = "List all orders", description = "Retrieves a paginated list of all delivery orders")
    public ResponseEntity<Page<DeliveryOrderResponse>> getAllOrders(
            @PageableDefault(size = 20) Pageable pageable) {
        Page<DeliveryOrderResponse> response = deliveryOrderService.getAllOrders(pageable);
        return ResponseEntity.ok(response);
    }
}
