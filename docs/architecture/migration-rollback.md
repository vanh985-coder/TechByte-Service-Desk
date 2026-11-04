# Đánh Giá Migration & Phương Án Rollback — TechByte Service Desk

> [!NOTE]
> Tài liệu phân tích rủi ro khi rollback ứng dụng và quy trình xử lý migration dành cho TV4 (Database) phối hợp cùng TV3 (Backend) và TV5 (DevOps).

---

## 1. Cơ chế hoạt động của Flyway Migration

- Dự án sử dụng **Flyway** tích hợp trong Spring Boot Java Backend.
- Mỗi migration được đánh version tăng dần (`V001__init.sql`, `V002__...sql`).
- Flyway tự động ghi nhận các migration đã thực thi vào bảng quản trị `flyway_schema_history` kèm mã kiểm tra checksum.
- **Đặc điểm quan trọng:** Flyway Community Edition chỉ thực hiện nâng cấp (`Flyway Migrate`), **không có cơ chế tự động hạ cấp ("down migration")**. Do đó, việc chỉ rollback Docker Image của Backend là **KHÔNG ĐỦ** để hoàn tác thay đổi schema trong Database.

---

## 2. Phân loại thay đổi Schema & Tác động khi Rollback

### Tình huống A: Migration Tương thích ngược (Backward-Compatible / Additive Changes)

*Ví dụ:* Thêm bảng mới, thêm cột có `DEFAULT` hoặc cho phép `NULL`, tạo thêm index mới.

- **Tác động:** Khi Backend gặp sự cố và DevOps rollback image từ version mới (v2) về version cũ (v1):
  - Ứng dụng v1 vẫn hoạt động bình thường trên Database vì các bảng và cột mà v1 cần vẫn tồn tại nguyên vẹn.
- **Hành động:** Chỉ cần rollback Backend Image, giữ nguyên schema Database. Không cần can thiệp DB.

---

### Tình huống B: Migration Phá vỡ tương thích (Breaking / Destructive Changes)

*Ví dụ:* Đổi tên bảng, xóa cột, đổi kiểu dữ liệu, thêm ràng buộc `NOT NULL` (không có default) hoặc thêm ràng buộc `CHECK` chặt chẽ hơn mà code cũ không đáp ứng được.

- **Tác động:** Khi rollback Backend image về v1:
  - Ứng dụng v1 lập tức báo lỗi (ví dụ: `Column not found`, `Check constraint violation`).
  - Giao diện người dùng bị crash hoặc API trả lỗi `500 Internal Server Error`.
- **Hành động:** Rollback image là **không đủ** — bắt buộc phải có phương án hoàn tác Database riêng biệt.

---

## 3. Chiến lược phòng ngừa: Mô hình Expand / Contract (Khuyến nghị áp dụng)

Để tránh hoàn toàn tình huống B, nhóm phát triển cam kết tuân thủ quy tắc **Expand and Contract**:

```
[Trạng thái cũ] ────────► [Phase 1: Expand] ────────► [Phase 2: Migrate App] ────────► [Phase 3: Contract]
(Dùng cột cũ)             (Thêm cột mới nullable)     (App ghi song song 2 cột)         (Xóa cột cũ ở V mới)
```

1. **Tuyệt đối không xóa cột hoặc đổi tên cột trong một lần release duy nhất.**
2. Nếu muốn đổi tên cột `description` thành `details`:
   - *Release 1:* Tạo cột mới `details` (nullable), copy dữ liệu cũ sang. Code mới đọc `details`, ghi vào cả 2 cột.
   - *Release 2:* Sau khi hệ thống vận hành ổn định tối thiểu 1 chu kỳ release, mới tạo migration `V...` xóa cột cũ `description`.

---

## 4. Quy trình xử lý khẩn cấp khi gặp sự cố Rollback Schema

Nếu bắt buộc phải rollback hệ thống khi đã chạy migration phá vỡ tương thích:

### Phương án 1: Tiến lên bằng Migration khắc phục (Forward-Fix Migration - Khuyến nghị)

1. Database Engineer (TV4) tạo ngay một file migration mới với version kế tiếp, ví dụ: `V003__revert_breaking_change.sql`.
2. Nội dung file là các lệnh DDL đảo ngược thay đổi gây lỗi (ví dụ: xóa ràng buộc mới, phục hồi lại kiểu dữ liệu cũ).
3. Backend build lại image hoặc chạy Flyway cli để áp dụng migration khắc phục.
4. Ưu điểm: Bảo toàn toàn bộ lịch sử `flyway_schema_history`, không gây sai lệch checksum.

### Phương án 2: Khôi phục lại từ Snapshot Backup gần nhất (Disaster Recovery)

Áp dụng khi dữ liệu bị xáo trộn nghiêm trọng hoặc cấu trúc DB hỏng không thể rollback bằng DDL:

1. **Dừng Backend tạm thời** để ngắt kết nối ghi dữ liệu:
   ```bash
   docker compose stop backend
   ```
2. **Khôi phục dữ liệu từ bản backup trước lúc release** (sử dụng script `restore.sh` đã chuẩn hóa):
   ```bash
   ./database/scripts/restore.sh database/backups/techbyte_backup_truoc_release.dump database
   ```
3. **Rollback Backend Image** về phiên bản ổn định trước đó:
   ```bash
   docker compose up -d backend
   ```
4. **Kiểm tra Healthcheck:** Gọi `GET /ready` của backend để xác nhận kết nối DB đã bình thường trở lại.

---

## 5. Trách nhiệm phối hợp giữa các thành viên

| Vai trò | Trách nhiệm cụ thể |
|---|---|
| **TV4 (Database)** | Thẩm định tính tương thích ngược của mọi migration trước khi merge PR; chuẩn bị script backup trước release; viết migration khắc phục nếu có sự cố. |
| **TV3 (Backend)** | Viết entity/query tương thích với schema chuyển tiếp; kiểm tra readiness probe. |
| **TV5 (DevOps)** | Thực hiện backup snapshot trước khi deploy version mới; phối hợp dừng/chạy container theo đúng thứ tự khi cần rollback. |

