package com.logistics.hub.feature.auth.constant;

public final class AuthConstant {

    private AuthConstant() {
    }

    public static final String USER_INFO_RETRIEVED_SUCCESS = "Lấy thông tin người dùng thành công";
    public static final String NOT_AUTHENTICATED = "Chưa được xác thực";
    public static final String LOGIN_SUCCESS = "Đăng nhập thành công";
    public static final String TOKEN_REFRESH_SUCCESS = "Làm mới token thành công";
    public static final String LOGOUT_SUCCESS = "Đăng xuất thành công";
    public static final String ACCOUNT_CREATED_SUCCESS = "Tạo tài khoản nhân viên thành công";
    public static final String PASSWORD_CHANGED_SUCCESS = "Đổi mật khẩu thành công";
    public static final String FORGOT_PASSWORD_EMAIL_SENT = "Nếu email tồn tại, hệ thống đã gửi hướng dẫn đặt lại mật khẩu";
    public static final String PASSWORD_RESET_SUCCESS = "Đặt lại mật khẩu thành công";
    public static final String INVALID_CREDENTIALS = "Tên đăng nhập hoặc mật khẩu không đúng";
    public static final String INVALID_TOKEN = "Token không hợp lệ hoặc đã hết hạn";
    public static final String REFRESH_TOKEN_NOT_FOUND = "Refresh token không tồn tại hoặc đã bị thu hồi";
    public static final String USERNAME_ALREADY_EXISTS = "Tên đăng nhập đã tồn tại";
    public static final String EMAIL_ALREADY_EXISTS = "Email đã tồn tại";
    public static final String CURRENT_PASSWORD_INCORRECT = "Mật khẩu hiện tại không đúng";
    public static final String PASSWORD_MUST_BE_DIFFERENT = "Mật khẩu mới phải khác mật khẩu hiện tại";

    public static final String USERNAME_REQUIRED = "Tên đăng nhập không được để trống";
    public static final String PASSWORD_REQUIRED = "Mật khẩu không được để trống";
}
