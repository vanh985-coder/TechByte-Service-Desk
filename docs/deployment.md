# CI/CD — TechByte Service Desk

Workflow chính: `.github/workflows/deploy.yml`.

## Luồng chạy

- Push `dang` và PR vào `main`: lint, 7 test contract FE, build React, test/package BE,
  thử migration + API bằng PostgreSQL 16 tạm, kiểm tra backup/restore trên database
  lab tách biệt, kiểm tra Compose và build Dockerfiles.
- Push/merge `main`: chạy toàn bộ CI, publish hai image gắn commit SHA vào GHCR,
  chuyển cấu hình qua SSH, deploy theo image digest và kiểm tra HTTPS/readiness.
- Run workflow thủ công trên `main`: cùng quy trình release. Chạy ở nhánh khác chỉ
  chạy CI. Workflow từ chối deploy một commit không còn là đầu nhánh `main`.
- Chỉ có một release production chạy tại một thời điểm. VPS có thêm khóa `flock`.

Không triển khai Nginx trên máy phát triển. Nginx production chạy trong service
`reverse-proxy` trên VPS; xem [hướng dẫn server](nginx-server.md).

## Secrets và Variables trên GitHub

Vào **Settings -> Secrets and variables -> Actions**:

| Loại | Tên | Giá trị |
| --- | --- | --- |
| Secret | `SSH_IP_SERVER` | IP/hostname SSH của VPS, không có `https://` |
| Secret | `SSH_PRIVATE_KEY` | Private key chuyên dùng cho deploy |
| Secret | `SSH_USER` | User SSH; workflow cũng hỗ trợ Variable cùng tên, mặc định `ubuntu` |
| Secret | `SSH_KNOWN_HOSTS` | Khóa host SSH đã đối chiếu qua console/kênh tin cậy |
| Variable | `APP_URL` | Domain HTTPS, ví dụ `https://desk.example.com` |
| Variable | `SSH_PORT` | Không bắt buộc; mặc định `22` |

Với cổng SSH khác 22, known_hosts phải có dạng `[hostname]:port` tương ứng.
Không tắt StrictHostKeyChecking. Các secret production không được dùng trong CI PR.
Tạo GitHub Environment tên `production`; nhóm có thể thêm người duyệt release.

Job publish dùng `GITHUB_TOKEN` với `packages: write`; không cần thêm token push
registry vào repository secrets. Với GHCR private, user deploy trên VPS cần đăng
nhập GHCR bằng token có `read:packages` và được cấp quyền đọc hai package.

## Chuẩn bị VPS một lần

Cần Docker Engine + Compose >= 2.24, Bash, OpenSSH, `curl`, `jq`, `tar`, `flock`
(util-linux). User SSH phải được chạy Docker và ghi vào `/opt/techbyte`.
Firewall phải cho phép runner tới SSH theo quy định lab; không giả định runner
GitHub thông thường có IP cố định.

Tạo thư mục do user deploy quản lý:

```bash
sudo install -d -m 0750 -o "$USER" -g "$(id -gn)" /opt/techbyte
mkdir -p /opt/techbyte/shared/tls /opt/techbyte/incoming /opt/techbyte/releases
cp infra/compose/env.example /opt/techbyte/shared/.env
chmod 600 /opt/techbyte/shared/.env
nano /opt/techbyte/shared/.env
```

Điền password/JWT ngẫu nhiên, `APP_DOMAIN`, `APP_URL` và đặt chứng chỉ origin ở:

```text
/opt/techbyte/shared/tls/origin.crt
/opt/techbyte/shared/tls/origin.key
```

Cấu hình Cloudflare Full (strict) theo runbook Nginx. Với GHCR private, chạy
`docker login ghcr.io` bằng user deploy và nhập token đọc package qua prompt.
Không lưu token trong file Git. Workflow không tự đổi package thành public.

## Migration và dữ liệu

Nguồn migration duy nhất là `database/migrations`. Maven đóng gói chúng vào
`BOOT-INF/classes/db/migration` trong JAR; Dockerfile mang theo chính JAR đó.
Flyway chạy trước Hibernate `validate`. Profile test dùng H2 với Flyway tắt;
CI còn khởi động JAR thật với PostgreSQL 16 để kiểm tra migration và API.

Production ghi rõ cả `SPRING_DATASOURCE_*` và `DB_*` cho đúng service `database`;
giá trị localhost còn trong một file env cũ không thể lấn át kết nối container.
Tắt demo seed và tự động baseline trong production. Database đã có schema nhưng
chưa có Flyway history cần nhóm DB/BE xác nhận baseline riêng, không bật tự động
để che lỗi migration. Giữ nguyên named volume `db_volume`.

## Kiểm tra sau deploy và rollback

Deploy lưu image digest/config theo thư mục release riêng, backup database hiện có
bằng `pg_dump`, chờ healthchecks và kiểm tra giao diện HTTPS + `/api/ready`.
Mỗi release giữ snapshot `.env` và chứng chỉ trên VPS với quyền riêng tư 600/700;
rollback dùng đúng snapshot đó. Các file bí mật này không được chuyển về GitHub.
Readiness phải trả `status=READY` và `database=UP` sau khi startup/seed hoàn tất.

CI chạy smoke test đăng nhập, tạo ticket, đổi trạng thái, bình luận, đọc lịch sử và
dashboard; ticket kiểm thử đi đủ luồng tới CLOSED và được giữ làm lịch sử lab.
Để chạy cùng kiểm tra nghiệp vụ qua
domain sau deploy, chuẩn bị một tài khoản ADMIN lab và file tin cậy trên VPS:

```bash
nano /opt/techbyte/shared/smoke.env
chmod 600 /opt/techbyte/shared/smoke.env
```

Nội dung mẫu (thay giá trị, không commit file):

```bash
SMOKE_EMAIL='admin-lab@example.com'
SMOKE_PASSWORD='mat-khau-cua-tai-khoan-lab'
```

File này là cấu hình shell tin cậy, chỉ user deploy được sửa. Test tạo và đóng một
ticket giả lập; dùng môi trường/tài khoản lab được phép. Chỉ ticket còn OPEN khi
test dừng sớm mới được xóa; backend cấm xóa ticket đã chuyển trạng thái.
Nếu không có file này,
CD chỉ chạy kiểm tra HTTPS/readiness; cần bổ sung kiểm tra nghiệp vụ thủ công
khi nghiệm thu. Tài khoản mặc định trong source chỉ được bật ở PostgreSQL CI tạm.

Release lỗi luôn báo failed, kể cả rollback thành công. Tự rollback image/config
chỉ được bật khi người phụ trách xác nhận schema tương thích ngược, bằng marker:

```bash
touch /opt/techbyte/shared/allow-image-rollback
```

Gỡ marker trước migration không tương thích. Rollback image không đảo schema và
không restore DB tự động. Release đầu tiên chưa có bản cũ để khôi phục.
Không chạy `docker compose down -v` hoặc xóa volume để xử lý lỗi.

## Bằng chứng và bàn giao

- GitHub Actions: CI result và deployment job summary có SHA, digest, actor, UTC.
- `/opt/techbyte/releases.log`: kết quả deploy/rollback.
- `/opt/techbyte/current`: release thành công gần nhất; `previous`: bản thành công trước đó.
- `/opt/techbyte/backups`: backup trước thay đổi; cần diễn tập restore riêng trên lab.
- Thư mục release lỗi giữ container status/log trên VPS, không upload log chứa dữ liệu riêng.

Sau khi review, commit/push lên `dang`, mở PR vào `main`, bật required status check
và yêu cầu ít nhất một reviewer. Không push trực tiếp `main`.
