package com.logistics.hub.feature.dashboard.controller;

import com.logistics.hub.common.base.ApiResponse;
import com.logistics.hub.feature.dashboard.constant.DashboardConstant;
import com.logistics.hub.feature.dashboard.dto.response.DashboardStatisticsResponse;
import com.logistics.hub.feature.dashboard.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Tag(name = "Dashboard", description = "Dashboard statistics API")
public class DashboardController {

  private final DashboardService dashboardService;

  @GetMapping("/statistics")
  @Operation(summary = "Get dashboard statistics", description = "Retrieves comprehensive statistics for the dashboard including vehicles, orders, depots, drivers, and routing runs")
  public ResponseEntity<ApiResponse<DashboardStatisticsResponse>> getStatistics(
      @RequestParam(required = false) Long depotId) {
    DashboardStatisticsResponse statistics = dashboardService.getStatistics(depotId);
    return ResponseEntity.ok(
        ApiResponse.success(200, DashboardConstant.DASHBOARD_STATISTICS_RETRIEVED_SUCCESS, statistics));
  }
}
