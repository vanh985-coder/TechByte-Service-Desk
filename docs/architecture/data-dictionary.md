# Data Dictionary — TechByte Service Desk

> [!NOTE]
> Phản ánh schema `V001__init.sql`. PostgreSQL 16. Mọi trường `NOT NULL` nếu không ghi khác.

---

## 1. Bảng `users`

Lưu thông tin tài khoản người dùng hệ thống.

| Trường | Kiểu | Bắt buộc | Mặc định | Ràng buộc | Ý nghĩa |
|---|---|---|---|---|---|
| `id` | BIGINT | Có | GENERATED ALWAYS AS IDENTITY | PK | Định danh duy nhất |
| `email` | VARCHAR(255) | Có | — | UNIQUE INDEX trên `lower(email)` | Email đăng nhập, duy nhất không phân biệt hoa thường |
| `password_hash` | VARCHAR(255) | Có | — | — | Chuỗi hash BCrypt của mật khẩu |
| `full_name` | VARCHAR(100) | Có | — | — | Họ tên đầy đủ |
| `role` | VARCHAR(20) | Có | — | CHECK IN ('STAFF', 'TECHNICIAN', 'ADMIN') | Vai trò phân quyền |
| `created_at` | TIMESTAMPTZ | Có | `now()` | — | Thời điểm tạo tài khoản |

---

## 2. Bảng `tickets`

Lưu yêu cầu hỗ trợ kỹ thuật.

| Trường | Kiểu | Bắt buộc | Mặc định | Ràng buộc | Ý nghĩa |
|---|---|---|---|---|---|
| `id` | BIGINT | Có | GENERATED ALWAYS AS IDENTITY | PK | Định danh duy nhất |
| `code` | VARCHAR(50) | Có | — | UNIQUE | Mã ticket hiển thị (VD: TK-001) |
| `title` | VARCHAR(200) | Có | — | — | Tiêu đề mô tả ngắn vấn đề |
| `description` | VARCHAR(5000) | Có | — | — | Mô tả chi tiết vấn đề |
| `priority` | VARCHAR(20) | Có | — | CHECK IN ('LOW', 'MEDIUM', 'HIGH', 'URGENT') | Mức ưu tiên |
| `status` | VARCHAR(20) | Có | `'OPEN'` | CHECK IN ('OPEN', 'IN_PROGRESS', 'RESOLVED', 'CLOSED') | Trạng thái hiện tại |
| `created_by` | BIGINT | Có | — | FK → `users(id)` ON DELETE RESTRICT | Người tạo ticket |
| `assigned_to` | BIGINT | Không | NULL | FK → `users(id)` ON DELETE SET NULL | Kỹ thuật viên được phân công (chỉ TECHNICIAN) |
| `created_at` | TIMESTAMPTZ | Có | `now()` | — | Thời điểm tạo ticket |
| `updated_at` | TIMESTAMPTZ | Có | `now()` | — | Thời điểm cập nhật cuối (Backend tự quản lý) |

---

## 3. Bảng `ticket_comments`

Lưu bình luận và trao đổi trên ticket.

| Trường | Kiểu | Bắt buộc | Mặc định | Ràng buộc | Ý nghĩa |
|---|---|---|---|---|---|
| `id` | BIGINT | Có | GENERATED ALWAYS AS IDENTITY | PK | Định danh duy nhất |
| `ticket_id` | BIGINT | Có | — | FK → `tickets(id)` ON DELETE CASCADE | Ticket được bình luận |
| `author_id` | BIGINT | Có | — | FK → `users(id)` ON DELETE RESTRICT | Người viết bình luận |
| `content` | VARCHAR(2000) | Có | — | — | Nội dung bình luận |
| `created_at` | TIMESTAMPTZ | Có | `now()` | — | Thời điểm bình luận |

---

## 4. Bảng `ticket_history`

Ghi lại mọi thay đổi trạng thái ticket (audit trail).

| Trường | Kiểu | Bắt buộc | Mặc định | Ràng buộc | Ý nghĩa |
|---|---|---|---|---|---|
| `id` | BIGINT | Có | GENERATED ALWAYS AS IDENTITY | PK | Định danh duy nhất |
| `ticket_id` | BIGINT | Có | — | FK → `tickets(id)` ON DELETE CASCADE | Ticket thay đổi trạng thái |
| `actor_id` | BIGINT | Có | — | FK → `users(id)` ON DELETE RESTRICT | Người thực hiện thay đổi |
| `old_status` | VARCHAR(20) | Không | NULL | CHECK NULL hoặc IN ('OPEN', 'IN_PROGRESS', 'RESOLVED', 'CLOSED') | Trạng thái trước (NULL khi tạo ticket mới) |
| `new_status` | VARCHAR(20) | Có | — | CHECK IN ('OPEN', 'IN_PROGRESS', 'RESOLVED', 'CLOSED') | Trạng thái sau |
| `changed_at` | TIMESTAMPTZ | Có | `now()` | — | Thời điểm thay đổi |

---

## Hệ thống Index

| Index | Bảng | Cột | Lý do |
|---|---|---|---|
| `uq_users_lower_email` | users | `lower(email)` UNIQUE | Email duy nhất không phân biệt hoa thường |
| `uq_tickets_code` | tickets | `code` UNIQUE | Mã ticket duy nhất toàn hệ thống |
| `idx_tickets_status` | tickets | `status` | Dashboard đếm và lọc theo trạng thái |
| `idx_tickets_created_at_desc` | tickets | `created_at DESC` | Sắp xếp danh sách theo ngày tạo mới nhất |
| `idx_tickets_status_created_at` | tickets | `status, created_at DESC` | Lọc trạng thái kết hợp sắp xếp ngày |
| `idx_tickets_assigned_to` | tickets | `assigned_to` | Tra cứu ticket được phân công (FK lookup) |
| `idx_tickets_created_by` | tickets | `created_by` | Tra cứu ticket do người dùng tạo (FK lookup) |
| `idx_ticket_comments_ticket_id` | ticket_comments | `ticket_id` | Tra cứu bình luận theo ticket (FK lookup) |
| `idx_ticket_history_ticket_id` | ticket_history | `ticket_id` | Tra cứu lịch sử theo ticket (FK lookup) |

