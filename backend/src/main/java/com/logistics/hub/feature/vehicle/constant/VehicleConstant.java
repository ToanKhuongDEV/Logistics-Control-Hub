package com.logistics.hub.feature.vehicle.constant;

public final class VehicleConstant {

    private VehicleConstant() {
    }

    public static final String VEHICLE_CODE_REQUIRED = "Mã phương tiện là bắt buộc";
    public static final String VEHICLE_STATUS_REQUIRED = "Trạng thái phương tiện là bắt buộc";
    public static final String VEHICLE_CODE_EXISTS = "Mã phương tiện đã tồn tại: ";
    public static final String VEHICLE_NOT_FOUND = "Không tìm thấy phương tiện với id: ";
    public static final String VEHICLE_IN_USE = "Phương tiện đang được gán cho các tuyến hoạt động và không thể xóa";
    public static final String DRIVER_ALREADY_ASSIGNED = "Tài xế đã được gán cho một phương tiện khác";
    public static final String VEHICLE_DEPOT_REQUIRED = "Kho hàng của phương tiện là bắt buộc";

    public static final String VEHICLE_RETRIEVED_SUCCESS = "Lấy dữ liệu phương tiện thành công";
    public static final String VEHICLES_RETRIEVED_SUCCESS = "Lấy danh sách phương tiện thành công";
    public static final String VEHICLE_CREATED_SUCCESS = "Tạo phương tiện thành công";
    public static final String VEHICLE_UPDATED_SUCCESS = "Cập nhật phương tiện thành công";
    public static final String VEHICLE_DELETED_SUCCESS = "Xóa phương tiện thành công";
    public static final String VEHICLE_STATISTICS_RETRIEVED_SUCCESS = "Lấy thống kê phương tiện thành công";

    public static final String MAX_WEIGHT_POSITIVE = "Trọng tải tối đa phải là số dương";
    public static final String MAX_VOLUME_POSITIVE = "Thể tích tối đa phải là số dương";
    public static final String COST_PER_KM_POSITIVE = "Chi phí mỗi km phải là số dương";

    public static final String VEHICLE_CODE_LENGTH_EXCEEDED = "Mã phương tiện không được vượt quá 50 ký tự";
    public static final String VEHICLE_TYPE_REQUIRED = "Loại phương tiện (nhãn hiệu/mẫu xe) là bắt buộc";
    public static final String VEHICLE_TYPE_LENGTH_EXCEEDED = "Loại phương tiện không được vượt quá 100 ký tự";
}
