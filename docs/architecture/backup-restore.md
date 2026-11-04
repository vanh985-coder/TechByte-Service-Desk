# Runbook: Backup & Restore Database — TechByte Service Desk

> [!NOTE]
> Tài liệu chuẩn quy trình sao lưu và phục hồi dữ liệu PostgreSQL 16 dành cho TV4 (Database Engineer) phối hợp cùng TV5 (DevOps).

---

## 1. Thông tin cấu hình hệ thống

- **Hệ quản trị:** PostgreSQL 16
- **Service & Container Name:** `database`
- **Volume lưu trữ cố định:** `db_volume` (Named volume do Docker quản trị, bảo toàn dữ liệu qua restart/recreate container)
- **Mạng:** `db_network` (Mạng nội bộ riêng biệt giữa `backend` và `database`, không public port 5432 ra Internet)
- **Công cụ sao lưu:** `pg_dump` (định dạng Custom `-Fc`)
- **Công cụ phục hồi:** `pg_restore`

---

## 2. Kịch bản 1: Sao lưu định kỳ / trước khi cập nhật (Backup)

### Cách 1: Sử dụng Script tự động

- **Trên Linux/macOS:**
  ```bash
  chmod +x database/scripts/backup.sh
  ./database/scripts/backup.sh
  ```
- **Trên Windows PowerShell:**
  ```powershell
  .\database\scripts\backup.ps1
  ```

File backup sẽ được lưu tự động tại thư mục `database/backups/techbyte_backup_YYYYMMDD_HHMMSS.dump`.

### Cách 2: Thực hiện thủ công bằng lệnh Docker

```bash
docker exec -t database pg_dump \
  -U postgres \
  -d postgres \
  -F c \
  -b \
  -v > techbyte_backup_$(date +%Y%m%d_%H%M%S).dump
```

*Giải thích cờ lệnh:*
- `-F c`: Định dạng nén Custom Binary của PostgreSQL, nén tối ưu, hỗ trợ phục hồi đa luồng và chọn lọc bảng.
- `-b`: Bao gồm dữ liệu Large Objects (nếu có).
- `-v`: Verbose mode ghi lại chi tiết các bước.

---

## 3. Kịch bản 2: Phục hồi vào môi trường Lab kiểm thử độc lập (Test Restore)

> [!IMPORTANT]
> Theo quy định kiểm thử nghiệm thu của dự án, **tuyệt đối không thử nghiệm khôi phục trực tiếp trên database đang vận hành**. Phải dựng một container lab riêng để xác minh file backup.

### Bước 1: Khởi tạo container Lab tạm thời

```bash
docker run -d --name pg-lab-restore \
  -e POSTGRES_PASSWORD=labpassword \
  postgres:16
```
*(Chờ 3-5 giây để PostgreSQL khởi động hoàn tất).*

### Bước 2: Thực hiện khôi phục vào container Lab

- **Linux / macOS:**
  ```bash
  ./database/scripts/restore.sh database/backups/techbyte_backup_YYYYMMDD_HHMMSS.dump pg-lab-restore
  ```
- **Windows PowerShell:**
  ```powershell
  .\database\scripts\restore.ps1 -BackupFile .\database\backups\techbyte_backup_YYYYMMDD_HHMMSS.dump -ContainerName pg-lab-restore
  ```

### Bước 3: Đối chiếu số lượng bản ghi (Trước & Sau)

Chạy truy vấn kiểm chứng số lượng bản ghi trên container lab:

```bash
docker exec -t pg-lab-restore psql -U postgres -d postgres -c "
  SELECT 'users' AS bang, count(*) AS so_luong FROM users
  UNION ALL SELECT 'tickets', count(*) FROM tickets
  UNION ALL SELECT 'ticket_comments', count(*) FROM ticket_comments
  UNION ALL SELECT 'ticket_history', count(*) FROM ticket_history;
"
```

**Tiêu chí thành công:**
- `users`: 8 bản ghi
- `tickets`: 20 bản ghi
- `ticket_comments`: 10 bản ghi
- `ticket_history`: 50 bản ghi (hoặc khớp với thời điểm backup)

### Bước 4: Dọn dẹp container Lab

```bash
docker rm -f pg-lab-restore
```

---

## 4. Kịch bản 3: Xác minh tính bền vững dữ liệu (Persistence Verification)

Quy trình phối hợp cùng TV5 (DevOps) để chứng minh dữ liệu trong `db_volume` không bị mất khi container bị tắt hoặc tạo lại:

1. **Khởi động hệ thống:**
   ```bash
   docker compose up -d database
   ```
2. **Kiểm tra dữ liệu ban đầu:** Đếm số bản ghi trong bảng `tickets`.
3. **Thao tác dừng và xóa container (KHÔNG xóa volume):**
   ```bash
   docker compose down
   # Lưu ý: Tuyệt đối KHÔNG truyền cờ -v (--volumes)
   ```
4. **Khởi động lại container mới sử dụng lại volume cũ:**
   ```bash
   docker compose up -d database
   ```
5. **Kiểm chứng lại:** Truy vấn lại số lượng bản ghi. Kết quả phải nguyên vẹn 100%.

