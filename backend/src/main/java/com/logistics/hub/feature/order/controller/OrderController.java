package com.logistics.hub.feature.order.controller;

import com.logistics.hub.common.base.ApiResponse;
import com.logistics.hub.feature.order.constant.OrderConstant;
import com.logistics.hub.feature.order.dto.request.OrderRequest;
import com.logistics.hub.feature.order.dto.response.OrderResponse;
import com.logistics.hub.feature.order.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import com.logistics.hub.common.constant.UrlConstant;

@RestController
@RequestMapping(UrlConstant.Order.PREFIX)
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<OrderResponse>>> findAll() {
        List<OrderResponse> orders = orderService.findAll();
        return ResponseEntity.ok(ApiResponse.success(OrderConstant.ORDERS_RETRIEVED_SUCCESS, orders));
    }

    @GetMapping(UrlConstant.Order.BY_ID)
    public ResponseEntity<ApiResponse<?>> findById(@PathVariable Long id) {
        OrderResponse order = orderService.findById(id);
        return ResponseEntity.ok(ApiResponse.success(OrderConstant.ORDER_RETRIEVED_SUCCESS, order));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<?>> create(@Valid @RequestBody OrderRequest request) {
        OrderResponse createdOrder = orderService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(201, OrderConstant.ORDER_CREATED_SUCCESS, createdOrder));
    }

    @PutMapping(UrlConstant.Order.BY_ID)
    public ResponseEntity<ApiResponse<?>> update(@PathVariable Long id, @Valid @RequestBody OrderRequest request) {
        OrderResponse updatedOrder = orderService.update(id, request);
        return ResponseEntity.ok(ApiResponse.success(OrderConstant.ORDER_UPDATED_SUCCESS, updatedOrder));
    }

    @DeleteMapping(UrlConstant.Order.BY_ID)
    public ResponseEntity<ApiResponse<?>> delete(@PathVariable Long id) {
        orderService.delete(id);
        return ResponseEntity.ok(ApiResponse.success(OrderConstant.ORDER_DELETED_SUCCESS, null));
    }
}
