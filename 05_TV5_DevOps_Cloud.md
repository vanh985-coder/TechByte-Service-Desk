# TV5 — DevOps / Cloud Engineer

**Thành viên:** [Điền tên]  
**Mục tiêu:** Hệ thống chạy trên Ubuntu VPS qua HTTPS, build/deploy có version và có thể rollback.  
**Phạm vi chính:** Dockerfiles, `infra/`, `.github/workflows/`, runbook/evidence triển khai.

## 1. Container, server và mạng

- [ ] Chuẩn bị 01 Ubuntu VPS, Docker Engine + Compose; ghi phiên bản. 4 vCPU/8 GB RAM là gợi ý của đề, không phải cấu hình bắt buộc.
- [ ] Viết Dockerfiles FE/BE, phối hợp chủ source xác nhận lệnh build/start, port và biến môi trường.
- [ ] Compose có tối thiểu frontend, backend, database, reverse-proxy; network phù hợp và volume riêng cho DB.
- [ ] Nginx route / tới FE, /api tới BE bằng tên service/địa chỉ hợp lệ; thống nhất xử lý prefix với BE.
- [ ] Thêm healthcheck, restart policy, log rotation phù hợp.
- [ ] Tách cấu hình local và production lab; staging là mở rộng. env.example chỉ chứa mẫu không bí mật.
- [ ] Firewall/SSH theo quy định lab; chỉ mở cổng cần thiết. Không expose PostgreSQL 5432 ra Internet, không public API nội bộ nếu không cần.
- [ ] Cung cấp thông tin port/service/network cho TV1; trực tiếp cập nhật chi tiết triển khai trên 2 sơ đồ do TV1 tổng hợp.

## 2. Domain, HTTPS và Cloudflare

- [ ] DNS tới IP VPS; bật proxy hostname, xác minh truy cập HTTPS từ Internet.
- [ ] SSL/TLS Full (strict), origin có chứng chỉ hợp lệ; mô tả HTTPS từ client đến Cloudflare và từ Cloudflare đến origin.
- [ ] WAF/Security Rule phù hợp nhu cầu và gói hiện có.
- [ ] Thử hạn chế một endpoint nhạy cảm hoặc IP lab; lưu trước/sau và cách rollback rule, tránh khóa toàn hệ thống.
- [ ] Cache tài nguyên phù hợp; không cache phản hồi login/API cá nhân hoặc dữ liệu phụ thuộc token/cookie.
- [ ] Nêu và kiểm chứng cách hạn chế truy cập origin bỏ qua Cloudflare khi mô hình cho phép.

## 3. CI/CD và rollback

- [ ] PR: lint/format nếu có, chạy BE tests, build FE/BE, hiển thị pass/fail.
- [ ] Merge main: build image gắn commit SHA/version, push registry; không dựa riêng tag latest.
- [ ] Registry/SSH secrets đặt trong cơ chế secrets phù hợp quyền tối thiểu; không in ra log.
- [ ] CD đưa đúng image đã kiểm thử lên VPS, cập nhật Compose, phối hợp TV4/TV3 chạy migration theo quy trình.
- [ ] Smoke test sau deploy: trạng thái container, health/ready, API thật, luồng FE qua domain và log 5xx.
- [ ] Có quy trình phục hồi image/config trước nếu smoke test fail; ghi rõ thao tác tự động hoặc thủ công.
- [ ] Với migration không tương thích, thực hiện phương án riêng do DB/BE thống nhất; không coi rollback image là đủ.
- [ ] Ghi release version, người triển khai, thời gian, kết quả kiểm tra và rollback nếu có.
- [ ] Chạy thử rollback trên lab và giữ evidence.

## 4. Phối hợp và nghiệm thu

| Phối hợp | Phần cần nhận / cùng kiểm tra |
|---|---|
| TV2 | Lệnh build/start FE, cấu hình API URL, smoke test trình duyệt |
| TV3 | Lệnh test/build BE, port, health/ready, DB config, API smoke test |
| TV4 | Volume, migration, backup/restore, tương thích schema khi rollback |
| TV1 | Sơ đồ thực tế, test nghiệm thu, lịch release và hồ sơ |

- [ ] Compose kiểm tra cấu hình hợp lệ; service lên đúng network, volume giữ dữ liệu.
- [ ] Pipeline có lần chạy thành công gắn commit/image/version và review PR.
- [ ] Domain HTTPS sử dụng được đầy đủ; backend readiness phản ánh khả năng kết nối DB.
- [ ] Có evidence Cloudflare, container, API, pipeline, persistence, release/rollback.
- [ ] Người khác có thể làm theo runbook với quyền truy cập hợp lệ.

## 5. Thứ tự, PR và sự cố phụ trách

1. Sau buổi chốt thiết kế: dựng VPS, Compose/Nginx và Dockerfiles.
2. Tích hợp FE/BE/DB, domain/HTTPS và cấu hình an toàn.
3. Hoàn thành CI/CD, version image, smoke test và rollback.
4. Diễn tập lỗi, thu bằng chứng và bàn giao runbook.
- **PR 1:** Dockerfiles, Compose, Nginx, env.example và cấu hình CI ban đầu.
- **PR 2:** CD/version/rollback, tài liệu Cloudflare/server, sơ đồ cập nhật và evidence.
- Reviewer: TV2/TV3 review đóng gói/build, TV4 review DB/volume/migration, TV1 review sơ đồ/nghiệm thu.

**Chủ trì 3 tình huống:** reverse proxy 502, HTTPS/Cloudflare lỗi, pipeline thất bại. Khi pipeline lỗi source/test, chủ source chịu trách nhiệm sửa; TV5 khoanh vùng job/step và xác minh pipeline. Với release lỗi, TV3 phân tích nghiệp vụ, TV5 rollback; với DB lỗi, hỗ trợ TV4.

Mỗi báo cáo sự cố có thời điểm/version/tác động, ít nhất 2 giả thuyết, bằng chứng, nguyên nhân, xử lý/retest và phòng ngừa. Bàn giao cấu hình, workflows, 2 sơ đồ phối hợp TV1, deployment evidence, incident reports, release/rollback runbook và ≥2 PR.

## Quy định chung cho cả 5 thành viên

- Dự án: **TechByte Service Desk**. Phân công dựa trên `Teamwork_AI5.pdf`, mục II–XI, trang 3–10.
- Chưa có tên thành viên và hạn nộp: dùng TV1–TV5; các mốc bên dưới là trình tự đề xuất, không phải lịch do đề bài ấn định.
- Ngày đầu cùng thống nhất stack/phiên bản, API (URL, JSON, mã lỗi), quyền, chuyển trạng thái, schema và biến môi trường.
- Một repository chung; không push trực tiếp main. Mỗi người tối thiểu **02 PR thực chất**, có mô tả, bằng chứng và ít nhất một reviewer khác tác giả; xử lý góp ý/conflict trước merge.
- PM review tính đúng yêu cầu; DevOps kiểm tra khả năng build/deploy. Người phụ trách chuyên môn review phần liên quan.
- Mỗi lỗi tích hợp phải có issue: hiện tượng, mức ưu tiên, người xử lý, hướng xử lý và kết quả xác minh.
- Mỗi người tự cập nhật `AI_USAGE.md`: mục tiêu hỏi, nội dung dùng, người kiểm chứng, kết quả. Không đưa bí mật hoặc dữ liệu thật vào AI/Git/báo cáo công khai.
- Chỉ thử sự cố/backup/restore trên lab được phép. Không xóa DB/volume để chữa lỗi.
- Mọi người phải hiểu luồng toàn hệ thống, bảo vệ phần mình làm và giải thích được code/cấu hình.
- Hoàn thành P0 trước P1/P2. Tìm kiếm và lọc trạng thái vẫn được giao thực hiện vì mục IV yêu cầu rõ, dù bảng phạm vi xếp tìm kiếm vào P1.
- P0 ghi “ticket CRUD” nhưng bảng API mẫu thiếu sửa/xóa/phân công: TV1 chốt quyền, hành vi xóa và contract với TV2–TV4 ngay đầu; không tự bỏ các luồng này.

## Nhật ký cá nhân

| Công việc | Trạng thái | PR / bằng chứng | Reviewer | Vướng mắc |
|---|---|---|---|---|
| Điền khi bắt đầu | Chưa làm | Chưa có | Chưa chỉ định | Chưa có |

Chỉ đánh dấu hoàn thành khi có kết quả kiểm chứng; ảnh/log phải che thông tin bí mật.

