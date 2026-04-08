package com.logistics.hub.feature.vehicle.constant;

public final class VehicleConstant {

    private VehicleConstant() {
    }

    public static final String VEHICLE_CODE_REQUIRED = "Ma phuong tien la bat buoc";
    public static final String VEHICLE_STATUS_REQUIRED = "Trang thai phuong tien la bat buoc";
    public static final String VEHICLE_CODE_EXISTS = "Ma phuong tien da ton tai: ";
    public static final String VEHICLE_NOT_FOUND = "Khong tim thay phuong tien voi id: ";
    public static final String VEHICLE_IN_USE = "Phuong tien dang duoc gan cho cac tuyen hoat dong va khong the xoa";
    public static final String DRIVER_ALREADY_ASSIGNED = "Tai xe da duoc gan cho mot phuong tien khac";
    public static final String VEHICLE_DEPOT_REQUIRED = "Kho hang cua phuong tien la bat buoc";
    public static final String VEHICLE_IDS_REQUIRED = "Danh sach phuong tien la bat buoc";
    public static final String VEHICLE_DEPOT_ID_REQUIRED = "Kho dich la bat buoc";
    public static final String VEHICLE_IDS_NOT_FOUND = "Khong tim thay phuong tien voi id: ";

    public static final String VEHICLE_RETRIEVED_SUCCESS = "Lay du lieu phuong tien thanh cong";
    public static final String VEHICLES_RETRIEVED_SUCCESS = "Lay danh sach phuong tien thanh cong";
    public static final String VEHICLE_CREATED_SUCCESS = "Tao phuong tien thanh cong";
    public static final String VEHICLE_UPDATED_SUCCESS = "Cap nhat phuong tien thanh cong";
    public static final String VEHICLE_DELETED_SUCCESS = "Xoa phuong tien thanh cong";
    public static final String VEHICLE_STATISTICS_RETRIEVED_SUCCESS = "Lay thong ke phuong tien thanh cong";
    public static final String VEHICLES_DEPOT_UPDATED_SUCCESS = "Cap nhat kho truc thuoc hang loat thanh cong";

    public static final String MAX_WEIGHT_POSITIVE = "Trong tai toi da phai la so duong";
    public static final String MAX_VOLUME_POSITIVE = "The tich toi da phai la so duong";
    public static final String COST_PER_KM_POSITIVE = "Chi phi moi km phai la so duong";

    public static final String VEHICLE_CODE_LENGTH_EXCEEDED = "Ma phuong tien khong duoc vuot qua 50 ky tu";
    public static final String VEHICLE_TYPE_REQUIRED = "Loai phuong tien (nhan hieu/mau xe) la bat buoc";
    public static final String VEHICLE_TYPE_LENGTH_EXCEEDED = "Loai phuong tien khong duoc vuot qua 100 ky tu";
}
