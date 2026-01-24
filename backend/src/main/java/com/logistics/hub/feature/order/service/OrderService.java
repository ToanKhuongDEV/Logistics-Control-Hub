package com.logistics.hub.feature.order.service;

import com.logistics.hub.feature.order.dto.request.OrderRequest;
import com.logistics.hub.feature.order.dto.response.OrderResponse;

import java.util.List;

/**
 * Service interface for Order operations
 */
public interface OrderService {
    
    List<OrderResponse> findAll();
    
    OrderResponse findById(Long id);
    
    OrderResponse create(OrderRequest request);
    
    OrderResponse update(Long id, OrderRequest request);
    
    void delete(Long id);
}
