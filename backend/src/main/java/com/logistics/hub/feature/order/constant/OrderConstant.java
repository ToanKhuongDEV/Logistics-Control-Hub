package com.logistics.hub.feature.order.constant;

public final class OrderConstant {

    private OrderConstant() {
    }

    public static final String ORDER_CODE_REQUIRED = "Mã đơn hàng là bắt buộc";
    public static final String DELIVERY_LOCATION_REQUIRED = "Địa điểm giao hàng là bắt buộc";
    public static final String DELIVERY_LOCATION_DETAILS_REQUIRED = "Chi tiết địa điểm giao hàng là bắt buộc";
    public static final String ORDER_NOT_FOUND = "Không tìm thấy đơn hàng với id: ";
    public static final String ORDER_CODE_EXISTS = "Mã đơn hàng đã tồn tại: ";

    public static final String ORDER_RETRIEVED_SUCCESS = "Lấy dữ liệu đơn hàng thành công";
    public static final String ORDERS_RETRIEVED_SUCCESS = "Lấy danh sách đơn hàng thành công";
    public static final String ORDER_CREATED_SUCCESS = "Tạo đơn hàng thành công";
    public static final String ORDER_UPDATED_SUCCESS = "Cập nhật đơn hàng thành công";
    public static final String ORDER_DELETED_SUCCESS = "Xóa đơn hàng thành công";
    public static final String ORDER_STATISTICS_RETRIEVED_SUCCESS = "Lấy thống kê đơn hàng thành công";
}
