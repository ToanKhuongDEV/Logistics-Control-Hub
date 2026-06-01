# Kế Hoạch Kiểm Thử 6 Use Case Nổi Bật

Tài liệu này mô tả kế hoạch kiểm thử, test case, cách thực thi và mẫu test report cho 6 use case nổi bật của dự án Logistics Control Hub:

1. Đăng nhập hệ thống
2. Quên mật khẩu / đặt lại mật khẩu
3. Tìm kiếm đơn hàng
4. Tạo xe giao hàng
5. Tối ưu tuyến giao hàng
6. Tài xế xác nhận giao hàng thành công

## 1. Kế Hoạch Kiểm Thử

### 1.1 Mục tiêu

- Xác nhận các chức năng nghiệp vụ chính hoạt động đúng theo yêu cầu.
- Kiểm tra phân quyền theo role `ADMIN`, `DISPATCHER`, `DRIVER`.
- Kiểm tra dữ liệu sau thao tác được cập nhật đúng trong hệ thống.
- Phát hiện lỗi validation, lỗi phân quyền, lỗi trạng thái đơn hàng và lỗi routing.
- Đảm bảo các luồng quan trọng có thể được kiểm thử lại thủ công hoặc tự động hóa sau này.

### 1.2 Phạm vi kiểm thử

| Use case | Phạm vi |
| --- | --- |
| Đăng nhập hệ thống | Login đúng/sai, cookie auth, role, redirect, menu theo permission |
| Quên mật khẩu | Gửi yêu cầu reset, token hợp lệ/sai/hết hạn, đổi mật khẩu |
| Tìm kiếm đơn hàng | Tìm theo mã, trạng thái, kho, phân trang, phạm vi kho |
| Tạo xe giao hàng | Tạo xe hợp lệ, validate dữ liệu, trùng mã xe, tài xế đã gán |
| Tối ưu tuyến giao hàng | Chạy routing theo kho, tạo route, cập nhật đơn/tài xế |
| Tài xế xác nhận giao hàng | Driver xem đơn của mình, hoàn tất đơn, cập nhật route |

### 1.3 Ngoài phạm vi

- Kiểm thử tải lớn/performance chuyên sâu.
- Kiểm thử bảo mật nâng cao như penetration testing.
- Kiểm thử tích hợp email thật trên nhiều nhà cung cấp SMTP.
- Kiểm thử dữ liệu bản đồ OSRM cho toàn bộ Việt Nam.

### 1.4 Môi trường kiểm thử

| Thành phần | Giá trị đề xuất |
| --- | --- |
| Frontend | `http://localhost:3000` |
| Backend | `http://localhost:8080` |
| Swagger | `http://localhost:8080/swagger-ui.html` |
| Database | PostgreSQL từ Docker Compose |
| OSRM | `http://localhost:5000` |
| Redis | Redis external hoặc Redis local |

### 1.5 Dữ liệu kiểm thử

Tài khoản seed mặc định:

| Role | Username | Password | Ghi chú |
| --- | --- | --- | --- |
| Admin | `admin01` | `password123` | Có toàn quyền |
| Dispatcher | `user01` | `password123` | Phụ trách kho `1`, `2` |
| Driver | `driver01` | `password123` | Liên kết với driver id `7` |

Điều kiện dữ liệu:

- Database đã import `database_schema.sql` và `seeding_data.sql`.
- Có đơn hàng trạng thái `CREATED` để chạy routing.
- Có xe `ACTIVE`, có tài xế và cùng kho với đơn hàng.
- Có ít nhất một đơn `IN_TRANSIT` được gán cho driver để kiểm thử driver portal.

### 1.6 Tiêu chí pass/fail

| Kết quả | Tiêu chí |
| --- | --- |
| Pass | Kết quả thực tế khớp expected result, dữ liệu cập nhật đúng, không phát sinh lỗi ngoài mong đợi |
| Fail | Sai dữ liệu, sai phân quyền, sai trạng thái, API lỗi 5xx, UI không phản ánh đúng kết quả |
| Blocked | Không thể kiểm thử do thiếu môi trường, thiếu dữ liệu, OSRM/Redis/SMTP không sẵn sàng |
| N/A | Test case không áp dụng trong môi trường hiện tại |

## 2. Test Case

### UC01 - Đăng Nhập Hệ Thống

| ID | Mục tiêu | Tiền điều kiện | Bước thực hiện | Kết quả mong đợi |
| --- | --- | --- | --- | --- |
| UC01-TC01 | Admin đăng nhập thành công | Có tài khoản `admin01` | 1. Mở `/login` 2. Nhập `admin01/password123` 3. Bấm đăng nhập | Điều hướng vào dashboard, hiển thị menu admin như accounts, audit, settings |
| UC01-TC02 | Dispatcher đăng nhập thành công | Có tài khoản `user01` | 1. Mở `/login` 2. Nhập `user01/password123` 3. Bấm đăng nhập | Đăng nhập thành công, chỉ thấy menu theo permission dispatcher |
| UC01-TC03 | Driver đăng nhập thành công | Có tài khoản `driver01` | 1. Mở `/login` 2. Nhập `driver01/password123` | Điều hướng vào khu vực driver hoặc chỉ thấy menu ca giao |
| UC01-TC04 | Sai mật khẩu | Không yêu cầu | 1. Nhập username hợp lệ 2. Nhập mật khẩu sai 3. Bấm đăng nhập | Hiển thị lỗi đăng nhập, không tạo session |
| UC01-TC05 | Truy cập trang protected khi chưa đăng nhập | Chưa login | 1. Mở `/dashboard` trực tiếp | Bị chuyển về `/login` hoặc bị chặn truy cập |
| UC01-TC06 | Logout | Đã login | 1. Bấm đăng xuất 2. Truy cập lại trang protected | Cookie auth bị xóa, người dùng bị yêu cầu đăng nhập lại |

### UC02 - Quên Mật Khẩu / Đặt Lại Mật Khẩu

| ID | Mục tiêu | Tiền điều kiện | Bước thực hiện | Kết quả mong đợi |
| --- | --- | --- | --- | --- |
| UC02-TC01 | Gửi yêu cầu quên mật khẩu hợp lệ | SMTP đã cấu hình hoặc kiểm tra API | 1. Mở `/forgot-password` 2. Nhập email tồn tại 3. Submit | Hệ thống trả thông báo đã gửi hướng dẫn reset |
| UC02-TC02 | Gửi email không tồn tại | Không yêu cầu | 1. Nhập email không có trong hệ thống 2. Submit | Hệ thống xử lý an toàn, không lộ thông tin nhạy cảm |
| UC02-TC03 | Reset bằng token hợp lệ | Có token reset hợp lệ | 1. Mở `/reset-password?token=...` 2. Nhập mật khẩu mới 3. Submit | Mật khẩu được đổi, có thể đăng nhập bằng mật khẩu mới |
| UC02-TC04 | Reset bằng token sai | Có token không hợp lệ | 1. Nhập token sai 2. Submit mật khẩu mới | Hiển thị lỗi token không hợp lệ, không đổi mật khẩu |
| UC02-TC05 | Reset bằng token hết hạn | Có token hết hạn | 1. Dùng token quá hạn 2. Submit | Hiển thị lỗi token hết hạn |
| UC02-TC06 | Validate mật khẩu mới | Không yêu cầu | 1. Nhập mật khẩu mới không đạt rule 2. Submit | Hệ thống báo lỗi validation |

### UC03 - Tìm Kiếm Đơn Hàng

| ID | Mục tiêu | Tiền điều kiện | Bước thực hiện | Kết quả mong đợi |
| --- | --- | --- | --- | --- |
| UC03-TC01 | Tìm theo mã đơn | Có đơn `ORD-001` | 1. Login admin/dispatcher 2. Mở `/orders` 3. Nhập `ORD-001` vào ô tìm kiếm | Danh sách chỉ hiển thị đơn phù hợp |
| UC03-TC02 | Lọc theo trạng thái `CREATED` | Có đơn `CREATED` | 1. Chọn filter trạng thái `CREATED` | Chỉ hiển thị đơn trạng thái `CREATED` |
| UC03-TC03 | Lọc theo trạng thái `IN_TRANSIT` | Có đơn `IN_TRANSIT` | 1. Chọn filter trạng thái `IN_TRANSIT` | Chỉ hiển thị đơn đang giao |
| UC03-TC04 | Phân trang | Có nhiều hơn một trang dữ liệu | 1. Chuyển trang 2. Đổi page size nếu có | Dữ liệu thay đổi đúng theo trang |
| UC03-TC05 | Dispatcher chỉ thấy đơn trong kho được phân công | Login `user01` | 1. Mở `/orders` 2. Lọc theo kho ngoài phạm vi nếu UI/API cho phép | Không thấy hoặc bị chặn dữ liệu ngoài kho |
| UC03-TC06 | Tìm kiếm không có kết quả | Không yêu cầu | 1. Nhập chuỗi không tồn tại | Hiển thị trạng thái rỗng, không lỗi |

### UC04 - Tạo Xe Giao Hàng

| ID | Mục tiêu | Tiền điều kiện | Bước thực hiện | Kết quả mong đợi |
| --- | --- | --- | --- | --- |
| UC04-TC01 | Tạo xe hợp lệ | Login admin hoặc user có quyền | 1. Mở `/fleet` 2. Bấm tạo xe 3. Nhập mã xe mới, tải trọng, thể tích, chi phí/km, loại xe, trạng thái, kho 4. Lưu | Xe được tạo và xuất hiện trong danh sách |
| UC04-TC02 | Tạo xe trùng mã | Có mã xe đã tồn tại | 1. Tạo xe với code đã tồn tại | Hệ thống báo lỗi trùng mã xe |
| UC04-TC03 | Tạo xe thiếu trường bắt buộc | Không yêu cầu | 1. Bỏ trống code hoặc thông tin bắt buộc 2. Lưu | Form/API báo lỗi validation |
| UC04-TC04 | Tạo xe với số âm | Không yêu cầu | 1. Nhập tải trọng/thể tích/chi phí âm 2. Lưu | Hệ thống từ chối dữ liệu không hợp lệ |
| UC04-TC05 | Gán tài xế đã có xe khác | Có tài xế đã được gán | 1. Chọn tài xế đã gán cho xe khác 2. Lưu | Hệ thống báo tài xế đã được gán |
| UC04-TC06 | Dispatcher tạo xe ngoài phạm vi kho | Login dispatcher | 1. Tạo xe ở kho không thuộc phạm vi | Bị chặn quyền hoặc không thể chọn kho ngoài phạm vi |

### UC05 - Tối Ưu Tuyến Giao Hàng

| ID | Mục tiêu | Tiền điều kiện | Bước thực hiện | Kết quả mong đợi |
| --- | --- | --- | --- | --- |
| UC05-TC01 | Chạy routing thành công theo kho | Có đơn `CREATED`, xe `ACTIVE` có tài xế cùng kho | 1. Login admin/dispatcher 2. Mở màn hình routing/orders 3. Chọn kho 4. Bấm tối ưu tuyến | Tạo routing run `COMPLETED`, có route và route stop |
| UC05-TC02 | Đơn được cập nhật sau routing | Routing thành công | 1. Kiểm tra các đơn được route | Đơn chuyển sang `IN_TRANSIT`, có driver được gán |
| UC05-TC03 | Không có xe phù hợp | Kho không có xe `ACTIVE` có tài xế | 1. Chạy routing cho kho đó | Hệ thống báo không tìm thấy phương tiện phù hợp |
| UC05-TC04 | Không có đơn phù hợp | Kho không có đơn `CREATED` | 1. Chạy routing cho kho đó | Hệ thống báo không tìm thấy đơn hàng phù hợp |
| UC05-TC05 | Dispatcher chạy routing ngoài phạm vi kho | Login dispatcher | 1. Gọi/chọn kho ngoài phạm vi | Bị chặn quyền truy cập |
| UC05-TC06 | Xem lịch sử routing | Có routing run | 1. Mở `/history` 2. Chọn kho 3. Xem chi tiết run | Hiển thị run, route, stop, khoảng cách, chi phí |

### UC06 - Tài Xế Xác Nhận Giao Hàng Thành Công

| ID | Mục tiêu | Tiền điều kiện | Bước thực hiện | Kết quả mong đợi |
| --- | --- | --- | --- | --- |
| UC06-TC01 | Driver xem danh sách đơn được giao | Login `driver01`, có đơn `IN_TRANSIT` gán cho driver | 1. Mở `/driver` | Chỉ thấy đơn của driver hiện tại |
| UC06-TC02 | Driver xem chi tiết đơn | Có đơn trong danh sách | 1. Chọn một đơn | Hiển thị địa chỉ, kho, trạng thái, route/stop nếu có |
| UC06-TC03 | Driver hoàn tất đơn `IN_TRANSIT` | Có đơn `IN_TRANSIT` | 1. Bấm hoàn tất giao hàng | Đơn chuyển sang `DELIVERED`, ghi audit log |
| UC06-TC04 | Không cho hoàn tất đơn không thuộc driver | Có đơn của driver khác | 1. Gọi API hoàn tất orderId không thuộc driver | Trả lỗi not found/forbidden, không cập nhật đơn |
| UC06-TC05 | Không cho hoàn tất đơn không phải `IN_TRANSIT` | Có đơn `CREATED` hoặc `DELIVERED` | 1. Gọi hoàn tất đơn đó | Hệ thống báo chỉ đơn `IN_TRANSIT` mới hoàn tất được |
| UC06-TC06 | Route hoàn tất khi tất cả đơn đã giao | Tất cả order stop trong route được delivered | 1. Hoàn tất các đơn trong route | Route chuyển sang `COMPLETED` |

## 3. Thực Thi Kiểm Thử

### 3.1 Chuẩn bị môi trường

```bash
docker compose up -d --build
docker compose ps
```

Kiểm tra các URL:

```text
Frontend: http://localhost:3000
Backend:  http://localhost:8080
Swagger:  http://localhost:8080/swagger-ui.html
Health:   http://localhost:8080/actuator/health
```

Nếu cần reset dữ liệu:

```bash
docker compose down -v
docker compose up -d --build
```

### 3.2 Thực thi thủ công qua UI

1. Mở frontend tại `http://localhost:3000`.
2. Đăng nhập bằng tài khoản phù hợp từng test case.
3. Thực hiện test case theo bảng ở phần 2.
4. Ghi lại actual result, trạng thái pass/fail và bằng chứng nếu có.
5. Với các case liên quan dữ liệu, kiểm tra lại trên UI hoặc Swagger/API.

### 3.3 Thực thi qua API

Có thể dùng Swagger UI hoặc Postman để kiểm thử API. Vì hệ thống dùng HttpOnly cookie, nên cần giữ cookie sau khi login.

Các endpoint chính:

| Use case | Endpoint liên quan |
| --- | --- |
| Đăng nhập | `POST /api/v1/auth/login`, `GET /api/v1/auth/me`, `POST /api/v1/auth/logout` |
| Quên mật khẩu | `POST /api/v1/auth/forgot-password`, `POST /api/v1/auth/reset-password` |
| Tìm kiếm đơn hàng | `GET /api/v1/orders` |
| Tạo xe | `POST /api/v1/vehicles` |
| Tối ưu tuyến | `POST /api/v1/routing/optimize?depotId={id}` |
| Driver hoàn tất đơn | `PATCH /api/v1/driver/me/orders/{orderId}/complete` |

### 3.4 Thứ tự thực thi đề xuất

1. UC01 - Đăng nhập hệ thống
2. UC02 - Quên mật khẩu / đặt lại mật khẩu
3. UC03 - Tìm kiếm đơn hàng
4. UC04 - Tạo xe giao hàng
5. UC05 - Tối ưu tuyến giao hàng
6. UC06 - Tài xế xác nhận giao hàng thành công

Lý do: UC05 và UC06 phụ thuộc vào dữ liệu đăng nhập, đơn hàng, xe, tài xế và trạng thái đơn.

## 4. Test Report

### 4.1 Thông tin tổng quan

| Trường | Giá trị |
| --- | --- |
| Tên báo cáo | Test report 6 use case nổi bật |
| Dự án | Logistics Control Hub |
| Phiên bản build | `<ghi commit/build/version>` |
| Người kiểm thử | `<ghi tên>` |
| Ngày kiểm thử | `<dd/mm/yyyy>` |
| Môi trường | Local Docker / Dev / Staging |

### 4.2 Tổng hợp kết quả

| Use case | Tổng TC | Pass | Fail | Blocked | Ghi chú |
| --- | ---: | ---: | ---: | ---: | --- |
| UC01 - Đăng nhập hệ thống | 6 |  |  |  |  |
| UC02 - Quên mật khẩu | 6 |  |  |  |  |
| UC03 - Tìm kiếm đơn hàng | 6 |  |  |  |  |
| UC04 - Tạo xe giao hàng | 6 |  |  |  |  |
| UC05 - Tối ưu tuyến giao hàng | 6 |  |  |  |  |
| UC06 - Tài xế xác nhận giao hàng | 6 |  |  |  |  |
| **Tổng** | **36** |  |  |  |  |

### 4.3 Chi tiết lỗi

| Bug ID | Test case | Mức độ | Mô tả lỗi | Bước tái hiện | Kết quả mong đợi | Kết quả thực tế | Trạng thái |
| --- | --- | --- | --- | --- | --- | --- | --- |
| BUG-001 | `<UCxx-TCxx>` | High/Medium/Low |  |  |  |  | Open |

### 4.4 Kết luận

| Tiêu chí | Kết quả |
| --- | --- |
| Có thể release không? | `<Yes/No>` |
| Use case rủi ro cao | `<ghi use case>` |
| Lỗi cần sửa trước release | `<ghi danh sách bug>` |
| Ghi chú bổ sung | `<ghi chú>` |

### 4.5 Mẫu ghi kết quả từng test case

| Test case ID | Người test | Ngày test | Actual result | Status | Evidence |
| --- | --- | --- | --- | --- | --- |
| UC01-TC01 |  |  |  | Pass/Fail/Blocked | Screenshot/log/link |
