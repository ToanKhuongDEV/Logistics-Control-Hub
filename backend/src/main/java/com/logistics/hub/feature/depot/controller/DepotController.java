package com.logistics.hub.feature.depot.controller;

import com.logistics.hub.common.base.ApiResponse;
import com.logistics.hub.common.base.PaginatedResponse;
import com.logistics.hub.common.constant.UrlConstant;
import com.logistics.hub.feature.depot.constant.DepotConstant;
import com.logistics.hub.feature.depot.dto.request.DepotRequest;
import com.logistics.hub.feature.depot.dto.response.DepotResponse;
import com.logistics.hub.feature.depot.dto.response.DepotStatisticsResponse;
import com.logistics.hub.feature.depot.service.DepotService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(UrlConstant.Depot.PREFIX)
@RequiredArgsConstructor
@Tag(name = "Depot", description = "APIs for managing distribution centers (depots)")
public class DepotController {

  private final DepotService depotService;

  @GetMapping
  @Operation(summary = "Get all depots", description = "Returns a paginated list of depots")
  public ResponseEntity<ApiResponse<PaginatedResponse<DepotResponse>>> findAll(
      @RequestParam(required = false) String search,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size) {
    Pageable pageable = PageRequest.of(page, size);
    Page<DepotResponse> depotPage = depotService.findAll(search, pageable);

    PaginatedResponse<DepotResponse> response = new PaginatedResponse<>();
    response.setData(depotPage.getContent());
    response.setPagination(new PaginatedResponse.PaginationInfo(
        depotPage.getNumber(),
        depotPage.getSize(),
        depotPage.getTotalElements(),
        depotPage.getTotalPages()));

    return ResponseEntity.ok(ApiResponse.success(DepotConstant.DEPOTS_RETRIEVED_SUCCESS, response));
  }

  @GetMapping(UrlConstant.Depot.BY_ID)
  @Operation(summary = "Get depot by ID", description = "Returns detailed information of a single depot")
  public ResponseEntity<ApiResponse<?>> findById(@PathVariable Long id) {
    DepotResponse depot = depotService.findById(id);
    return ResponseEntity.ok(ApiResponse.success(DepotConstant.DEPOT_RETRIEVED_SUCCESS, depot));
  }

  @PostMapping
  @Operation(summary = "Create new depot", description = "Creates a new depot and automatically geocodes its address")
  public ResponseEntity<ApiResponse<?>> create(@Valid @RequestBody DepotRequest request) {
    DepotResponse createdDepot = depotService.create(request);
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ApiResponse.success(201, DepotConstant.DEPOT_CREATED_SUCCESS, createdDepot));
  }

  @PutMapping(UrlConstant.Depot.BY_ID)
  @Operation(summary = "Update depot", description = "Updates an existing depot and re-geocodes its address if changed")
  public ResponseEntity<ApiResponse<?>> update(@PathVariable Long id, @Valid @RequestBody DepotRequest request) {
    DepotResponse updatedDepot = depotService.update(id, request);
    return ResponseEntity.ok(ApiResponse.success(DepotConstant.DEPOT_UPDATED_SUCCESS, updatedDepot));
  }

  @DeleteMapping(UrlConstant.Depot.BY_ID)
  @Operation(summary = "Delete depot", description = "Permanently removes a depot by its ID")
  public ResponseEntity<ApiResponse<?>> delete(@PathVariable Long id) {
    depotService.delete(id);
    return ResponseEntity.ok(ApiResponse.success(DepotConstant.DEPOT_DELETED_SUCCESS, null));
  }

  @GetMapping(UrlConstant.Depot.STATISTICS)
  @Operation(summary = "Get depot statistics", description = "Returns statistics about all depots")
  public ResponseEntity<ApiResponse<DepotStatisticsResponse>> getStatistics() {
    DepotStatisticsResponse statistics = depotService.getStatistics();
    return ResponseEntity.ok(ApiResponse.success(DepotConstant.DEPOT_STATISTICS_RETRIEVED_SUCCESS, statistics));
  }
}
