package com.logistics.hub.feature.order.service;

import com.logistics.hub.feature.order.dto.request.DeliveryOrderRequest;
import com.logistics.hub.feature.order.dto.response.DeliveryOrderResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface DeliveryOrderService {
    DeliveryOrderResponse createOrder(DeliveryOrderRequest request);
    DeliveryOrderResponse getOrderById(Long id);
    DeliveryOrderResponse updateOrder(Long id, DeliveryOrderRequest request);
    void deleteOrder(Long id);
    Page<DeliveryOrderResponse> getAllOrders(Pageable pageable);
}
