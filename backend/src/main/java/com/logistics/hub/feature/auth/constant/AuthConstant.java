package com.logistics.hub.feature.auth.constant;

public final class AuthConstant {

    private AuthConstant() {
    }

    public static final String USER_INFO_RETRIEVED_SUCCESS = "Lấy thông tin người dùng thành công";
    public static final String NOT_AUTHENTICATED = "Chưa được xác thực";
    public static final String LOGIN_SUCCESS = "Đăng nhập thành công";
    public static final String TOKEN_REFRESH_SUCCESS = "Làm mới token thành công";
    public static final String INVALID_CREDENTIALS = "Tên đăng nhập hoặc mật khẩu không đúng";
    public static final String INVALID_TOKEN = "Token không hợp lệ hoặc đã hết hạn";

    public static final String USERNAME_REQUIRED = "Tên đăng nhập không được để trống";
    public static final String PASSWORD_REQUIRED = "Mật khẩu không được để trống";
    public static final String REFRESH_TOKEN_REQUIRED = "Refresh token không được để trống";
}
