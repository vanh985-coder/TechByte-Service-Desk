---
name: db-schema
description: Bắt buộc dùng khi người dùng nhắc tới ERD, schema, migration, seed, data dictionary, index, constraint, bảng users/tickets/ticket_comments/ticket_history hoặc PostgreSQL của dự án TechByte Service Desk (vai trò TV4 Database Engineer), kể cả khi họ chỉ nói "làm phần database".
---

# Quy tắc chung

- Trả lời NGẮN. Chỉ tạo file, rồi liệt kê lệnh kiểm tra và kết quả. Không giải thích dài, không hỏi lại điều đã chốt bên dưới.
- Không đọc lại đề bài. Mỗi lượt chỉ làm MỘT việc người dùng giao.
- Cấm: DROP DATABASE, xóa volume, `docker compose down -v`, commit mật khẩu/hash thật, mở cổng 5432 ra Internet.
- Sửa schema = tạo migration mới (V002, V003...). Không sửa migration đã chạy.
- Phân định rõ ràng: (1) Quyết định từ PM, (2) Quyết định từ Backend, (3) Quyết định từ DevOps, (4) Điểm tồn đọng cần xác nhận. Tuyệt đối không tự ý thêm bảng/cột ngoài quyết định đã thống nhất.
- Sau khi hoàn thành triển khai DB, ghi 5 quyết định chính (mỗi cái 1 câu) vào cuối file `docs/architecture/db-decisions.md`.

---

# I. Quyết định kỹ thuật Database đã chốt (Core Schema)

1. **Hệ quản trị & Kiểu dữ liệu:**
   - PostgreSQL 16.
   - Primary Key: `BIGINT GENERATED ALWAYS AS IDENTITY`.
   - Kiểu thời gian: `TIMESTAMPTZ NOT NULL DEFAULT now()`.
   - Password hash: Lưu chuỗi hash BCrypt cost 10 (`VARCHAR(255) NOT NULL`, dạng `$2a$10$...` khớp Spring Security).
2. **Cấu trúc 4 bảng (Đã chốt giữ nguyên, không thêm bảng/cột):**
   - `users`:
     * `id`: BIGINT GENERATED ALWAYS AS IDENTITY PK.
     * `email`: VARCHAR(255) NOT NULL, UNIQUE không phân biệt hoa thường (`UNIQUE INDEX ON lower(email)`).
     * `password_hash`: VARCHAR(255) NOT NULL (BCrypt cost 10).
     * `full_name`: VARCHAR(100) NOT NULL.
     * `role`: VARCHAR(20) NOT NULL, `CHECK (role IN ('STAFF', 'TECHNICIAN', 'ADMIN'))`.
     * `created_at`: TIMESTAMPTZ NOT NULL DEFAULT now().
   - `tickets`:
     * `id`: BIGINT GENERATED ALWAYS AS IDENTITY PK.
     * `code`: VARCHAR(50) NOT NULL UNIQUE.
     * `title`: VARCHAR(200) NOT NULL.
     * `description`: VARCHAR(5000) NOT NULL.
     * `priority`: VARCHAR(20) NOT NULL, `CHECK (priority IN ('LOW', 'MEDIUM', 'HIGH', 'URGENT'))`.
     * `status`: VARCHAR(20) NOT NULL DEFAULT 'OPEN', `CHECK (status IN ('OPEN', 'IN_PROGRESS', 'RESOLVED', 'CLOSED'))`.
     * `created_by`: BIGINT NOT NULL, FK `users(id)` ON DELETE RESTRICT.
     * `assigned_to`: BIGINT NULL, FK `users(id)` ON DELETE SET NULL.
     * `created_at`: TIMESTAMPTZ NOT NULL DEFAULT now().
     * `updated_at`: TIMESTAMPTZ NOT NULL DEFAULT now().
   - `ticket_comments`:
     * `id`: BIGINT GENERATED ALWAYS AS IDENTITY PK.
     * `ticket_id`: BIGINT NOT NULL, FK `tickets(id)` ON DELETE CASCADE.
     * `author_id`: BIGINT NOT NULL, FK `users(id)` ON DELETE RESTRICT.
     * `content`: VARCHAR(2000) NOT NULL.
     * `created_at`: TIMESTAMPTZ NOT NULL DEFAULT now().
   - `ticket_history`:
     * `id`: BIGINT GENERATED ALWAYS AS IDENTITY PK.
     * `ticket_id`: BIGINT NOT NULL, FK `tickets(id)` ON DELETE CASCADE.
     * `actor_id`: BIGINT NOT NULL, FK `users(id)` ON DELETE RESTRICT.
     * `old_status`: VARCHAR(20) NULL, `CHECK (old_status IS NULL OR old_status IN ('OPEN', 'IN_PROGRESS', 'RESOLVED', 'CLOSED'))`.
     * `new_status`: VARCHAR(20) NOT NULL, `CHECK (new_status IN ('OPEN', 'IN_PROGRESS', 'RESOLVED', 'CLOSED'))`.
     * `changed_at`: TIMESTAMPTZ NOT NULL DEFAULT now().
3. **Chính sách xóa & Ràng buộc toàn vẹn:**
   - Xóa ticket: Xóa vật lý (`DELETE FROM tickets WHERE id = ...`).
   - Điều kiện xóa: Chỉ **ADMIN** được xóa, và **chỉ được xóa khi ticket đang OPEN** (nếu status khác OPEN thì BE chặn mã lỗi 400).
   - Xóa liên hoàn: Comments và history liên kết tự động bị xóa theo ticket (`ON DELETE CASCADE`).
   - Xóa người dùng: Không được phép xóa nếu user đã tạo ticket, comment hoặc history (`ON DELETE RESTRICT`). Nếu user là người được phân công (`assigned_to`), trường này được gán thành NULL (`ON DELETE SET NULL`).
4. **Hệ thống Index bắt buộc (Kèm comment SQL 1 dòng nêu rõ lý do):**
   - `tickets(status)`: Hỗ trợ dashboard đếm và lọc theo trạng thái.
   - `tickets(created_at DESC)`: Hỗ trợ lọc và sắp xếp theo ngày tạo mới nhất.
   - `tickets(status, created_at DESC)`: Tối ưu truy vấn kết hợp lọc trạng thái và sắp xếp theo ngày tạo.
   - `tickets(assigned_to)`: Tối ưu tra cứu ticket được phân công cho kỹ thuật viên (FK lookup).
   - `tickets(created_by)`: Tối ưu tra cứu ticket do người dùng tạo (FK lookup).
   - `ticket_comments(ticket_id)`: Tối ưu tra cứu danh sách bình luận theo ticket (FK lookup).
   - `ticket_history(ticket_id)`: Tối ưu tra cứu lịch sử thay đổi theo ticket (FK lookup).

---

# II. Quyết định nghiệp vụ từ PM & Hợp đồng API chốt từ Backend

1. **Vòng đời trạng thái ticket:**
   - Luồng cơ bản: `OPEN` → `IN_PROGRESS` → `RESOLVED` → `CLOSED`.
   - Có tính năng mở lại ticket (Reopen).
2. **Quy tắc phân công & Ghi log:**
   - Chỉ `TECHNICIAN` mới được phân công xử lý ticket (`assigned_to`). Backend validate ở Java Service layer.
   - Khi tạo ticket mới: Bắt buộc ghi 1 bản ghi đầu tiên vào `ticket_history` trong cùng transaction với `old_status = NULL` và `new_status = 'OPEN'`.
3. **Ma trận phân quyền (RBAC) & API endpoints:**
   - `PATCH /api/tickets/{id}`: Sửa `title`, `description`, `priority` (STAFF sửa khi `OPEN`; ADMIN sửa khi chưa `CLOSED`).
   - `PATCH /api/tickets/{id}/assign`: Chỉ ADMIN gọi, body `{ "assigned_to": <userId> }`.
   - `DELETE /api/tickets/{id}`: Chỉ ADMIN gọi, chỉ xóa khi ticket đang `OPEN`.
   - `PATCH /api/tickets/{id}/status`: Cập nhật trạng thái + ghi log history.

---

# III. Quyết định kỹ thuật từ Backend & DevOps

1. **Backend:**
   - Ngôn ngữ: Java Spring Boot.
   - Quản lý Migration: **Flyway** (sử dụng thư mục chuẩn `database/migrations/V001__init.sql`).
   - Password Hash: Spring Security BCrypt cost 10 (`$2a$10$...`).
   - Cập nhật `updated_at`: Backend tự cập nhật khi update ticket.
   - Transaction: Backend ghi `ticket_history` đồng thời trong cùng transaction.
   - Schema: Giữ nguyên 4 bảng, không cần thêm cột.
2. **DevOps & Docker Compose:**
   - 4 services: `reverse-proxy`, `frontend`, `backend`, `database`.
   - Service database: `database`.
   - Container name: `database`.
   - Volume: `db_volume` (named volume lưu trữ lâu dài).
   - Network: `db_network` (chỉ kết nối `backend` và `database`). Không public port 5432 ra ngoài Internet.

---

# IV. Các điểm nghiệp vụ chi tiết còn chờ PM xác nhận

1. **Quy tắc Reopen:** Khi mở lại ticket, trạng thái đích cụ thể là `OPEN` hay `REOPENED`?
2. **Vai trò Reopen:** Chỉ `STAFF` tạo ticket được mở lại hay cả `ADMIN`?
3. **Thao tác sang `CLOSED`:** TECHNICIAN chuyển trạng thái tới `RESOLVED`, ai là người xác nhận chuyển sang `CLOSED` (STAFF nghiệm thu hay ADMIN)?

---

# V. Đầu ra bắt buộc của TV4 (Database Engineer)

1. `database/migrations/V001__init.sql` — Schema khởi tạo ban đầu (4 bảng, constraints, indexes).
2. `database/seed/seed.sql` — Dữ liệu seed (8 users, 20 tickets, 10 comments, 50 history; biến psql `:'demo_hash'`).
3. `database/tests/constraints.sql` — 10 test case vi phạm ràng buộc PASS/FAIL.
4. `database/scripts/backup.sh` & `database/scripts/restore.sh` — Scripts backup/restore tự động hóa cho container `database`.
5. `docs/architecture/erd.md` — Mermaid ERD + chính sách xóa.
6. `docs/architecture/data-dictionary.md` — Bảng mô tả chi tiết trường dữ liệu.
7. `docs/architecture/db-decisions.md` — Quyết định kiến trúc & ghi nhận xác nhận nhóm.
8. `docs/architecture/backup-restore.md` — Runbook hướng dẫn sao lưu và phục hồi dữ liệu.
9. `docs/architecture/migration-rollback.md` — Đánh giá tương thích và chiến lược rollback migration.
10. `database/README.md` — Hướng dẫn thao tác chuẩn 6 lệnh kèm hướng dẫn backup/restore.
