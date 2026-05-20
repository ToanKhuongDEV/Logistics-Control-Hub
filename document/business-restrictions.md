# Ràng buộc logic nghiệp vụ

Tài liệu này liệt kê các ràng buộc restrict cơ bản nên có trong dự án Logistics Control Hub để tránh sai lệch dữ liệu và sai luồng vận hành.

## 1. Ràng buộc đơn hàng

- Đơn hàng chỉ được sửa địa chỉ, kho, khối lượng, thể tích khi còn ở trạng thái `CREATED`.
- Đơn hàng đang ở trạng thái `IN_TRANSIT` không được đổi địa chỉ giao, đổi kho, đổi tài xế, đổi khối lượng hoặc đổi thể tích.
- Đơn hàng ở trạng thái `DELIVERED` hoặc `CANCELLED` là trạng thái cuối, không được sửa, xóa hoặc chuyển ngược về trạng thái khác.
- Luồng trạng thái hợp lệ nên là:
  - `CREATED -> IN_TRANSIT -> DELIVERED`
  - `CREATED -> CANCELLED`
  - `IN_TRANSIT -> CANCELLED` chỉ dành cho admin hoặc người có quyền cao.
- Một đơn hàng không được nằm trong nhiều route đang hoạt động cùng lúc.
- Không cho chuyển đơn hàng sang `IN_TRANSIT` nếu chưa có route, tài xế và xe hợp lệ.
- Không cho xóa đơn hàng đang `IN_TRANSIT`; cần hủy hoặc hoàn tất đơn trước.
- Chỉ các đơn hàng `CREATED` mới được đưa vào tối ưu tuyến đường.
- Không đưa đơn hàng `DELIVERED`, `CANCELLED` hoặc `IN_TRANSIT` vào routing mới.

## 2. Ràng buộc tài xế

- Một tài xế chỉ được gán cho một xe tại một thời điểm.
- Tài xế đang có đơn hàng `IN_TRANSIT` hoặc route `IN_PROGRESS` không được nhận thêm đơn hoặc route khác trùng thời gian.
- Không cho xóa tài xế nếu tài xế đang được gán với xe hoặc còn đơn hàng liên quan.
- Không cho đổi thông tin quan trọng của tài xế, ví dụ số bằng lái, khi tài xế đang đi giao.
- Tài xế được gán với xe thuộc kho nào thì chỉ nên nhận đơn hàng của kho đó, trừ trường hợp admin điều phối liên kho.
- Số bằng lái và số điện thoại tài xế phải là duy nhất trong hệ thống.

## 3. Ràng buộc xe

- Chỉ xe `ACTIVE`, có tài xế và có kho mới được dùng để tối ưu tuyến đường.
- Xe `MAINTENANCE` hoặc `IDLE` không được đưa vào route giao hàng.
- Xe đang có route `IN_PROGRESS` không được đổi tài xế, đổi kho, chuyển sang bảo trì hoặc xóa.
- Tổng khối lượng đơn hàng trong route không được vượt quá `maxWeightKg` của xe.
- Tổng thể tích đơn hàng trong route không được vượt quá `maxVolumeM3` của xe.
- `costPerKm`, `maxWeightKg` và `maxVolumeM3` không được là giá trị âm.
- Mã xe phải là duy nhất trong hệ thống.

## 4. Ràng buộc kho

- Chỉ kho đang hoạt động, tức `isActive = true`, mới được nhận đơn mới hoặc chạy routing.
- Không được tắt hoặc xóa kho nếu kho còn đơn hàng `CREATED`, đơn hàng `IN_TRANSIT`, xe active hoặc route chưa hoàn tất.
- Đơn hàng và xe được chọn để tối ưu tuyến đường phải thuộc cùng một kho.
- Nhân viên bị giới hạn kho chỉ được xem, tạo, sửa, xóa dữ liệu trong các kho được phân quyền.
- Chuyển đơn hàng hoặc xe sang kho khác nên yêu cầu quyền admin hoặc quyền điều phối liên kho.

## 5. Ràng buộc tuyến đường

- Route phải bắt đầu tại kho và kết thúc tại kho.
- Mỗi đơn hàng trong một route chỉ được xuất hiện đúng một lần.
- Khi route đã `IN_PROGRESS`, không được đổi thứ tự điểm dừng, đổi xe, đổi tài xế hoặc đổi địa chỉ đơn hàng.
- Route chỉ được chuyển sang `COMPLETED` khi tất cả đơn hàng trong route đã được giao thành công hoặc được xử lý hợp lệ.
- Nếu sửa đơn hàng, xe hoặc kho sau khi đã tạo route nhưng trước khi route chạy, route cũ nên bị hủy hoặc bắt buộc tối ưu lại.
- Một xe không được có nhiều route `IN_PROGRESS` cùng lúc.
- Một route `CANCELLED` hoặc `COMPLETED` không được tiếp tục cập nhật điểm dừng.

## 6. Ràng buộc dữ liệu

- Mã đơn hàng phải là duy nhất trong hệ thống.
- Địa chỉ giao hàng phải có đủ thông tin cần thiết và geocode được ra tọa độ.
- Không cho tạo đơn hàng thiếu địa chỉ giao.
- Không cho tạo xe thiếu tải trọng hoặc thể tích nếu xe được dùng cho routing.
- Khối lượng và thể tích đơn hàng không được là giá trị âm.
- Bulk update phải validate toàn bộ danh sách trước khi cập nhật. Nếu có một bản ghi lỗi, không nên cập nhật nửa chừng.
- Các bản ghi đã soft delete không được xuất hiện trong danh sách lựa chọn hoặc được gán vào nghiệp vụ mới.

## 7. Ràng buộc phân quyền và audit

- Dispatcher chỉ được hủy đơn hàng `CREATED`; hủy đơn đã xác nhận hoặc đang giao cần quyền cao hơn.
- Chuyển đơn hàng hoặc xe sang kho khác cần quyền admin hoặc quyền tương đương.
- Mọi thao tác quan trọng phải được ghi audit log, bao gồm tạo đơn, sửa đơn, hủy đơn, tạo route, chạy routing, đổi kho, đổi tài xế và đổi trạng thái.
- Không cho người dùng thao tác trên dữ liệu ngoài phạm vi kho được phân quyền.
- Khi thao tác thất bại do validation hoặc phân quyền, hệ thống nên ghi nhận audit log thất bại để phục vụ truy vết.

## 8. Nhóm ràng buộc nên ưu tiên triển khai

- Khóa sửa đơn hàng khi đơn đã `IN_TRANSIT`.
- Chặn tài xế hoặc xe nhận route trùng khi đang giao hàng.
- Chỉ cho routing các đơn hàng `CREATED`.
- Không cho đổi xe, tài xế hoặc kho khi route đang chạy.
- Enforce tải trọng và thể tích tối đa của xe.
- Không cho xóa tài xế, xe, kho hoặc đơn hàng đang tham gia nghiệp vụ chưa hoàn tất.
