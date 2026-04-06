package com.logistics.hub.feature.auth.constant;

public final class AuthConstant {

    private AuthConstant() {
    }

    public static final String USER_INFO_RETRIEVED_SUCCESS = "Lay thong tin nguoi dung thanh cong";
    public static final String NOT_AUTHENTICATED = "Chua duoc xac thuc";
    public static final String LOGIN_SUCCESS = "Dang nhap thanh cong";
    public static final String TOKEN_REFRESH_SUCCESS = "Lam moi token thanh cong";
    public static final String LOGOUT_SUCCESS = "Dang xuat thanh cong";
    public static final String ACCOUNT_CREATED_SUCCESS = "Tao tai khoan nhan vien thanh cong";
    public static final String ACCOUNTS_RETRIEVED_SUCCESS = "Lay danh sach tai khoan thanh cong";
    public static final String ACCOUNT_RETRIEVED_SUCCESS = "Lay chi tiet tai khoan thanh cong";
    public static final String ACCOUNT_UPDATED_SUCCESS = "Cap nhat tai khoan nhan vien thanh cong";
    public static final String ACCOUNT_DELETED_SUCCESS = "Xoa tai khoan nhan vien thanh cong";
    public static final String PASSWORD_CHANGED_SUCCESS = "Doi mat khau thanh cong";
    public static final String FORGOT_PASSWORD_EMAIL_SENT = "Neu email ton tai, he thong da gui huong dan dat lai mat khau";
    public static final String PASSWORD_RESET_SUCCESS = "Dat lai mat khau thanh cong";
    public static final String INVALID_CREDENTIALS = "Ten dang nhap hoac mat khau khong dung";
    public static final String INVALID_TOKEN = "Token khong hop le hoac da het han";
    public static final String REFRESH_TOKEN_NOT_FOUND = "Refresh token khong ton tai hoac da bi thu hoi";
    public static final String USERNAME_ALREADY_EXISTS = "Ten dang nhap da ton tai";
    public static final String EMAIL_ALREADY_EXISTS = "Email da ton tai";
    public static final String ACCOUNT_NOT_FOUND = "Khong tim thay tai khoan";
    public static final String CANNOT_DELETE_OWN_ACCOUNT = "Khong the xoa chinh tai khoan dang dang nhap";
    public static final String LAST_ADMIN_REQUIRED = "He thong phai con it nhat mot admin";
    public static final String CURRENT_PASSWORD_INCORRECT = "Mat khau hien tai khong dung";
    public static final String PASSWORD_MUST_BE_DIFFERENT = "Mat khau moi phai khac mat khau hien tai";

    public static final String USERNAME_REQUIRED = "Ten dang nhap khong duoc de trong";
    public static final String PASSWORD_REQUIRED = "Mat khau khong duoc de trong";
}
