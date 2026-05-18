package com.logistics.hub.feature.driverportal.service;

import com.logistics.hub.feature.driverportal.dto.response.DriverDeliveryOrderResponse;
import com.logistics.hub.feature.routing.dto.response.RoutingRunResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface DriverPortalService {

    Page<DriverDeliveryOrderResponse> findMyOrders(Pageable pageable);

    DriverDeliveryOrderResponse findMyOrder(Long orderId);

    DriverDeliveryOrderResponse completeMyOrder(Long orderId);

    Page<RoutingRunResponse> findMyRoutingHistory(Pageable pageable);

    RoutingRunResponse findMyRoutingRun(Long runId);

    RoutingRunResponse findMyLatestRoutingRun();
}
