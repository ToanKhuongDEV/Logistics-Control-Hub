package com.logistics.hub.feature.redis.constant;

public class CacheConstant {

  private CacheConstant() {
  }

  public static final String OSRM_MATRIX = "osrm:matrix";
  public static final String OSRM_ROUTE = "osrm:route";
  public static final String DASHBOARD_STATS = "dashboard:stats";

  public static final String DEPOTS = "depots";
  public static final String DRIVERS = "drivers";
  public static final String VEHICLES = "vehicles";

  public static final String DEPOT_STATS = "depot:stats";
  public static final String DRIVER_STATS = "driver:stats";
  public static final String VEHICLE_STATS = "vehicle:stats";

  public static final long OSRM_TTL_HOURS = 24;
}
