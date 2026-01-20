# AI Supply Chain Control Tower

Một hệ thống logistics tập trung thực hiện tối ưu hóa lộ trình theo thời gian thực, xử lý sự cố hướng sự kiện và quy trình giao hàng bền vững.
Dự án này mô phỏng cách các công ty logistics hiện đại quản lý vận hành đội xe dưới các ràng buộc thực tế và những sự cố bất ngờ.

---

## 1. Tổng Quan Dự Án

Trong logistics thực tế, việc lập kế hoạch lộ trình không phải là một tác vụ tĩnh làm một lần là xong. Xe di chuyển liên tục, đơn hàng có khung giờ giao nhận cụ thể, và các sự cố như tắc đường hay hỏng xe có thể xảy ra bất cứ lúc nào.

**AI Supply Chain Control Tower** được xây dựng để mô phỏng một môi trường vận hành thực tế, nơi hệ thống:
- Tối ưu hóa lộ trình giao hàng cho nhiều xe và đơn hàng cùng lúc
- Theo dõi di chuyển của xe liên tục gần như thời gian thực
- Tự động phản ứng với các gián đoạn mà không cần con người can thiệp
- Cho phép quản trị viên ghi đè quyết định khi cần thiết
- Quản lý các quy trình giao hàng kéo dài một cách tin cậy

---

## 2. Các Khả Năng Chính

### 2.1 Tối Ưu Hóa Lộ Trình (Tính Năng Cốt Lõi)

- Giải quyết **Bài toán định tuyến xe (Vehicle Routing Problem - VRP)** cho nhiều xe và đơn hàng
- Cân nhắc các yếu tố:
    - Tải trọng của xe
    - Khung giờ giao hàng (Time windows)
    - Khoảng cách và thời gian di chuyển ước tính
- Tối ưu hóa cho:
    - Tổng quãng đường di chuyển ngắn nhất
    - Giảm thiểu việc giao hàng trễ giờ
- Hỗ trợ **tối ưu hóa lại (re-optimization)** khi điều kiện thay đổi

---

### 2.2 Theo Dõi Đội Xe Thời Gian Thực (Mô Phỏng)

- Các xe định kỳ phát tín hiệu dữ liệu GPS
- Cập nhật vị trí được truyền qua Kafka
- Vị trí xe và trạng thái đơn hàng được đẩy xuống frontend thông qua WebSocket
- Cho phép nhìn thấy toàn cảnh hoạt động của đội xe gần như tức thời

---

### 2.3 Quản Lý Sự Cố (Ra Quyết Định Tự Động)

Hệ thống tự động phản ứng với các gián đoạn vận hành như:
- Tắc đường
- Xe bị hỏng
- Giao hàng bị trễ

Đối với mỗi sự cố, hệ thống sẽ:
1. Phát hiện sự kiện
2. Phân tích các lộ trình và đơn hàng bị ảnh hưởng
3. Quyết định chiến lược giảm thiểu rủi ro tốt nhất
4. Kích hoạt tối ưu hóa lại một phần hoặc toàn bộ lộ trình
5. Cập nhật thời gian dự kiến (ETA) và trạng thái đơn hàng

Điều này loại bỏ sự cần thiết phải có điều phối viên xử lý thủ công.

---

### 2.4 Kiểm Soát Có Con Người Tham Gia (Human-in-the-loop)

Quản trị viên có thể can thiệp khi cần thiết bằng cách:
- Ép buộc tối ưu hóa lại lộ trình
- Vô hiệu hóa xe
- Khóa lộ trình để ngăn chặn thay đổi
- Tăng mức độ ưu tiên cho các đơn hàng quan trọng

Mọi hành động thủ công đều được kiểm tra tính hợp lệ so với các ràng buộc và được ghi lại trong nhật ký kiểm toán.

---

### 2.5 Quy Trình Giao Hàng Bền Vững

- Mỗi chuyến giao hàng được mô hình hóa như một **quy trình dài hạn (long-running workflow)**
- Các bước quy trình bao gồm:
    - Gán lộ trình
    - Theo dõi tiến độ
    - Xử lý sự cố
    - Hoàn tất giao hàng
- Các quy trình có tính **bền vững (durable)**:
    - Khởi động lại ứng dụng không làm mất trạng thái giao hàng
    - Các lỗi kỹ thuật được thử lại (retry) một cách an toàn

---

## 3. Kiến Trúc Hệ Thống

Hệ thống được triển khai dưới dạng **ứng dụng đơn khối (monolithic)** với cấu trúc mô-đun hóa dựa trên tính năng (**feature-based**).

### Technology Stack

**Backend**
- Java 17
- Spring Boot
- Kafka (event streaming)
- PostgreSQL
- Temporal (workflow engine)
- OptaPlanner / OR-Tools (tối ưu hóa lộ trình)
- WebSocket (cập nhật thời gian thực)

**Frontend**
- React.js
- Trực quan hóa bản đồ (Leaflet / Mapbox)

**Infrastructure**
- Docker & Docker Compose

---

## 4. Các Nguyên Lý Kiến Trúc

- Triển khai đơn khối để đơn giản hóa
- Mô-đun hóa dựa trên tính năng
- Phân tách rõ ràng giữa:
    - Logic nghiệp vụ (Domain logic)
    - Dịch vụ ứng dụng (Application services)
    - Hạ tầng và bộ chuyển đổi (Infrastructure and adapters)
- Giao tiếp nội bộ hướng sự kiện
- Được thiết kế để dễ dàng tách thành microservices nếu cần

---

## 5. Các Mô-đun Chức Năng Chính

- **Order Management (Quản lý Đơn hàng)**
  Tạo và theo dõi các đơn giao hàng với trạng thái vòng đời

- **Vehicle & Driver Management (Quản lý Xe & Tài xế)**
  Quản lý tính sẵn sàng, tải trọng và trạng thái của đội xe

- **Routing & Optimization (Định tuyến & Tối ưu hóa)**
  Tính toán và cập nhật các lộ trình giao hàng tối ưu

- **Event Processing (Xử lý Sự kiện)**
  Xử lý cập nhật GPS và các sự kiện gián đoạn

- **Disruption Handling (Xử lý Sự cố)**
  Tự động giảm thiểu các sự cố vận hành

- **Workflow Management (Quản lý Quy trình)**
  Đảm bảo thực thi giao hàng tin cậy, lâu dài

- **Control Tower Dashboard (Bảng Điều khiển)**
  Cung cấp khả năng hiển thị và kiểm soát vận hành

---

## 6. Chạy Dự Án

### Yêu cầu tiên quyết
- Docker
- Docker Compose

### Khởi chạy hệ thống
```bash
docker-compose up