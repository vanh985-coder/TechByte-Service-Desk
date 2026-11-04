# Database — TechByte Service Desk

## Thông tin kiến trúc đã thống nhất

- **Hệ quản trị:** PostgreSQL 16
- **Công cụ Migration:** Flyway (`database/migrations/V001__init.sql`)
- **Docker Compose Service:** `database`
- **Container Name:** `database`
- **Named Volume lưu trữ lâu dài:** `db_volume`
- **Network nội bộ:** `db_network` (chỉ kết nối `backend` và `database`, không mở port ra Internet)
- **Số bảng:** 4 bảng cốt lõi (`users`, `tickets`, `ticket_comments`, `ticket_history`)
- **Dữ liệu Seed:** `database/seed/seed.sql` (8 users, 20 tickets, 10 comments, 50 history)

---

## Hướng dẫn chạy thử nghiệm độc lập (6 bước)

### Yêu cầu
- Docker đã cài đặt và đang chạy.
- Chuỗi BCrypt hash cost 10 cho mật khẩu demo (mặc định tương thích Spring Security).

### Bước 1 — Dựng DB tạm
```bash
docker run -d --name pg-tv4-test \
  -e POSTGRES_PASSWORD=testpass123 \
  -p 127.0.0.1:55432:5432 \
  postgres:16
```
*(Chờ 3 giây để PostgreSQL khởi động xong)*

### Bước 2 — Chạy migration V001
```bash
psql -h 127.0.0.1 -p 55432 -U postgres -d postgres \
  -f database/migrations/V001__init.sql
```
*Kỳ vọng: Tạo 4 bảng, 7 index, 0 lỗi.*

### Bước 3 — Seed dữ liệu mẫu
```bash
# Hash của 'Password123!' (BCrypt cost 10)
DEMO_HASH='$2a$10$dXJ3SW6G7P50lGmMQgel3uGqKa7LXOMD0.hor74MTbLJspKlCZpOq'

psql -h 127.0.0.1 -p 55432 -U postgres -d postgres \
  -v demo_hash="$DEMO_HASH" \
  -f database/seed/seed.sql
```
*Kỳ vọng: 8 users (1 ADMIN, 2 TECH, 5 STAFF), 20 tickets, 10 comments, 50 history records.*

### Bước 4 — Kiểm thử Ràng buộc (Constraint Tests)
```bash
psql -h 127.0.0.1 -p 55432 -U postgres -d postgres \
  -f database/tests/constraints.sql
```
*Kỳ vọng: 10 dòng thông báo `PASS`, 0 dòng `FAIL`.*

### Bước 5 — Đếm bản ghi & Kiểm tra phân bố trạng thái
```bash
psql -h 127.0.0.1 -p 55432 -U postgres -d postgres -c "
  SELECT 'users' AS bang, count(*) AS so_luong FROM users
  UNION ALL SELECT 'tickets', count(*) FROM tickets
  UNION ALL SELECT 'ticket_comments', count(*) FROM ticket_comments
  UNION ALL SELECT 'ticket_history', count(*) FROM ticket_history;
"
```
*Kỳ vọng số lượng:* users=8, tickets=20, ticket_comments=10, ticket_history=50.

Kiểm tra phủ đủ 4 trạng thái:
```bash
psql -h 127.0.0.1 -p 55432 -U postgres -d postgres -c "
  SELECT status, count(*) FROM tickets GROUP BY status ORDER BY status;
"
```
*Kỳ vọng:* CLOSED=5, IN_PROGRESS=5, OPEN=5, RESOLVED=5.

### Bước 6 — Dọn dẹp DB tạm
```bash
docker rm -f pg-tv4-test
```

---

## Hướng dẫn Sao lưu & Phục hồi (Backup / Restore)

Dành cho container chính thức `database`:

### Sao lưu (Backup)
- **Linux/macOS:**
  ```bash
  chmod +x database/scripts/backup.sh
  ./database/scripts/backup.sh
  ```
- **Windows PowerShell:**
  ```powershell
  .\database\scripts\backup.ps1
  ```
File sao lưu sẽ được xuất ra tại thư mục `database/backups/`.

### Phục hồi vào môi trường kiểm thử (Restore Test)
- **Linux/macOS:**
  ```bash
  ./database/scripts/restore.sh database/backups/<ten_file>.dump pg-lab-restore
  ```
- **Windows PowerShell:**
  ```powershell
  .\database\scripts\restore.ps1 -BackupFile .\database\backups\<ten_file>.dump -ContainerName pg-lab-restore
  ```

---

## Cấu trúc thư mục Database

```
database/
├── migrations/
│   └── V001__init.sql          # Flyway migration ban đầu
├── seed/
│   └── seed.sql                # Dữ liệu mẫu (dùng biến psql :'demo_hash')
├── tests/
│   └── constraints.sql         # 10 test case vi phạm ràng buộc PASS/FAIL
├── scripts/
│   ├── backup.sh / backup.ps1   # Scripts backup tự động container "database"
│   └── restore.sh / restore.ps1 # Scripts restore tự động kiểm thử
└── README.md                   # Hướng dẫn này
```

## Tài liệu liên quan

- [ERD](../docs/architecture/erd.md) — Sơ đồ quan hệ thực thể
- [Data Dictionary](../docs/architecture/data-dictionary.md) — Bảng tra cứu thuộc tính
- [DB Decisions](../docs/architecture/db-decisions.md) — Toàn bộ quyết định kiến trúc đã thống nhất
- [Runbook Backup & Restore](../docs/architecture/backup-restore.md) — Chi tiết quy trình sao lưu, phục hồi & persistence
- [Đánh giá Migration Rollback](../docs/architecture/migration-rollback.md) — Chiến lược rollback migration khi sự cố

---

## Quy tắc bắt buộc

- **KHÔNG** sửa migration đã chạy. Khi thay đổi schema, tạo file `V002__...sql`.
- **KHÔNG** commit mật khẩu hoặc BCrypt hash thật vào Git.
- **KHÔNG** mở port 5432 ra Internet.
- **KHÔNG** dùng `docker compose down -v` hoặc xóa volume `db_volume`.
