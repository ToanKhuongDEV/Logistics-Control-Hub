package com.logistics.hub.feature.order.service.impl;

import com.logistics.hub.common.exception.ResourceNotFoundException;
import com.logistics.hub.feature.order.constant.OrderConstant;
import com.logistics.hub.feature.order.dto.request.OrderRequest;
import com.logistics.hub.feature.order.dto.response.OrderResponse;
import com.logistics.hub.feature.order.entity.OrderEntity;
import com.logistics.hub.feature.order.enums.OrderStatus;
import com.logistics.hub.feature.order.repository.OrderRepository;
import com.logistics.hub.feature.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of OrderService
 */
@Service
@RequiredArgsConstructor
@Transactional
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> findAll() {
        return orderRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse findById(Long id) {
        OrderEntity entity = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(OrderConstant.ORDER_NOT_FOUND + id));
        return toResponse(entity);
    }

    @Override
    public OrderResponse create(OrderRequest request) {
        OrderEntity entity = new OrderEntity();
        entity.setCode(request.getCode());
        entity.setDeliveryLocationId(request.getDeliveryLocationId());
        entity.setWeightKg(request.getWeightKg());
        entity.setVolumeM3(request.getVolumeM3());
        entity.setStatus(OrderStatus.CREATED);
        
        OrderEntity saved = orderRepository.save(entity);
        return toResponse(saved);
    }

    @Override
    public OrderResponse update(Long id, OrderRequest request) {
        OrderEntity entity = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(OrderConstant.ORDER_NOT_FOUND + id));
        
        entity.setCode(request.getCode());
        entity.setDeliveryLocationId(request.getDeliveryLocationId());
        entity.setWeightKg(request.getWeightKg());
        entity.setVolumeM3(request.getVolumeM3());
        if (request.getStatus() != null) {
            entity.setStatus(OrderStatus.valueOf(request.getStatus()));
        }
        
        OrderEntity saved = orderRepository.save(entity);
        return toResponse(saved);
    }

    @Override
    public void delete(Long id) {
        if (!orderRepository.existsById(id)) {
            throw new ResourceNotFoundException(OrderConstant.ORDER_NOT_FOUND + id);
        }
        orderRepository.deleteById(id);
    }

    private OrderResponse toResponse(OrderEntity entity) {
        OrderResponse response = new OrderResponse();
        response.setId(entity.getId());
        response.setCode(entity.getCode());
        response.setDeliveryLocationId(entity.getDeliveryLocationId());
        response.setWeightKg(entity.getWeightKg());
        response.setVolumeM3(entity.getVolumeM3());
        response.setStatus(entity.getStatus());
        response.setCreatedAt(entity.getCreatedAt());
        return response;
    }
}
