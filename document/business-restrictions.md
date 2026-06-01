# Ràng Buộc Nghiệp Vụ

Tài liệu này ghi lại các ràng buộc nghiệp vụ của Logistics Control Hub theo trạng thái code hiện tại. Một số ràng buộc đã được enforce trong service/controller, một số là quy tắc nên bổ sung tiếp để giảm sai lệch dữ liệu vận hành.

## 1. Phân Quyền Và Phạm Vi Kho

### Đã có

- Hệ thống có 3 role: `ADMIN`, `DISPATCHER`, `DRIVER`.
- Quyền được gom theo permission như `order.read`, `order.manage`, `routing.execute`, `audit.read`, `account.manage`.
- `ADMIN` có global scope.
- `DISPATCHER` bị giới hạn theo danh sách kho được phân công.
- `DRIVER` chỉ có quyền liên quan đến ca giao của chính mình.
- Các service nghiệp vụ chính kiểm tra permission và depot access trước khi đọc/sửa dữ liệu.
- API quản lý tài khoản, audit, company và driver portal có `@PreAuthorize` theo permission cụ thể.

### Nên bổ sung/duy trì

- Đưa cấu hình CORS production ra biến môi trường thay vì hard-code host.
- Bổ sung test cho từng boundary: admin, dispatcher cùng kho, dispatcher khác kho, driver không liên kết hồ sơ tài xế.

## 2. Đơn Hàng

### Đã có

- Trạng thái đơn hàng: `CREATED`, `IN_TRANSIT`, `DELIVERED`, `CANCELLED`.
- Khi tạo đơn, nếu không nhập mã thì hệ thống tự sinh mã dạng `ORD-xxx`.
- Không cho trùng mã đơn.
- Dispatcher bắt buộc chọn kho khi tạo đơn.
- Admin có thể tạo đơn không chọn kho; hệ thống tự gán kho active gần nhất trong phạm vi truy cập.
- Dispatcher không được hủy đơn đã xác nhận hoặc đang giao nếu không có quyền `order.cancel.confirmed`.
- Dispatcher không được bulk cancel.
- Chuyển đơn sang kho khác yêu cầu quyền admin tương đương `vehicle.reassign`.
- Các thao tác create, update, delete, bulk update được ghi audit log; lỗi validation/forbidden/not found cũng được ghi nhận ở một số luồng.

### Nên siết thêm

- Chỉ cho sửa địa chỉ, kho, khối lượng, thể tích khi đơn còn `CREATED`.
- Không cho sửa/xóa đơn `IN_TRANSIT` nếu thao tác đó làm sai route đang chạy.
- Coi `DELIVERED` và `CANCELLED` là trạng thái cuối, không cho chuyển ngược.
- Bulk update nên validate toàn bộ danh sách trước khi lưu, đặc biệt với trạng thái terminal.

## 3. Xe

### Đã có

- Trạng thái xe: `ACTIVE`, `MAINTENANCE`, `IDLE`.
- Loại xe: `KG_500`, `KG_750`, `T_1`, `T_1_25`, `T_1_49`.
- Không cho trùng mã xe.
- Không cho gán một tài xế cho nhiều xe.
- Dispatcher không được chuyển xe sang kho khác; thao tác này cần quyền admin.
- Không cho gán tài xế thuộc kho khác cho xe trong phạm vi không hợp lệ.
- Bulk update depot cho xe có kiểm tra danh sách ID tồn tại.

### Nên siết thêm

- Không cho đổi tài xế, đổi kho hoặc chuyển trạng thái bảo trì nếu xe đang có route `IN_PROGRESS`.
- Không cho xóa xe đang tham gia đơn hoặc route chưa hoàn tất.
- Validate rõ `maxWeightKg`, `maxVolumeM3`, `costPerKm` không âm ở cả request và database.

## 4. Tài Xế

### Đã có

- Có CRUD-style API cho tài xế.
- Có API lấy tài xế khả dụng.
- Tài khoản role `DRIVER` có thể liên kết với một hồ sơ tài xế.
- Driver portal yêu cầu user hiện tại phải có hồ sơ tài xế liên kết.

### Nên siết thêm

- Không cho xóa tài xế nếu đang được gán với xe hoặc còn đơn `IN_TRANSIT`.
- Không cho đổi thông tin quan trọng như bằng lái khi tài xế đang có route chưa hoàn tất.
- Duy trì unique cho số bằng lái, số điện thoại hoặc email nếu nghiệp vụ yêu cầu.

## 5. Kho

### Đã có

- Kho có cờ `isActive`.
- Đơn hàng và xe trong routing phải thuộc cùng kho.
- Dispatcher chỉ được thao tác trong kho được phân công.
- Routing theo kho kiểm tra depot access trước khi chạy.

### Nên siết thêm

- Chỉ kho `isActive = true` mới được nhận đơn mới hoặc chạy auto-routing.
- Không cho tắt/xóa kho nếu còn đơn `CREATED`, đơn `IN_TRANSIT`, xe active hoặc route chưa hoàn tất.
- Việc chuyển đơn/xe liên kho nên tiếp tục giới hạn ở admin.

## 6. Routing

### Đã có

- Routing chạy theo `depotId`.
- Auto-routing chọn đơn `CREATED` và xe `ACTIVE` có tài xế trong kho.
- Tất cả xe đưa vào routing phải cùng kho.
- Đơn hàng được chọn phải cùng kho với đội xe.
- OR-Tools tối ưu theo ma trận khoảng cách OSRM, có ràng buộc tải trọng và thể tích.
- OSRM failure có fallback Haversine.
- Redis cache ma trận OSRM.
- Kết quả routing lưu `routing_runs`, `routes`, `route_stops`.
- Sau khi routing thành công, các đơn được route sẽ chuyển sang `IN_TRANSIT` và gán tài xế theo xe.

### Nên siết thêm

- Không cho một đơn nằm trong nhiều route active cùng lúc.
- Không cho một xe có nhiều route `IN_PROGRESS` cùng lúc.
- Nếu sửa đơn/xe/kho sau khi đã tạo route nhưng trước khi route hoàn tất, cần hủy route cũ hoặc bắt buộc tối ưu lại.
- Route `COMPLETED` hoặc `CANCELLED` không nên cho cập nhật stop/vehicle/driver.

## 7. Driver Portal

### Đã có

- Driver chỉ xem đơn `IN_TRANSIT` được gán cho chính mình.
- Driver chỉ hoàn tất đơn của chính mình.
- Chỉ đơn `IN_TRANSIT` mới được driver mark delivered.
- Không cho hoàn tất đơn thuộc route đã `CANCELLED`.
- Khi tất cả order stop của route đã delivered, route tự chuyển sang `COMPLETED`; nếu chưa đủ thì route là `IN_PROGRESS`.
- Hoàn tất đơn có ghi audit log.

### Nên siết thêm

- Bổ sung timestamp thực tế lúc giao thành công nếu cần báo cáo SLA.
- Bổ sung trạng thái thất bại giao hàng hoặc giao lại nếu nghiệp vụ yêu cầu.

## 8. Excel Và Audit

### Đã có

- Export/template hỗ trợ `DEPOT`, `DRIVER`, `ORDER`, `ROUTING`, `VEHICLE`.
- Export hỗ trợ search, status, depotId, fromDate, toDate và maxRows.
- Audit log lưu actor, role, action, resource type, resource id/name, scope depot, status, before/after data, metadata, IP, user agent, request id.

### Nên siết thêm

- Giới hạn `maxRows` bằng cấu hình để tránh export quá lớn.
- Chuẩn hóa audit cho mọi thao tác thất bại quan trọng, không chỉ một số service.
- Nếu có import Excel trong tương lai, cần validate toàn bộ file trước khi ghi database.

## 9. Ưu Tiên Tiếp Theo

- Siết lifecycle của đơn hàng và route.
- Chặn sửa/xóa tài nguyên đang tham gia vận hành chưa hoàn tất.
- Bổ sung test phân quyền theo kho.
- Bổ sung test routing cho trường hợp sai kho, thiếu xe active, thiếu đơn `CREATED`, OSRM fallback và vượt tải.
- Đưa các cấu hình triển khai như CORS, frontend URL, Redis, OSRM vào tài liệu môi trường nhất quán.
