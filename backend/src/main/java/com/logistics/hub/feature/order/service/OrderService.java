package com.logistics.hub.feature.order.service;

import com.logistics.hub.feature.order.dto.request.OrderRequest;
import com.logistics.hub.feature.order.enums.OrderStatus;
import com.logistics.hub.feature.order.dto.response.OrderResponse;

import java.util.List;

public interface OrderService {

    org.springframework.data.domain.Page<OrderResponse> findAll(org.springframework.data.domain.Pageable pageable,
            com.logistics.hub.feature.order.enums.OrderStatus status, String search, Long depotId);

    OrderResponse findById(Long id);

    OrderResponse create(OrderRequest request);

    OrderResponse update(Long id, OrderRequest request);

    void updateStatusBulk(List<Long> orderIds, OrderStatus status);

    void delete(Long id);

    com.logistics.hub.feature.order.dto.response.OrderStatisticsResponse getStatistics();
}
