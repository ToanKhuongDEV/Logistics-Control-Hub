package com.logistics.hub.feature.order.service.impl;

import com.logistics.hub.common.exception.EntityNotFoundException;
import com.logistics.hub.common.exception.ValidationException;
import com.logistics.hub.feature.order.constant.OrderConstant;
import com.logistics.hub.feature.order.dto.request.DeliveryOrderRequest;
import com.logistics.hub.feature.order.dto.response.DeliveryOrderResponse;
import com.logistics.hub.feature.order.entity.DeliveryOrderEntity;
import com.logistics.hub.feature.order.repository.DeliveryOrderRepository;
import com.logistics.hub.feature.order.service.DeliveryOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DeliveryOrderServiceImpl implements DeliveryOrderService {

    private final DeliveryOrderRepository deliveryOrderRepository;

    @Override
    @Transactional
    public DeliveryOrderResponse createOrder(DeliveryOrderRequest request) {
        if (deliveryOrderRepository.findByOrderNumber(request.getOrderNumber()).isPresent()) {
            throw new ValidationException(OrderConstant.ORDER_NUMBER_EXISTS + request.getOrderNumber());
        }
        DeliveryOrderEntity entity = mapToEntity(request);
        DeliveryOrderEntity savedEntity = deliveryOrderRepository.save(entity);
        return mapToResponse(savedEntity);
    }

    @Override
    public DeliveryOrderResponse getOrderById(Long id) {
        DeliveryOrderEntity entity = deliveryOrderRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(OrderConstant.ENTITY_NAME, id));
        return mapToResponse(entity);
    }

    @Override
    @Transactional
    public DeliveryOrderResponse updateOrder(Long id, DeliveryOrderRequest request) {
        DeliveryOrderEntity entity = deliveryOrderRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(OrderConstant.ENTITY_NAME, id));

        if (!entity.getOrderNumber().equals(request.getOrderNumber()) &&
                deliveryOrderRepository.findByOrderNumber(request.getOrderNumber()).isPresent()) {
            throw new ValidationException(OrderConstant.ORDER_NUMBER_EXISTS + request.getOrderNumber());
        }

        updateEntity(entity, request);
        DeliveryOrderEntity updatedEntity = deliveryOrderRepository.save(entity);
        return mapToResponse(updatedEntity);
    }

    @Override
    @Transactional
    public void deleteOrder(Long id) {
        if (!deliveryOrderRepository.existsById(id)) {
            throw new EntityNotFoundException(OrderConstant.ENTITY_NAME, id);
        }
        deliveryOrderRepository.deleteById(id);
    }

    @Override
    public Page<DeliveryOrderResponse> getAllOrders(Pageable pageable) {
        return deliveryOrderRepository.findAll(pageable)
                .map(this::mapToResponse);
    }

    private DeliveryOrderEntity mapToEntity(DeliveryOrderRequest request) {
        DeliveryOrderEntity entity = new DeliveryOrderEntity();
        entity.setOrderNumber(request.getOrderNumber());
        entity.setCustomerId(request.getCustomerId());
        entity.setPickupLocationId(request.getPickupLocationId());
        entity.setDeliveryLocationId(request.getDeliveryLocationId());
        entity.setDeliveryTimeWindow(request.getDeliveryTimeWindow());
        entity.setPickupTimeWindow(request.getPickupTimeWindow());
        entity.setWeight(request.getWeight());
        entity.setPriority(request.getPriority());
        entity.setStatus(request.getStatus());
        entity.setNotes(request.getNotes());
        return entity;
    }

    private void updateEntity(DeliveryOrderEntity entity, DeliveryOrderRequest request) {
        entity.setOrderNumber(request.getOrderNumber());
        entity.setCustomerId(request.getCustomerId());
        entity.setPickupLocationId(request.getPickupLocationId());
        entity.setDeliveryLocationId(request.getDeliveryLocationId());
        entity.setDeliveryTimeWindow(request.getDeliveryTimeWindow());
        entity.setPickupTimeWindow(request.getPickupTimeWindow());
        entity.setWeight(request.getWeight());
        entity.setPriority(request.getPriority());
        entity.setStatus(request.getStatus());
        entity.setNotes(request.getNotes());
    }

    private DeliveryOrderResponse mapToResponse(DeliveryOrderEntity entity) {
        DeliveryOrderResponse response = new DeliveryOrderResponse();
        response.setId(entity.getId());
        response.setOrderNumber(entity.getOrderNumber());
        response.setCustomerId(entity.getCustomerId());
        response.setPickupLocationId(entity.getPickupLocationId());
        response.setDeliveryLocationId(entity.getDeliveryLocationId());
        response.setDeliveryTimeWindow(entity.getDeliveryTimeWindow());
        response.setPickupTimeWindow(entity.getPickupTimeWindow());
        response.setWeight(entity.getWeight());
        response.setPriority(entity.getPriority());
        response.setStatus(entity.getStatus());
        response.setNotes(entity.getNotes());
        response.setCreatedAt(entity.getCreatedAt());
        response.setUpdatedAt(entity.getUpdatedAt());
        return response;
    }
}
