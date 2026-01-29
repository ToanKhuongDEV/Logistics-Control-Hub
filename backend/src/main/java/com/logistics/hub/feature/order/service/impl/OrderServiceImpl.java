package com.logistics.hub.feature.order.service.impl;

import com.logistics.hub.common.exception.ResourceNotFoundException;
import com.logistics.hub.feature.order.constant.OrderConstant;
import com.logistics.hub.feature.order.dto.request.OrderRequest;
import com.logistics.hub.feature.order.dto.response.OrderResponse;
import com.logistics.hub.feature.order.entity.OrderEntity;
import com.logistics.hub.feature.order.enums.OrderStatus;
import com.logistics.hub.feature.order.repository.OrderRepository;
import com.logistics.hub.feature.order.service.OrderService;
import com.logistics.hub.feature.order.mapper.OrderMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@Transactional
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final com.logistics.hub.feature.location.service.LocationService locationService;

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> findAll() {
        return orderRepository.findAll().stream()
                .map(orderMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse findById(Long id) {
        OrderEntity entity = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(OrderConstant.ORDER_NOT_FOUND + id));
        return orderMapper.toResponse(entity);
    }

    @Override
    public OrderResponse create(OrderRequest request) {
        OrderEntity entity = orderMapper.toEntity(request);
        entity.setStatus(OrderStatus.CREATED);
        
        com.logistics.hub.feature.location.entity.LocationEntity location = locationService.getOrCreateLocation(request.getDeliveryLocation());
        entity.setDeliveryLocationId(location.getId());
        
        OrderEntity saved = orderRepository.save(entity);
        return orderMapper.toResponse(saved);
    }

    @Override
    public OrderResponse update(Long id, OrderRequest request) {
        OrderEntity entity = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(OrderConstant.ORDER_NOT_FOUND + id));
        
        orderMapper.updateEntityFromRequest(request, entity);
        
        if (request.getStatus() != null) {
            entity.setStatus(request.getStatus());
        }

        // Handle location update if needed (currently focus on create flow)
        if (request.getDeliveryLocation() != null) {
             com.logistics.hub.feature.location.entity.LocationEntity location = locationService.getOrCreateLocation(request.getDeliveryLocation());
             entity.setDeliveryLocationId(location.getId());
        }
        
        OrderEntity saved = orderRepository.save(entity);
        return orderMapper.toResponse(saved);
    }

    @Override
    public void delete(Long id) {
        if (!orderRepository.existsById(id)) {
            throw new ResourceNotFoundException(OrderConstant.ORDER_NOT_FOUND + id);
        }
        orderRepository.deleteById(id);
    }
}
