package com.logistics.hub.feature.order.service.impl;

import com.logistics.hub.common.exception.ResourceNotFoundException;
import com.logistics.hub.common.exception.ValidationException;
import com.logistics.hub.feature.location.entity.LocationEntity;
import com.logistics.hub.feature.location.service.LocationService;
import com.logistics.hub.feature.order.constant.OrderConstant;
import com.logistics.hub.feature.order.dto.request.OrderRequest;
import com.logistics.hub.feature.order.dto.response.OrderResponse;
import com.logistics.hub.feature.order.dto.response.OrderStatisticsResponse;
import com.logistics.hub.feature.order.entity.OrderEntity;
import com.logistics.hub.feature.order.enums.OrderStatus;
import com.logistics.hub.feature.order.mapper.OrderMapper;
import com.logistics.hub.feature.order.repository.OrderRepository;
import com.logistics.hub.feature.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
@RequiredArgsConstructor
@Transactional
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final LocationService locationService;

    @Override
    @Transactional(readOnly = true)
    public Page<OrderResponse> findAll(Pageable pageable, OrderStatus status, String search) {
        String statusStr = status != null ? status.name() : null;
        String searchStr = (search != null && !search.isEmpty()) ? search : null;
        
        return orderRepository.findAllWithLocationAndFilters(statusStr, searchStr, pageable)
                .map(OrderResponse::fromProjection);
    }
    
    @Override
    public OrderStatisticsResponse getStatistics() {
        List<OrderEntity> allOrders = orderRepository.findAll();
        
        long activeCount = allOrders.stream()
                .filter(o -> o.getStatus() != OrderStatus.DELIVERED && o.getStatus() != OrderStatus.CANCELLED)
                .count();
        
        long pending = allOrders.stream()
                .filter(o -> o.getStatus() == OrderStatus.CREATED)
                .count();
        
        long inTransit = allOrders.stream()
                .filter(o -> o.getStatus() == OrderStatus.IN_TRANSIT)
                .count();

        return OrderStatisticsResponse.builder()
                .total(activeCount)
                .pending(pending)
                .inTransit(inTransit)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse findById(Long id) {
        return orderRepository.findByIdWithLocation(id)
                .map(OrderResponse::fromProjection)
                .orElseThrow(() -> new ResourceNotFoundException(OrderConstant.ORDER_NOT_FOUND + id));
    }

    
    @Override
    public OrderResponse create(OrderRequest request) {
        if (request.getCode() == null || request.getCode().trim().isEmpty()) {
            request.setCode(generateOrderCode());
        } else if (orderRepository.existsByCode(request.getCode())) {
            throw new ValidationException(OrderConstant.ORDER_CODE_EXISTS + request.getCode());
        }

        OrderEntity entity = orderMapper.toEntity(request);
        entity.setStatus(OrderStatus.CREATED);
        
        LocationEntity location = locationService.getOrCreateLocation(request.getDeliveryLocation());
        entity.setDeliveryLocationId(location.getId());
        
        OrderEntity saved = orderRepository.save(entity);
        return findById(saved.getId());
    }

    @Override
    public OrderResponse update(Long id, OrderRequest request) {
        OrderEntity entity = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(OrderConstant.ORDER_NOT_FOUND + id));
        
        if (!entity.getCode().equals(request.getCode()) && orderRepository.existsByCode(request.getCode())) {
            throw new ValidationException(OrderConstant.ORDER_CODE_EXISTS + request.getCode());
        }

        orderMapper.updateEntityFromRequest(request, entity);
        
        if (request.getStatus() != null) {
            entity.setStatus(request.getStatus());
        }

        if (request.getDeliveryLocation() != null) {
            LocationEntity location = locationService.getOrCreateLocation(request.getDeliveryLocation());
            entity.setDeliveryLocationId(location.getId());
        }
        
        OrderEntity saved = orderRepository.save(entity);
        return findById(saved.getId());
    }

    @Override
    public void delete(Long id) {
        if (!orderRepository.existsById(id)) {
            throw new ResourceNotFoundException(OrderConstant.ORDER_NOT_FOUND + id);
        }
        orderRepository.deleteById(id);
    }

    private String generateOrderCode() {
        return orderRepository.findTopByOrderByIdDesc()
            .map(lastOrder -> {
                String lastCode = lastOrder.getCode();
                if (lastCode.startsWith("ORD-")) {
                    try {
                        int sequence = Integer.parseInt(lastCode.substring(4));
                        return String.format("ORD-%03d", sequence + 1);
                    } catch (NumberFormatException e) {
                        return "ORD-001";
                    }
                }
                return "ORD-001";
            })
            .orElse("ORD-001");
    }
}
