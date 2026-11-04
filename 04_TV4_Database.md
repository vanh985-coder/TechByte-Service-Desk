# TV4 — Database Engineer

**Thành viên:** [Điền tên]  
**Mục tiêu:** PostgreSQL có cấu trúc đúng nghiệp vụ, migration tái lập được và dữ liệu có thể khôi phục.  
**Phạm vi chính:** `database/`, ERD/data dictionary và tài liệu backup/restore.

## 1. Công việc phụ trách

- [ ] Thiết kế cùng TV3; thống nhất kiểu ID, kiểu dữ liệu, nullability, unique, FK và chính sách xóa.
- [ ] Vẽ ERD có PK/FK, quan hệ 1-n, nullability, ràng buộc chính.
- [ ] Viết data dictionary: ý nghĩa trường, kiểu, bắt buộc/null, mặc định, ràng buộc.
- [ ] Viết migration có version; thống nhất công cụ migration và người/lúc chạy với BE/DevOps.
- [ ] Thiết kế index phục vụ trạng thái/ngày và truy vấn thực tế; giải thích lý do.
- [ ] Tạo seed tối thiểu **08 user giả lập, 20 ticket nhiều trạng thái, 10 bình luận**; bổ sung lịch sử để demo.
- [ ] Password seed phải hash tương thích BE; thông tin đăng nhập demo trao riêng, không commit mật khẩu thật.
- [ ] Cấu hình tài khoản ứng dụng chỉ đủ quyền cần thiết, phối hợp tách quyền migration nếu cần.
- [ ] Cùng TV5 dùng volume giữ dữ liệu qua restart/recreate; xác minh persistence.
- [ ] Viết hướng dẫn/script backup và restore vào DB lab riêng; ghi kết quả trước/sau.
- [ ] Đánh giá migration khi rollback ứng dụng; nêu phương án riêng nếu schema không tương thích.

## 2. Các bảng tối thiểu theo nghiệp vụ

| Bảng | Trường chính gợi ý | Ràng buộc cần kiểm tra |
|---|---|---|
| users | id, email, password_hash, full_name, role, created_at | Email unique, mật khẩu hash, role hợp lệ |
| tickets | id, code, title, description, priority, status, created_by, assigned_to, created_at, updated_at | FK users; index trạng thái/ngày; nullability người được giao do nhóm chốt |
| ticket_comments | id, ticket_id, author_id, content, created_at | FK ticket và user, không có bình luận mồ côi |
| ticket_history | id, ticket_id, actor_id, old_status, new_status, changed_at | Truy vết thay đổi trạng thái và người thực hiện |

Không tạo bảng chỉ để đủ số lượng. Quy định xóa ticket phải thống nhất với TV1/TV3 để xử lý bình luận/lịch sử đúng yêu cầu.

## 3. Checklist nghiệm thu

- [ ] Dựng DB mới từ migration và seed thành công.
- [ ] Bộ dữ liệu đủ số lượng; các quan hệ hợp lệ, có nhiều trạng thái.
- [ ] Email trùng, FK không hợp lệ và các giá trị bị cấm bị chặn theo thiết kế.
- [ ] BE tạo/đọc/cập nhật/bình luận được trên schema đã thống nhất.
- [ ] Restart/recreate container theo hướng dẫn TV5 không làm mất dữ liệu.
- [ ] Backup rồi restore vào môi trường lab riêng; đối chiếu số bản ghi và dữ liệu đại diện.
- [ ] BE có thể sử dụng DB đã restore; có log/query/ảnh trước và sau.
- [ ] Database không public Internet, credential không nằm trong Git.

## 4. Phối hợp, thứ tự và PR

1. Ngày đầu chốt ERD/schema với BE, quyền/xóa với PM.
2. Viết migration, seed, data dictionary; bàn giao sớm để BE tích hợp.
3. Kiểm tra constraint/index, persistence, backup/restore cùng TV5.
4. Hoàn thiện bằng chứng và phương án migration/rollback.
- **PR 1:** ERD, data dictionary, migration ban đầu và seed.
- **PR 2:** kiểm chứng/ràng buộc bổ sung theo tích hợp, script và báo cáo backup/restore, tài liệu rollback schema.
- Reviewer: TV3 kiểm tra schema/nghiệp vụ; TV5 kiểm tra vận hành.
- Mọi thay đổi schema có migration mới, thông báo BE; tránh sửa migration đã chạy trên môi trường dùng chung.

## 5. Sự cố và bàn giao

- Chủ trì **DB mất kết nối sau restart**, phối hợp TV3/TV5 kiểm tra readiness, network, credentials, migration; không xóa volume.
- Incident report ghi ít nhất 2 giả thuyết, lệnh/query/log, nguyên nhân, cách sửa, retest và phòng ngừa.
- Bàn giao ERD, data dictionary, migration, seed, backup/restore scripts/runbook, evidence persistence/restore và ≥2 PR.
- Hoàn thành khi schema tái tạo được, BE sử dụng đúng và dữ liệu khôi phục được với bằng chứng.

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

