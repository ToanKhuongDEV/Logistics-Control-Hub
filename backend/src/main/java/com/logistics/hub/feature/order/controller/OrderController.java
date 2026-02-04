package com.logistics.hub.feature.order.controller;

import com.logistics.hub.common.base.ApiResponse;
import com.logistics.hub.common.base.PaginatedResponse;
import com.logistics.hub.feature.order.constant.OrderConstant;
import com.logistics.hub.feature.order.dto.request.OrderRequest;
import com.logistics.hub.feature.order.dto.response.OrderResponse;
import com.logistics.hub.feature.order.dto.response.OrderStatisticsResponse;
import com.logistics.hub.feature.order.enums.OrderStatus;
import com.logistics.hub.feature.order.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.logistics.hub.common.constant.UrlConstant;

@RestController
@RequestMapping(UrlConstant.Order.PREFIX)
@RequiredArgsConstructor
@Tag(name = "Order", description = "APIs for managing delivery orders")
public class OrderController {

    private final OrderService orderService;

    @GetMapping
    @Operation(summary = "Get all orders", description = "Returns a paginated list of orders with optional filtering")
    public ResponseEntity<ApiResponse<PaginatedResponse<OrderResponse>>> findAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(required = false) String search) {
        Pageable pageable = PageRequest.of(page, size);
        Page<OrderResponse> orderPage = orderService.findAll(pageable, status, search);

        PaginatedResponse<OrderResponse> response = new PaginatedResponse<>();
        response.setData(orderPage.getContent());
        response.setPagination(new PaginatedResponse.PaginationInfo(
                orderPage.getNumber(),
                orderPage.getSize(),
                orderPage.getTotalElements(),
                orderPage.getTotalPages()));

        return ResponseEntity.ok(ApiResponse.success(OrderConstant.ORDERS_RETRIEVED_SUCCESS, response));
    }

    @GetMapping(UrlConstant.Order.STATISTICS)
    @Operation(summary = "Get order statistics", description = "Returns overall statistics about orders status")
    public ResponseEntity<ApiResponse<?>> getStatistics() {
        OrderStatisticsResponse statistics = orderService.getStatistics();
        return ResponseEntity.ok(ApiResponse.success(OrderConstant.ORDER_STATISTICS_RETRIEVED_SUCCESS, statistics));
    }

    @GetMapping(UrlConstant.Order.BY_ID)
    @Operation(summary = "Get order by ID", description = "Returns detailed information of a single order")
    public ResponseEntity<ApiResponse<?>> findById(@PathVariable Long id) {
        OrderResponse order = orderService.findById(id);
        return ResponseEntity.ok(ApiResponse.success(OrderConstant.ORDER_RETRIEVED_SUCCESS, order));
    }

    @PostMapping
    @Operation(summary = "Create new order", description = "Adds a new delivery order to the system")
    public ResponseEntity<ApiResponse<?>> create(@Valid @RequestBody OrderRequest request) {
        OrderResponse createdOrder = orderService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(201, OrderConstant.ORDER_CREATED_SUCCESS, createdOrder));
    }

    @PutMapping(UrlConstant.Order.BY_ID)
    @Operation(summary = "Update order", description = "Updates an existing order's information")
    public ResponseEntity<ApiResponse<?>> update(@PathVariable Long id, @Valid @RequestBody OrderRequest request) {
        OrderResponse updatedOrder = orderService.update(id, request);
        return ResponseEntity.ok(ApiResponse.success(OrderConstant.ORDER_UPDATED_SUCCESS, updatedOrder));
    }

    @DeleteMapping(UrlConstant.Order.BY_ID)
    @Operation(summary = "Delete order", description = "Removes an order from the system by its ID")
    public ResponseEntity<ApiResponse<?>> delete(@PathVariable Long id) {
        orderService.delete(id);
        return ResponseEntity.ok(ApiResponse.success(OrderConstant.ORDER_DELETED_SUCCESS, null));
    }
}
