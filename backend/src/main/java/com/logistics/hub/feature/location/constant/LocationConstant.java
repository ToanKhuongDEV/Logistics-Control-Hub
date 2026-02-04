package com.logistics.hub.feature.location.constant;

public final class LocationConstant {

    private LocationConstant() {
    }

    public static final String LOCATION_NAME_REQUIRED = "Tên địa điểm/địa chỉ là bắt buộc";
    public static final String LATITUDE_RANGE = "Vĩ độ phải nằm trong khoảng từ -90 đến 90";
    public static final String LONGITUDE_RANGE = "Kinh độ phải nằm trong khoảng từ -180 đến 180";
    public static final String LATITUDE_REQUIRED = "Vĩ độ là bắt buộc";
    public static final String LONGITUDE_REQUIRED = "Kinh độ là bắt buộc";
    public static final String STREET_REQUIRED = "Tên đường là bắt buộc";
    public static final String CITY_REQUIRED = "Thành phố là bắt buộc";
    public static final String COUNTRY_REQUIRED = "Quốc gia là bắt buộc";

    public static final String LOCATION_CREATED_SUCCESS = "Tạo địa điểm thành công";
    public static final String LOCATION_DELETED_SUCCESS = "Xóa địa điểm thành công";

    public static final String GEOCODE_ERROR = "Không thể lấy tọa độ địa chỉ: ";
    public static final String LOCATION_NOT_FOUND = "Không tìm thấy địa điểm với id: ";
    public static final String LOCATION_RETRIEVAL_ERROR = "Địa điểm tồn tại nhưng không thể truy xuất";
}
