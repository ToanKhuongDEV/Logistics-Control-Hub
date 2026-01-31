package com.logistics.hub.feature.order.service.impl;

import com.logistics.hub.common.exception.ResourceNotFoundException;
import com.logistics.hub.feature.location.dto.response.LocationResponse;
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

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;


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
        
        Page<Map<String, Object>> pageResult = orderRepository.findAllWithLocationAndFilters(statusStr, searchStr, pageable);
        
        return pageResult.map(this::buildOrderResponseFromMap);
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
        Map<String, Object> resultMap = orderRepository.findByIdWithLocationData(id);
        if (resultMap == null || resultMap.isEmpty()) {
            throw new ResourceNotFoundException(OrderConstant.ORDER_NOT_FOUND + id);
        }
        
        return buildOrderResponseFromMap(resultMap);
    }
    
    private OrderResponse buildOrderResponseFromMap(Map<String, Object> map) {
        OrderResponse response = new OrderResponse();
        response.setId(((Number) map.get("id")).longValue());
        response.setCode((String) map.get("code"));
        response.setDeliveryLocationId(((Number) map.get("delivery_location_id")).longValue());
        
        if (map.get("weight_kg") != null) {
            response.setWeightKg(((Number) map.get("weight_kg")).intValue());
        }
        if (map.get("volume_m3") != null) {
            response.setVolumeM3(new BigDecimal(map.get("volume_m3").toString()));
        }
        response.setStatus(OrderStatus.valueOf((String) map.get("status")));
        response.setCreatedAt(((Timestamp) map.get("created_at")).toInstant());
        
        // Build location if joined
        if (map.get("loc_id") != null) {
            LocationResponse location = new LocationResponse();
            location.setId(((Number) map.get("loc_id")).longValue());
            location.setName((String) map.get("loc_name"));
            location.setLatitude(((Number) map.get("loc_lat")).doubleValue());
            location.setLongitude(((Number) map.get("loc_lng")).doubleValue());
            response.setDeliveryLocation(location);
        }
        
        return response;
    }
    
    @Override
    public OrderResponse create(OrderRequest request) {
        if (orderRepository.existsByCode(request.getCode())) {
            throw new IllegalArgumentException(OrderConstant.ORDER_CODE_EXISTS + request.getCode());
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
            throw new IllegalArgumentException(OrderConstant.ORDER_CODE_EXISTS + request.getCode());
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
}
