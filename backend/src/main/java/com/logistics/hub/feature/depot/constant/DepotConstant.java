package com.logistics.hub.feature.depot.constant;

public final class DepotConstant {

  private DepotConstant() {
  }

  public static final String DEPOT_NOT_FOUND = "Không tìm thấy kho hàng với id: ";
  public static final String DEPOT_CREATED_SUCCESS = "Tạo kho hàng thành công";
  public static final String DEPOT_UPDATED_SUCCESS = "Cập nhật kho hàng thành công";
  public static final String DEPOT_DELETED_SUCCESS = "Xóa kho hàng thành công";
  public static final String DEPOT_RETRIEVED_SUCCESS = "Lấy dữ liệu kho hàng thành công";
  public static final String DEPOTS_RETRIEVED_SUCCESS = "Lấy danh sách kho hàng thành công";

  public static final String DEPOT_NAME_REQUIRED = "Tên kho hàng là bắt buộc";
  public static final String DEPOT_LOCATION_REQUIRED = "Địa điểm kho hàng là bắt buộc";
  public static final String DEPOT_NAME_LENGTH_EXCEEDED = "Tên kho hàng không được vượt quá 255 ký tự";
  public static final String DEPOT_DESCRIPTION_LENGTH_EXCEEDED = "Mô tả kho hàng không được vượt quá 500 ký tự";
  public static final String DEPOT_LOCATION_EXISTS = "Vị trí này đã được gán cho một kho hàng khác";
  public static final String DEPOT_STATISTICS_RETRIEVED_SUCCESS = "Lấy thống kê kho hàng thành công";
  public static final String DEPOT_HAS_VEHICLES = "Không thể xóa kho vì vẫn còn xe đang trực thuộc. Vui lòng chuyển xe sang kho khác hoặc đóng cửa kho thay vì xóa.";
}
