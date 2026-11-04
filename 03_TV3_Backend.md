# TV3 — Backend Developer

**Thành viên:** [Điền tên]  
**Mục tiêu:** API thực hiện đúng nghiệp vụ, phân quyền ở server, lưu dữ liệu đúng và có kiểm thử tự động.  
**Phạm vi chính:** `backend/`, `docs/api/`.

## 1. Công việc phụ trách

- [ ] Chọn Spring Boot hoặc Node.js cùng nhóm; công bố phiên bản, lệnh build/test/start.
- [ ] Sở hữu API contract/OpenAPI hoặc Postman; chốt JSON, validation, mã lỗi với TV1/TV2.
- [ ] Xác thực session hoặc token; hash password, không trả password/hash hay secret trong response/log.
- [ ] Phân quyền STAFF và ADMIN; TECHNICIAN chỉ bổ sung nếu nhóm chọn. Kiểm tra quyền đối tượng cho cả đọc và cập nhật.
- [ ] Triển khai ticket CRUD theo scope được TV1 chốt, tìm kiếm/lọc trạng thái, phân công người xử lý.
- [ ] Triển khai bình luận, đọc lịch sử, chuyển trạng thái hợp lệ; ghi actor và thời gian.
- [ ] Đảm bảo đổi trạng thái và ghi lịch sử nhất quán trong cùng transaction phù hợp.
- [ ] Dashboard truy vấn số liệu thật theo phạm vi quyền, thống nhất cách đếm với PM/FE.
- [ ] Xử lý input sai, không tìm thấy, chưa xác thực, không đủ quyền; không lộ stacktrace.
- [ ] Cung cấp health kiểm tra tiến trình và readiness có kiểm tra DB; thống nhất mã trạng thái với TV5.
- [ ] Phối hợp migration với TV4, cấu hình môi trường và log với TV5.
- [ ] Viết kiểm thử tự động cho nghiệp vụ, quyền và trường hợp lỗi; cung cấp lệnh để CI chạy.

## 2. API cần bàn giao

| Phương thức | Endpoint tham khảo | Chức năng |
|---|---|---|
| POST | /api/auth/login | Đăng nhập |
| GET | /api/me | Người dùng hiện tại |
| GET | /api/tickets | Danh sách, tìm kiếm/lọc và giới hạn phạm vi |
| POST | /api/tickets | Tạo ticket |
| GET | /api/tickets/{id} | Chi tiết, kiểm tra quyền |
| PATCH | /api/tickets/{id}/status | Đổi trạng thái và lưu lịch sử |
| POST | /api/tickets/{id}/comments | Bình luận |
| GET | /api/dashboard/summary | Thống kê từ DB |
| GET | /health; /ready | Health và readiness |

**Phần cần nhóm chốt bổ sung:** API sửa, xóa, phân công ticket; API đọc bình luận/lịch sử riêng hoặc gộp trong chi tiết; cách logout theo cơ chế xác thực. Đây là phần cụ thể hóa nghiệp vụ/CRUD, không phải tên endpoint bắt buộc do đề đưa ra.

## 3. Bộ kiểm thử bắt buộc

- [ ] Đăng nhập đúng/sai; chưa đăng nhập; token hết hạn nếu chọn token.
- [ ] STAFF không xem/sửa ticket ngoài quyền; gọi API trực tiếp vẫn bị chặn.
- [ ] Thiếu tiêu đề, mô tả vượt giới hạn, ID không tồn tại, cập nhật trái phép.
- [ ] Chuyển trạng thái hợp lệ và không hợp lệ; lịch sử ghi chính xác.
- [ ] Bình luận gắn đúng ticket, tác giả và thời gian.
- [ ] CRUD/phân công tuân thủ ma trận quyền; xóa tuân thủ chính sách đã chốt.
- [ ] Dashboard thay đổi khi dữ liệu thay đổi.
- [ ] DB không sẵn sàng được phản ánh qua readiness và lỗi xử lý phù hợp.

## 4. Phối hợp, thứ tự và PR

1. Chốt contract/schema, tạo project và xác thực.
2. Triển khai ticket, bình luận, lịch sử, dashboard cùng test.
3. Tích hợp FE/DB; chuẩn hóa health/log/config cho DevOps.
4. Chạy test trong CI và smoke test sau deploy.
- TV4 sở hữu schema/migration; TV3 không tự sửa schema trên server.
- Mỗi thay đổi API phải cập nhật contract và thông báo TV2.
- **PR 1:** project, auth/quyền, contract và API tạo/xem ticket kèm test.
- **PR 2:** CRUD còn lại, phân công/trạng thái/bình luận/dashboard, health/ready và kiểm thử bổ sung.
- Reviewer: TV4 cho dữ liệu, TV1 cho nghiệp vụ; TV2 đối chiếu contract, TV5 kiểm tra build.

## 5. Sự cố và điều kiện bàn giao

- Chủ trì phân tích **release lỗi đổi trạng thái**; TV5 thực hiện rollback hạ tầng, TV1/TV2 xác minh luồng.
- Hỗ trợ lỗi 502, DB mất kết nối và pipeline lỗi build/test BE.
- Bàn giao source, OpenAPI/Postman, hướng dẫn chạy/test, env.example không secret, test report, log đã che bí mật và ≥2 PR.
- Hoàn thành khi API đúng quyền/nghiệp vụ, test chạy được trên CI và FE dùng được API sau deploy.

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

