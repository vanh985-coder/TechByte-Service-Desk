# TV1 — PM / System Architect / QA

**Thành viên:** [Điền tên]  
**Vai trò:** Nhóm trưởng, thiết kế tổng thể và kiểm thử tích hợp.  
**Mục tiêu:** Cả nhóm giao đúng yêu cầu, các thành phần tích hợp được, có hồ sơ nghiệm thu. TV1 không viết thay bài của các thành viên khác.

## 1. Công việc phụ trách

- [ ] Viết yêu cầu nghiệp vụ, luồng đăng nhập → tạo ticket → phân công → xử lý → đóng ticket.
- [ ] Lập backlog P0/P1/P2; mỗi task có người phụ trách, phụ thuộc, mốc hoàn thành và tiêu chí chấp nhận.
- [ ] Chốt STAFF và ADMIN; TECHNICIAN là tùy chọn. Lập ma trận quyền xem/tạo/sửa/xóa/phân công/bình luận/đổi trạng thái.
- [ ] Chốt chuyển trạng thái OPEN → IN_PROGRESS → RESOLVED → CLOSED và các ngoại lệ được phép; thống nhất dashboard tính “hoàn thành” thế nào.
- [ ] Chốt phạm vi CRUD và cách xóa ticket có bình luận/lịch sử cùng BE/DB; ghi quyết định để tránh mất dấu truy vết ngoài ý muốn.
- [ ] Điều phối API contract với TV2/TV3, schema với TV4, biến môi trường và cổng với TV5. TV3 sở hữu OpenAPI; TV1 kiểm tra tính đầy đủ.
- [ ] Chủ trì **02 sơ đồ riêng**: request/mạng và source → CI/CD → registry → VPS. TV5 cung cấp/cập nhật port, service, network thực tế; TV1 duyệt bản cuối.
- [ ] Ghi biên bản họp, quyết định, phân công; quản lý review PR và issue tích hợp.
- [ ] Viết và thực hiện test tích hợp trên môi trường đã deploy; tập hợp kết quả tự động của BE và bằng chứng của từng người.
- [ ] Tổ chức demo, nghiệm thu; tổng hợp README và báo cáo nhóm PDF/Word 5–10 trang, nêu cả phần chưa làm và lý do.
- [ ] Tổng hợp danh sách thành viên, repo/domain, bảng đóng góp, AI_USAGE và hồ sơ bàn giao. Tài khoản demo trao qua kênh riêng.

## 2. Bộ kiểm thử nghiệm thu cần có

| Nhóm kiểm thử | Kết quả mong đợi |
|---|---|
| Đăng nhập đúng/sai; chưa đăng nhập; token hết hạn nếu dùng token | Thành công hoặc lỗi phù hợp; không lộ password |
| STAFF truy cập ticket ngoài quyền | Bị chặn ở backend, kể cả gọi API trực tiếp |
| Thiếu tiêu đề, mô tả quá dài, ID không tồn tại | Lỗi rõ ràng theo contract, UI không trắng |
| CRUD, phân công, bình luận, đổi trạng thái | Dữ liệu đúng; quyền đúng; lịch sử có actor và thời gian |
| Dashboard | Thay đổi theo dữ liệu thật sau thao tác |
| Triển khai | HTTPS/domain, container, health/ready, API và FE hoạt động |
| DB persistence; backup/restore | TV4/TV5 cung cấp bằng chứng có thể tái hiện |
| Release/rollback | Version rõ; smoke test và khôi phục được kiểm chứng |

## 3. Phối hợp và xử lý sự cố

- TV2/TV3: đối chiếu giao diện với contract, tái hiện lỗi nghiệp vụ.
- TV4: kiểm tra dữ liệu mẫu, quan hệ và báo cáo restore.
- TV5: xác nhận sơ đồ đúng hệ thống, bằng chứng deploy và rollback.
- TV1 điều phối cả 5 tình huống mục IX; người chuyên môn xử lý kỹ thuật. TV1 ghi timeline, tác động, kết quả retest và tổng hợp báo cáo.
- Mỗi incident report gồm: thời điểm/version/triệu chứng; ít nhất 2 giả thuyết; bằng chứng; nguyên nhân hoặc giới hạn kết luận; xử lý và retest; cách tránh lặp lại.

## 4. Thứ tự thực hiện và PR đề xuất

1. Khởi động: chốt scope, quyền, contract, schema, backlog và kiến trúc.
2. Phát triển: review yêu cầu, theo dõi phụ thuộc, chuẩn bị test cases.
3. Tích hợp: chạy end-to-end, mở issue và xác minh bản sửa.
4. Bàn giao: demo, đóng hồ sơ, kiểm tra đủ đầu ra từng thành viên.
- **PR 1:** yêu cầu, ma trận quyền, backlog, biên bản và 2 sơ đồ ban đầu.
- **PR 2:** bộ test tích hợp, kết quả nghiệm thu, sơ đồ cập nhật và hồ sơ tổng hợp. Reviewer: TV3 và TV5 theo phần việc.

## 5. Đầu ra và điều kiện hoàn thành

- `docs/requirements.md`, `docs/backlog.md`, `docs/meetings/`.
- `docs/architecture/`: 2 sơ đồ có port/service/network, khớp thực tế.
- `docs/testing/`: test cases, kết quả pass/fail và issue liên quan.
- README tổng hợp, bảng đóng góp, báo cáo nhóm 5–10 trang, mục lục evidence và incident reports.
- Hoàn thành khi P0 được kiểm chứng hoặc thiếu sót được nêu rõ; mỗi thành viên có ≥2 PR được review; hồ sơ có đường dẫn truy cập được.

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

