---
name: db-ops
description: Bắt buộc dùng khi người dùng nhắc tới backup, restore, pg_dump, volume, persistence (dữ liệu không mất khi restart/recreate), rollback schema, quyền tài khoản DB, hoặc incident "DB mất kết nối" của dự án TechByte Service Desk (vai trò TV4 Database Engineer).
---

# Quy tắc chung

- Trả lời NGẮN: tạo file/script, liệt kê lệnh, ghi kết quả. Không giải thích dài.
- Chỉ thao tác trên môi trường lab và dữ liệu giả. Cấm: `docker compose down -v`, `docker volume rm`, DROP DATABASE trên DB đang dùng, restore đè lên DB chính.
- Không commit credential. Dùng biến môi trường / file .env (đã nằm trong .gitignore); chỉ commit env.example.
- Tên mặc định (đổi nếu TV5 chốt khác): service `postgres`, volume `pgdata`, DB `servicedesk`, biến POSTGRES_DB/USER/PASSWORD.

# Đầu ra

1. database/init/roles.sql — tạo user ứng dụng `sd_app` chỉ có SELECT/INSERT/UPDATE/DELETE trên 4 bảng + USAGE sequence; KHÔNG superuser, KHÔNG tạo bảng. Migration chạy bằng user riêng `sd_migrator`. Mật khẩu lấy từ biến, không ghi cứng.
2. scripts/db/backup.sh — `docker compose exec -T postgres pg_dump -U "$POSTGRES_USER" -Fc "$POSTGRES_DB" > backups/sd_$(date +%Y%m%d_%H%M%S).dump`. Thư mục backups/ nằm trong .gitignore.
3. scripts/db/restore-lab.sh — dựng container riêng `pg-lab` (volume riêng `pglab_data`, cổng chỉ bind 127.0.0.1, KHÔNG đụng DB chính), `pg_restore --no-owner -d <db_lab> <file.dump>`.
4. scripts/db/verify.sh — in số bản ghi 4 bảng + 3 dòng đại diện (ticket có code cố định, 1 comment, 1 history) để đối chiếu.
5. docs/db-persistence.md — biên bản persistence: đếm trước → `docker compose restart postgres` → đếm sau → `docker compose down` (KHÔNG -v) → `up -d` → đếm sau; bảng trước/sau + log.
6. docs/db-backup-restore.md — biên bản: thời gian, file dump + kích thước, số bản ghi gốc vs lab, dòng đại diện khớp, và bằng chứng BE kết nối được DB lab (gọi /ready hoặc GET /api/tickets trỏ vào lab). Ảnh/log che bí mật.
7. docs/db-rollback.md — bảng mỗi migration: thay đổi gì | tương thích ngược với bản BE cũ? | cách xử lý (thêm cột NULL/DEFAULT = an toàn; xóa/đổi tên cột = cần phương án riêng: expand→migrate→contract, hoặc restore từ backup). Nguyên tắc: không sửa migration đã chạy, rollback bằng migration mới.
8. docs/incidents/INC-03-db-mat-ket-noi.md — theo mẫu 6 mục của đề: (1) thời điểm/phiên bản/triệu chứng/ảnh hưởng; (2) >=2 giả thuyết (DB chưa ready, sai network/tên service, sai credential, migration chưa chạy, BE khởi động trước DB); (3) lệnh+log: `docker compose ps`, `docker compose logs --tail=100 postgres backend`, `pg_isready`, `docker compose exec postgres psql -U ... -c '\conninfo'`, `docker network inspect`; (4) root cause hoặc giới hạn kết luận; (5) cách sửa + retest, KHÔNG xóa volume; (6) phòng ngừa (healthcheck pg_isready, depends_on condition: service_healthy, retry kết nối ở BE).

# Kiểm tra bắt buộc

- Mỗi script chạy thật 1 lần, lưu output vào docs/evidence/db/.
- Xác nhận 5432 không public: `ss -ltnp | grep 5432` chỉ thấy 127.0.0.1 hoặc không có trong compose `ports:`.
- Xác nhận sd_app không tạo được bảng (thử CREATE TABLE → bị từ chối).
