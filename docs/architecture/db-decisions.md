# Database Decisions — TechByte Service Desk

> [!NOTE]
> Tài liệu ghi nhận các quyết định kiến trúc Database, lý do và nguồn gốc.
> Phân loại: **(PM)** = PM xác nhận, **(BE)** = Backend xác nhận, **(DEVOPS)** = DevOps xác nhận, **(DB)** = Database Engineer.

---

## 1. Kiểu Primary Key: BIGINT GENERATED ALWAYS AS IDENTITY **(DB / BE)**

- **Lý do:** Chuẩn SQL hiện đại, hiệu năng cao trên PostgreSQL 16, tăng tuần tự dễ debug và log, không phụ thuộc extension bên ngoài.
- **Backend xác nhận:** Hoàn toàn tương thích với Spring Data JPA / Java `Long`.

## 2. Kiểu thời gian: TIMESTAMPTZ **(DB / BE)**

- **Lý do:** Lưu kèm timezone chuẩn quốc tế, tránh lệch giờ giữa server, client và container.
- **Mặc định `now()`:** Mọi bản ghi luôn có timestamp tạo hợp lệ dù client có truyền hay không.
- **`updated_at`:** Backend tự cập nhật giá trị khi thay đổi ticket, không phụ thuộc trigger DB **(BE xác nhận)**.

## 3. Chính sách xóa ticket & Điều kiện xóa **(PM / BE)**

- **Xóa vật lý + CASCADE:** Xóa ticket sẽ đồng thời xóa vĩnh viễn toàn bộ comments và history liên quan (`ON DELETE CASCADE`).
- **Điều kiện xóa (BE & PM chốt):** Chỉ **ADMIN** mới có quyền gọi API xóa, và **chỉ được xóa khi ticket đang ở trạng thái `OPEN`** (nếu status khác `OPEN`, Backend trả về HTTP `400 Bad Request`).
- **Bảo toàn dữ liệu người dùng:** Xóa user bị `RESTRICT` đối với `created_by`, `author_id`, `actor_id` (chặn xóa nếu đã có dữ liệu ràng buộc). Nếu user là kỹ thuật viên được giao việc (`assigned_to`), giá trị chuyển thành `NULL` (`ON DELETE SET NULL`).

## 4. Hệ thống Index phục vụ nghiệp vụ **(DB)**

| Index | Lý do nghiệp vụ |
|---|---|
| `tickets(status)` | Dashboard đếm nhanh ticket theo từng trạng thái |
| `tickets(created_at DESC)` | Danh sách ticket sắp xếp theo ngày tạo mới nhất |
| `tickets(status, created_at DESC)` | Lọc ticket theo trạng thái kết hợp sắp xếp ngày tạo |
| `tickets(assigned_to)` | TECHNICIAN tra cứu danh sách ticket được giao (`WHERE assigned_to = ?`) |
| `tickets(created_by)` | STAFF tra cứu danh sách ticket mình tạo (`WHERE created_by = ?`) |
| `ticket_comments(ticket_id)` | Tải danh sách bình luận khi mở chi tiết ticket |
| `ticket_history(ticket_id)` | Tải dòng thời gian lịch sử trạng thái khi xem chi tiết ticket |

## 5. Email unique case-insensitive & Password Hash **(DB / BE)**

- **Unique Email:** Triển khai `CREATE UNIQUE INDEX uq_users_lower_email ON users (lower(email))` để ngăn chặn trùng lặp tài khoản do viết hoa/thường (VD: `Admin@techbyte.local` và `admin@techbyte.local`).
- **Password Hash:** Backend sử dụng Spring Security với **BCrypt cost 10** (`$2a$10$...`). Dữ liệu seed dùng hash mẫu tương thích tuyệt đối.

## 6. Quản lý Transaction & Audit Trail **(BE)**

- **Ghi history đồng thời:** Khi tạo mới ticket, Backend ghi đồng thời 1 bản ghi `ticket_history` (`old_status = NULL`, `new_status = 'OPEN'`) trong cùng một database transaction.
- **Khi cập nhật trạng thái:** UPDATE `tickets.status` và INSERT `ticket_history` thực hiện trong cùng transaction để đảm bảo toàn vẹn dữ liệu audit.
- **Không dùng Trigger:** Giữ schema thuần túy, Backend sở hữu toàn bộ nghiệp vụ transaction.

## 7. Phân công ticket & Kiểm tra TECHNICIAN **(PM / BE)**

- **Quy tắc:** Chỉ user có `role = 'TECHNICIAN'` mới được phân công xử lý ticket.
- **Phương án kiểm tra:** **Backend tự validate tại tầng Java Service Layer**. Khi gọi gán ticket, Backend truy vấn kiểm tra role của `assigned_to`, nếu sai trả lỗi `400 Bad Request`. Database không cần trigger phức tạp.

## 8. Công cụ Migration: Flyway **(BE / DB)**

- **Quyết định:** Dự án Java Backend chính thức sử dụng **Flyway** để quản lý migration.
- **Quy chuẩn đặt tên:** `V001__init.sql`, `V002__...sql` khớp chuẩn mặc định của Flyway trong Spring Boot.

## 9. Thống nhất Schema & Enums cốt lõi **(BE / DB)**

- Không thêm bảng hay cột mới ngoài đề bài. Giữ nguyên 4 bảng: `users`, `tickets`, `ticket_comments`, `ticket_history`.
- **Mức ưu tiên (Priority):** Đã thống nhất tuân thủ chính xác theo Backend Java Enum: `LOW`, `MEDIUM`, `HIGH`, `URGENT` (thay thế cho `CRITICAL`).
- **Trạng thái (Status):** `OPEN`, `IN_PROGRESS`, `RESOLVED`, `CLOSED`.
- **Vai trò (Role):** `STAFF`, `TECHNICIAN`, `ADMIN`.
- Thời gian trạng thái `RESOLVED` và `CLOSED` đã được lưu trữ chính xác trong `ticket_history.changed_at` và `tickets.updated_at`.

## 10. Kiến trúc Hạ tầng Docker Compose cho Database **(DEVOPS / DB)**

- **Service name:** `database`.
- **Container name:** `database`.
- **Volume:** `db_volume` (Named volume lưu trữ dữ liệu PostgreSQL 16 lâu dài, chống mất mát qua restart/recreate).
- **Network:** `db_network` (Mạng nội bộ chỉ kết nối `backend` và `database`, tuyệt đối không public port 5432 ra Internet).
- **Image:** `postgres:16-alpine` hoặc `postgres:16`.

## 11. Hợp đồng API liên quan đến Database **(BE / PM)**

- `PATCH /api/tickets/{id}`: Sửa `title`, `description`, `priority` (STAFF sửa khi `OPEN`; ADMIN sửa khi chưa `CLOSED`).
- `PATCH /api/tickets/{id}/assign`: Chỉ ADMIN gọi, body `{ "assigned_to": <userId> }`.
- `DELETE /api/tickets/{id}`: Chỉ ADMIN gọi, chỉ xóa khi status là `OPEN`.

---

## Các điểm nghiệp vụ chi tiết còn chờ PM làm rõ

> [!NOTE]
> Các nội dung kỹ thuật Backend và DevOps đã được chốt 100%. Dưới đây là các câu hỏi chi tiết về luồng UX/Nghiệp vụ cần PM xác nhận cuối cùng:

1. **Quy tắc Reopen:** Trạng thái đích khi mở lại là `OPEN` hay có trạng thái riêng `REOPENED`?
2. **Vai trò được phép Reopen:** Chỉ STAFF (người tạo ticket) được mở lại, hay ADMIN, hay cả hai?
3. **Thao tác chuyển `CLOSED`:** TECHNICIAN chuyển ticket tới `RESOLVED`, ai là người bấm hoàn tất sang `CLOSED` (STAFF nghiệm thu xác nhận hay ADMIN đóng)?

---

## 5 Quyết định chính (Tóm tắt cốt lõi)

1. **Hệ quản trị & Dữ liệu:** PostgreSQL 16, PK `BIGINT IDENTITY`, thời gian `TIMESTAMPTZ`, email unique case-insensitive, mật khẩu BCrypt cost 10.
2. **Chính sách xóa:** Xóa vật lý ticket kèm `CASCADE` xóa comments/history; chỉ ADMIN được xóa và chỉ được xóa khi ticket đang `OPEN`.
3. **Phân công & Kiểm tra quyền:** Chỉ TECHNICIAN được nhận ticket, Backend Java Service tự validate trước khi gán.
4. **Migration & Transaction:** Sử dụng Flyway (`V001__init.sql`), Backend đảm bảo ghi `ticket_history` đồng thời trong cùng transaction.
5. **Vận hành hạ tầng:** Container `database`, volume `db_volume`, mạng cô lập `db_network`, không mở port 5432 ra Internet.
