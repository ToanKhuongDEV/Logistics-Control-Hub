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
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

	@GetMapping(UrlConstant.Routing.LATEST_BY_DEPOT)
	@Operation(summary = "Get latest routing run for depot", description = "Retrieves the most recent successful routing run for a specific depot")
	public ResponseEntity<ApiResponse<RoutingRunResponse>> getLatestRoutingRunByDepot(@PathVariable Long depotId) {
		log.info("Fetching latest routing run for depot id: {}", depotId);

		RoutingRunResponse response = routingService.getLatestRunByDepot(depotId)
				.map(RoutingMapper::toRoutingRunResponse)
				.orElse(null);

		return ResponseEntity.ok(ApiResponse.success(RoutingConstant.ROUTING_RUN_RETRIEVED_SUCCESS, response));
	}

	@GetMapping(UrlConstant.Routing.HISTORY_BY_DEPOT)
	@Operation(summary = "Get routing run history for depot", description = "Returns paginated list of all routing runs for a specific depot, newest first")
	public ResponseEntity<ApiResponse<Map<String, Object>>> getHistoryByDepot(
			@PathVariable Long depotId,
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "20") int size) {
		log.info("Fetching routing history for depot id: {}, page: {}, size: {}", depotId, page, size);

		Page<RoutingRunEntity> runPage = routingService.getHistoryByDepot(depotId, page, size);

		List<RoutingRunResponse> content = runPage.getContent().stream()
				.map(RoutingMapper::toRoutingRunResponse)
				.collect(Collectors.toList());

		Map<String, Object> result = Map.of(
				"content", content,
				"totalElements", runPage.getTotalElements(),
				"totalPages", runPage.getTotalPages(),
				"currentPage", runPage.getNumber(),
				"pageSize", runPage.getSize());

		return ResponseEntity.ok(ApiResponse.success("Lấy lịch sử tối ưu thành công", result));
	}
}
