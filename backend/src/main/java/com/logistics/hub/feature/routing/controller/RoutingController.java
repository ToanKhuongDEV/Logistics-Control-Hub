package com.logistics.hub.feature.routing.controller;

import com.logistics.hub.common.base.ApiResponse;
import com.logistics.hub.common.constant.UrlConstant;
import com.logistics.hub.feature.routing.constant.RoutingConstant;
import com.logistics.hub.feature.routing.dto.response.RoutingRunResponse;
import com.logistics.hub.feature.routing.entity.RoutingRunEntity;
import com.logistics.hub.feature.routing.mapper.RoutingMapper;
import com.logistics.hub.feature.routing.repository.RoutingRunRepository;
import com.logistics.hub.feature.routing.service.RoutingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(UrlConstant.Routing.PREFIX)
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Routing", description = "Endpoints for route optimization and run management")
public class RoutingController {

	private final RoutingService routingService;
	private final RoutingRunRepository routingRunRepository;

	@PostMapping(UrlConstant.Routing.OPTIMIZE)
	@Operation(summary = "Optimize routes", description = "Automatically optimizes delivery routes for all CREATED orders using ACTIVE vehicles with assigned drivers for the given depot")
	public ResponseEntity<ApiResponse<RoutingRunResponse>> optimizeRouting(@RequestParam Long depotId) {
		RoutingRunEntity runEntity = routingService.executeAutoRouting(depotId);

		RoutingRunResponse response = RoutingMapper.toRoutingRunResponse(runEntity);

		return ResponseEntity.status(HttpStatus.CREATED)
				.body(ApiResponse.success(201, RoutingConstant.ROUTING_OPTIMIZATION_SUCCESS, response));
	}

	@GetMapping(UrlConstant.Routing.RUN_BY_ID)
	@Operation(summary = "Get routing run info", description = "Retrieves details of a specific routing run including its routes and stops")
	public ResponseEntity<ApiResponse<RoutingRunResponse>> getRoutingRunById(@PathVariable Long id) {
		log.info("Fetching routing run with id: {}", id);

		RoutingRunEntity runEntity = routingRunRepository.findById(id)
				.orElseThrow(() -> new com.logistics.hub.common.exception.ResourceNotFoundException(
						RoutingConstant.ROUTING_RUN_NOT_FOUND + id));

		RoutingRunResponse response = RoutingMapper.toRoutingRunResponse(runEntity);

		return ResponseEntity.ok(ApiResponse.success(RoutingConstant.ROUTING_RUN_RETRIEVED_SUCCESS, response));
	}
}
