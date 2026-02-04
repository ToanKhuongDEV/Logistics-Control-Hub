package com.logistics.hub.feature.routing.controller;

import com.logistics.hub.common.base.ApiResponse;
import com.logistics.hub.common.constant.UrlConstant;
import com.logistics.hub.feature.routing.constant.RoutingConstant;
import com.logistics.hub.feature.routing.dto.request.OptimizeRoutingRequest;
import com.logistics.hub.feature.routing.dto.response.RoutingRunResponse;
import com.logistics.hub.feature.routing.entity.RoutingRunEntity;
import com.logistics.hub.feature.routing.mapper.RoutingMapper;
import com.logistics.hub.feature.routing.repository.RoutingRunRepository;
import com.logistics.hub.feature.routing.service.RoutingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller cho Routing feature
 * Cung cấp endpoints để thực hiện tối ưu hóa routes và query kết quả
 */
@RestController
@RequestMapping(UrlConstant.Routing.PREFIX)
@RequiredArgsConstructor
@Slf4j
public class RoutingController {

	private final RoutingService routingService;
	private final RoutingRunRepository routingRunRepository;

	/**
	 * Endpoint chính để tối ưu hóa routes
	 * Nhận danh sách orders và vehicles, trả về routes đã được tối ưu hóa
	 * 
	 * POST /api/v1/routing/optimize
	 * 
	 * Request body:
	 * {
	 * "orderIds": [1, 2, 3, 4],
	 * "vehicleIds": [1, 2]
	 * }
	 * 
	 * @param request OptimizeRoutingRequest chứa orderIds và vehicleIds
	 * @return ApiResponse chứa RoutingRunResponse với đầy đủ routes và stops
	 */
	@PostMapping(UrlConstant.Routing.OPTIMIZE)
	public ResponseEntity<ApiResponse<RoutingRunResponse>> optimizeRouting(
			@Valid @RequestBody OptimizeRoutingRequest request) {

		// Gọi service để execute routing optimization
		RoutingRunEntity runEntity = routingService.executeRouting(
				request.getOrderIds(),
				request.getVehicleIds());

		// Convert entity sang response DTO
		RoutingRunResponse response = RoutingMapper.toRoutingRunResponse(runEntity);

		return ResponseEntity.status(HttpStatus.CREATED)
				.body(ApiResponse.success(201, RoutingConstant.ROUTING_OPTIMIZATION_SUCCESS, response));
	}

	/**
	 * Lấy thông tin chi tiết của một routing run theo ID
	 * 
	 * GET /api/v1/routing/runs/{id}
	 * 
	 * @param id ID của routing run cần lấy
	 * @return ApiResponse chứa RoutingRunResponse
	 */
	@GetMapping(UrlConstant.Routing.RUN_BY_ID)
	public ResponseEntity<ApiResponse<RoutingRunResponse>> getRoutingRunById(@PathVariable Long id) {
		log.info("Fetching routing run with id: {}", id);

		// Fetch từ database
		RoutingRunEntity runEntity = routingRunRepository.findById(id)
				.orElseThrow(() -> new RuntimeException(RoutingConstant.ROUTING_RUN_NOT_FOUND + id));

		// Convert entity sang response DTO
		RoutingRunResponse response = RoutingMapper.toRoutingRunResponse(runEntity);

		return ResponseEntity.ok(ApiResponse.success(RoutingConstant.ROUTING_RUN_RETRIEVED_SUCCESS, response));
	}
}
