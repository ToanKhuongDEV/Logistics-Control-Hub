package com.logistics.hub.feature.driverportal.controller;

import com.logistics.hub.common.base.ApiResponse;
import com.logistics.hub.common.base.PaginatedResponse;
import com.logistics.hub.common.constant.UrlConstant;
import com.logistics.hub.feature.driverportal.dto.response.DriverDeliveryOrderResponse;
import com.logistics.hub.feature.driverportal.service.DriverPortalService;
import com.logistics.hub.feature.routing.dto.response.RoutingRunResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(UrlConstant.DriverPortal.PREFIX)
@RequiredArgsConstructor
@Tag(name = "Driver Portal", description = "APIs for authenticated drivers to manage assigned deliveries")
public class DriverPortalController {

    private final DriverPortalService driverPortalService;

    @GetMapping(UrlConstant.DriverPortal.MY_ORDERS)
    @PreAuthorize("hasAuthority('driver.delivery.read')")
    @Operation(summary = "Get my delivery orders", description = "Returns IN_TRANSIT orders assigned to the current driver")
    public ResponseEntity<ApiResponse<PaginatedResponse<DriverDeliveryOrderResponse>>> findMyOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<DriverDeliveryOrderResponse> orderPage = driverPortalService.findMyOrders(pageable);

        PaginatedResponse<DriverDeliveryOrderResponse> response = new PaginatedResponse<>();
        response.setData(orderPage.getContent());
        response.setPagination(new PaginatedResponse.PaginationInfo(
                orderPage.getNumber(),
                orderPage.getSize(),
                orderPage.getTotalElements(),
                orderPage.getTotalPages()));

        return ResponseEntity.ok(ApiResponse.success("Lay danh sach don can giao thanh cong", response));
    }

    @GetMapping(UrlConstant.DriverPortal.MY_ORDER_BY_ID)
    @PreAuthorize("hasAuthority('driver.delivery.read')")
    @Operation(summary = "Get my delivery order detail", description = "Returns a delivery order assigned to the current driver")
    public ResponseEntity<ApiResponse<DriverDeliveryOrderResponse>> findMyOrder(@PathVariable Long orderId) {
        DriverDeliveryOrderResponse response = driverPortalService.findMyOrder(orderId);
        return ResponseEntity.ok(ApiResponse.success("Lay chi tiet don can giao thanh cong", response));
    }

    @PatchMapping(UrlConstant.DriverPortal.COMPLETE_MY_ORDER)
    @PreAuthorize("hasAuthority('driver.delivery.update')")
    @Operation(summary = "Complete my delivery order", description = "Marks an assigned IN_TRANSIT order as DELIVERED")
    public ResponseEntity<ApiResponse<DriverDeliveryOrderResponse>> completeMyOrder(@PathVariable Long orderId) {
        DriverDeliveryOrderResponse response = driverPortalService.completeMyOrder(orderId);
        return ResponseEntity.ok(ApiResponse.success("Hoan thanh don giao hang thanh cong", response));
    }

    @GetMapping(UrlConstant.DriverPortal.MY_ROUTING_HISTORY)
    @PreAuthorize("hasAuthority('driver.delivery.read')")
    @Operation(summary = "Get my routing history", description = "Returns routing runs containing routes assigned to the current driver")
    public ResponseEntity<ApiResponse<java.util.Map<String, Object>>> getMyRoutingHistory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<RoutingRunResponse> runPage = driverPortalService.findMyRoutingHistory(PageRequest.of(page, size));

        java.util.Map<String, Object> result = java.util.Map.of(
                "content", runPage.getContent(),
                "totalElements", runPage.getTotalElements(),
                "totalPages", runPage.getTotalPages(),
                "currentPage", runPage.getNumber(),
                "pageSize", runPage.getSize());

        return ResponseEntity.ok(ApiResponse.success("Lay lich su tuyen duong cua tai xe thanh cong", result));
    }

    @GetMapping(UrlConstant.DriverPortal.MY_ROUTING_RUN_BY_ID)
    @PreAuthorize("hasAuthority('driver.delivery.read')")
    @Operation(summary = "Get my routing run detail", description = "Returns routing run details scoped to the current driver's routes")
    public ResponseEntity<ApiResponse<RoutingRunResponse>> getMyRoutingRun(@PathVariable Long runId) {
        RoutingRunResponse response = driverPortalService.findMyRoutingRun(runId);
        return ResponseEntity.ok(ApiResponse.success("Lay chi tiet tuyen duong cua tai xe thanh cong", response));
    }

    @GetMapping(UrlConstant.DriverPortal.MY_LATEST_ROUTING_RUN)
    @PreAuthorize("hasAuthority('driver.delivery.read')")
    @Operation(summary = "Get my latest routing run", description = "Returns latest routing run containing routes assigned to the current driver")
    public ResponseEntity<ApiResponse<RoutingRunResponse>> getMyLatestRoutingRun() {
        RoutingRunResponse response = driverPortalService.findMyLatestRoutingRun();
        return ResponseEntity.ok(ApiResponse.success("Lay tuyen duong moi nhat cua tai xe thanh cong", response));
    }
}
