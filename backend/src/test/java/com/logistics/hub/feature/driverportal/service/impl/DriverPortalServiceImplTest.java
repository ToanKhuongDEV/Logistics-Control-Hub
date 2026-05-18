package com.logistics.hub.feature.driverportal.service.impl;

import com.logistics.hub.common.exception.ResourceNotFoundException;
import com.logistics.hub.common.exception.ValidationException;
import com.logistics.hub.feature.audit.service.AuditActorService;
import com.logistics.hub.feature.audit.service.AuditLogService;
import com.logistics.hub.feature.auth.policy.AuthorizationPolicy;
import com.logistics.hub.feature.auth.service.AuthorizationService;
import com.logistics.hub.feature.depot.entity.DepotEntity;
import com.logistics.hub.feature.driver.entity.DriverEntity;
import com.logistics.hub.feature.driverportal.dto.response.DriverDeliveryOrderResponse;
import com.logistics.hub.feature.location.entity.LocationEntity;
import com.logistics.hub.feature.order.entity.OrderEntity;
import com.logistics.hub.feature.order.enums.OrderStatus;
import com.logistics.hub.feature.order.repository.OrderRepository;
import com.logistics.hub.feature.routing.entity.RouteEntity;
import com.logistics.hub.feature.routing.entity.RouteStopEntity;
import com.logistics.hub.feature.routing.entity.RoutingRunEntity;
import com.logistics.hub.feature.routing.dto.response.RoutingRunResponse;
import com.logistics.hub.feature.routing.enums.RouteStatus;
import com.logistics.hub.feature.routing.enums.RoutingRunStatus;
import com.logistics.hub.feature.routing.repository.RouteRepository;
import com.logistics.hub.feature.routing.repository.RouteStopRepository;
import com.logistics.hub.feature.routing.repository.RoutingRunRepository;
import com.logistics.hub.feature.user.entity.UserEntity;
import com.logistics.hub.feature.vehicle.entity.VehicleEntity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DriverPortalServiceImplTest {

    @Mock
    private AuthorizationService authorizationService;

    @Mock
    private RouteStopRepository routeStopRepository;

    @Mock
    private RouteRepository routeRepository;

    @Mock
    private RoutingRunRepository routingRunRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private AuditLogService auditLogService;

    @Mock
    private AuditActorService auditActorService;

    @InjectMocks
    private DriverPortalServiceImpl driverPortalService;

    @Test
    void findMyOrders_shouldReturnOnlyCurrentDriverInTransitOrders() {
        DriverEntity driver = driver(7L);
        UserEntity user = driverUser(driver);
        RouteStopEntity stop = stop(100L, route(3L, RouteStatus.CREATED), order(11L, driver, OrderStatus.IN_TRANSIT));
        Pageable pageable = PageRequest.of(0, 10);

        doNothing().when(authorizationService)
                .requirePermission(AuthorizationPolicy.PERMISSION_DRIVER_DELIVERY_READ);
        when(authorizationService.getCurrentUser()).thenReturn(user);
        when(routeStopRepository.findDeliveryStopsByDriverIdAndOrderStatus(7L, OrderStatus.IN_TRANSIT, pageable))
                .thenReturn(new PageImpl<>(List.of(stop), pageable, 1));

        Page<DriverDeliveryOrderResponse> response = driverPortalService.findMyOrders(pageable);

        assertEquals(1, response.getContent().size());
        assertEquals(11L, response.getContent().get(0).getId());
        assertEquals(3L, response.getContent().get(0).getRouteId());
    }

    @Test
    void completeMyOrder_shouldRejectOrderOutsideCurrentDriver() {
        DriverEntity driver = driver(7L);

        doNothing().when(authorizationService)
                .requirePermission(AuthorizationPolicy.PERMISSION_DRIVER_DELIVERY_UPDATE);
        when(authorizationService.getCurrentUser()).thenReturn(driverUser(driver));
        when(routeStopRepository.findDeliveryStopsByDriverIdAndOrderId(eq(7L), eq(11L), any(Pageable.class)))
                .thenReturn(Page.empty());

        assertThrows(ResourceNotFoundException.class, () -> driverPortalService.completeMyOrder(11L));

        verify(orderRepository, never()).save(any(OrderEntity.class));
    }

    @Test
    void completeMyOrder_shouldRejectOrderThatIsNotInTransit() {
        DriverEntity driver = driver(7L);
        RouteStopEntity stop = stop(100L, route(3L, RouteStatus.CREATED), order(11L, driver, OrderStatus.DELIVERED));

        doNothing().when(authorizationService)
                .requirePermission(AuthorizationPolicy.PERMISSION_DRIVER_DELIVERY_UPDATE);
        when(authorizationService.getCurrentUser()).thenReturn(driverUser(driver));
        when(routeStopRepository.findDeliveryStopsByDriverIdAndOrderId(eq(7L), eq(11L), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(stop)));

        ValidationException ex = assertThrows(ValidationException.class,
                () -> driverPortalService.completeMyOrder(11L));

        assertEquals("Only IN_TRANSIT orders can be completed by the driver.", ex.getMessage());
        verify(orderRepository, never()).save(any(OrderEntity.class));
    }

    @Test
    void completeMyOrder_shouldDeliverOrderAndCompleteRouteWhenAllStopsAreDelivered() {
        DriverEntity driver = driver(7L);
        RouteEntity route = route(3L, RouteStatus.CREATED);
        OrderEntity order = order(11L, driver, OrderStatus.IN_TRANSIT);
        RouteStopEntity stop = stop(100L, route, order);

        doNothing().when(authorizationService)
                .requirePermission(AuthorizationPolicy.PERMISSION_DRIVER_DELIVERY_UPDATE);
        when(authorizationService.getCurrentUser()).thenReturn(driverUser(driver));
        when(routeStopRepository.findDeliveryStopsByDriverIdAndOrderId(eq(7L), eq(11L), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(stop)));
        when(routeStopRepository.findOrderStopsByRouteIdWithOrders(3L)).thenReturn(List.of(stop));

        DriverDeliveryOrderResponse response = driverPortalService.completeMyOrder(11L);

        assertEquals(OrderStatus.DELIVERED, order.getStatus());
        assertEquals(RouteStatus.COMPLETED, route.getStatus());
        assertEquals(OrderStatus.DELIVERED, response.getStatus());
        verify(orderRepository).save(order);
        verify(routeRepository).save(route);
    }

    @Test
    void completeMyOrder_shouldKeepRouteInProgressWhenOtherStopsRemainInTransit() {
        DriverEntity driver = driver(7L);
        RouteEntity route = route(3L, RouteStatus.CREATED);
        OrderEntity completedOrder = order(11L, driver, OrderStatus.IN_TRANSIT);
        RouteStopEntity completedStop = stop(100L, route, completedOrder);
        RouteStopEntity remainingStop = stop(101L, route, order(12L, driver, OrderStatus.IN_TRANSIT));

        doNothing().when(authorizationService)
                .requirePermission(AuthorizationPolicy.PERMISSION_DRIVER_DELIVERY_UPDATE);
        when(authorizationService.getCurrentUser()).thenReturn(driverUser(driver));
        when(routeStopRepository.findDeliveryStopsByDriverIdAndOrderId(eq(7L), eq(11L), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(completedStop)));
        when(routeStopRepository.findOrderStopsByRouteIdWithOrders(3L)).thenReturn(List.of(completedStop, remainingStop));

        driverPortalService.completeMyOrder(11L);

        assertEquals(OrderStatus.DELIVERED, completedOrder.getStatus());
        assertEquals(RouteStatus.IN_PROGRESS, route.getStatus());
        verify(routeRepository).save(route);
    }

    @Test
    void findMyRoutingHistory_shouldReturnDriverScopedRunsWithOnlyDriverRoutes() {
        DriverEntity driver = driver(7L);
        RoutingRunEntity run = routingRun(30L);
        RouteEntity route = route(3L, RouteStatus.CREATED, driver);
        route.setStops(List.of(stop(100L, route, order(11L, driver, OrderStatus.IN_TRANSIT))));
        Pageable pageable = PageRequest.of(0, 20);

        doNothing().when(authorizationService)
                .requirePermission(AuthorizationPolicy.PERMISSION_DRIVER_DELIVERY_READ);
        when(authorizationService.getCurrentUser()).thenReturn(driverUser(driver));
        when(routingRunRepository.findAllByDriverId(7L, pageable))
                .thenReturn(new PageImpl<>(List.of(run), pageable, 1));
        when(routeRepository.findAllByRoutingRunIdAndDriverId(30L, 7L)).thenReturn(List.of(route));

        Page<RoutingRunResponse> response = driverPortalService.findMyRoutingHistory(pageable);

        assertEquals(1, response.getContent().size());
        assertEquals(30L, response.getContent().get(0).getId());
        assertEquals(1, response.getContent().get(0).getRoutes().size());
        assertEquals(3L, response.getContent().get(0).getRoutes().get(0).getId());
    }

    @Test
    void findMyRoutingRun_shouldRejectRunWithoutCurrentDriverRoute() {
        DriverEntity driver = driver(7L);
        RoutingRunEntity run = routingRun(30L);

        doNothing().when(authorizationService)
                .requirePermission(AuthorizationPolicy.PERMISSION_DRIVER_DELIVERY_READ);
        when(authorizationService.getCurrentUser()).thenReturn(driverUser(driver));
        when(routingRunRepository.findById(30L)).thenReturn(java.util.Optional.of(run));
        when(routeRepository.findAllByRoutingRunIdAndDriverId(30L, 7L)).thenReturn(List.of());

        assertThrows(ResourceNotFoundException.class, () -> driverPortalService.findMyRoutingRun(30L));
    }

    private UserEntity driverUser(DriverEntity driver) {
        UserEntity user = new UserEntity();
        user.setId(1L);
        user.setUsername("driver01");
        user.setRole("DRIVER");
        user.setDriver(driver);
        user.setAssignedDepots(new java.util.ArrayList<>());
        return user;
    }

    private DriverEntity driver(Long id) {
        DriverEntity driver = new DriverEntity();
        driver.setId(id);
        driver.setName("Driver " + id);
        driver.setLicenseNumber("LIC-" + id);
        driver.setPhoneNumber("0900" + id);
        return driver;
    }

    private RouteEntity route(Long id, RouteStatus status) {
        RouteEntity route = new RouteEntity();
        route.setId(id);
        route.setStatus(status);
        return route;
    }

    private RouteEntity route(Long id, RouteStatus status, DriverEntity driver) {
        RouteEntity route = route(id, status);
        VehicleEntity vehicle = new VehicleEntity();
        vehicle.setId(50L);
        vehicle.setDriver(driver);
        route.setVehicle(vehicle);
        return route;
    }

    private RoutingRunEntity routingRun(Long id) {
        RoutingRunEntity run = new RoutingRunEntity();
        run.setId(id);
        run.setStatus(RoutingRunStatus.COMPLETED);
        return run;
    }

    private OrderEntity order(Long id, DriverEntity driver, OrderStatus status) {
        DepotEntity depot = new DepotEntity();
        depot.setId(9L);
        depot.setName("Depot");

        LocationEntity location = new LocationEntity();
        location.setId(4L);
        location.setStreet("Street");
        location.setCity("City");
        location.setCountry("Viet Nam");
        location.setLatitude(10.0);
        location.setLongitude(106.0);

        OrderEntity order = new OrderEntity();
        order.setId(id);
        order.setCode("ORD-" + id);
        order.setDriver(driver);
        order.setStatus(status);
        order.setDepot(depot);
        order.setDeliveryLocation(location);
        return order;
    }

    private RouteStopEntity stop(Long id, RouteEntity route, OrderEntity order) {
        RouteStopEntity stop = new RouteStopEntity();
        stop.setId(id);
        stop.setRoute(route);
        stop.setOrder(order);
        stop.setLocation(order.getDeliveryLocation());
        stop.setStopSequence(1);
        return stop;
    }
}
