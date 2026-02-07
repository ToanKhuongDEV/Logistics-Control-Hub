package com.logistics.hub.feature.routing.constant;

public final class RoutingConstant {

    private RoutingConstant() {
    }

    // ==================== Validation Messages ====================

    public static final String ORDER_IDS_REQUIRED = "Danh sách ID đơn hàng là bắt buộc";
    public static final String ORDER_IDS_EMPTY = "Danh sách ID đơn hàng không được để trống";
    public static final String VEHICLE_IDS_REQUIRED = "Danh sách ID phương tiện là bắt buộc";
    public static final String VEHICLE_IDS_EMPTY = "Danh sách ID phương tiện không được để trống";

    // ==================== Error Messages ====================

    public static final String ROUTING_RUN_NOT_FOUND = "Không tìm thấy phiên tối ưu lộ trình với id: ";
    public static final String ORDERS_NOT_FOUND = "Không có đơn hàng nào ở trạng thái CREATED để tối ưu";
    public static final String VEHICLES_NOT_FOUND = "Không có phương tiện nào ở trạng thái ACTIVE với tài xế được gán để tối ưu";
    public static final String LOCATIONS_NOT_FOUND = "Không tìm thấy đầy đủ các địa điểm";
    public static final String DEPOT_NOT_ASSIGNED = "Không tìm thấy kho hàng với id: ";
    public static final String DEPOT_LOCATION_NOT_FOUND = "Không tìm thấy địa điểm của kho với id kho: ";
    public static final String DELIVERY_LOCATION_NOT_FOUND = "Không tìm thấy địa điểm giao hàng với id: ";
    public static final String MULTIPLE_DEPOTS_ERROR = "Tất cả phương tiện phải thuộc cùng một kho hàng";
    public static final String OPTIMIZATION_FAILED = "Tối ưu hóa lộ trình thất bại - không tìm thấy giải pháp";
    public static final String OR_TOOLS_LOAD_FAILED = "Không thể tải thư viện native OR-Tools";
    public static final String ORDERS_MISMATCH = "Một số đơn hàng không được tìm thấy";
    public static final String VEHICLES_MISMATCH = "Một số phương tiện không được tìm thấy";

    // ==================== Success Messages ====================

    public static final String ROUTING_OPTIMIZATION_SUCCESS = "Tối ưu hóa lộ trình thành công";
    public static final String ROUTING_RUN_RETRIEVED_SUCCESS = "Lấy dữ liệu phiên tối ưu thành công";
    public static final String ROUTING_RUNS_RETRIEVED_SUCCESS = "Lấy danh sách phiên tối ưu thành công";

    // ==================== Configuration Keys ====================

    public static final String CONFIG_OSRM_URL_KEY = "osrm.url";
    public static final String CONFIG_DEFAULT_OSRM_URL = "http://router.project-osrm.org";
}
