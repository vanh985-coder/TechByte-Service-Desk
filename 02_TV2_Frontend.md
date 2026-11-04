# TV2 — Frontend Developer

**Thành viên:** [Điền tên]  
**Mục tiêu:** Người dùng thực hiện được nghiệp vụ qua trình duyệt với dữ liệu thật từ API.  
**Phạm vi chính:** `frontend/`; hướng dẫn chạy và bằng chứng giao diện.

## 1. Công việc phụ trách

- [ ] Cùng nhóm chọn React/Next.js và ghi phiên bản; tạo cấu trúc frontend, lệnh chạy/build, cấu hình API URL theo môi trường.
- [ ] Xây lớp gọi API, xử lý xác thực theo lựa chọn session/token đã chốt với TV3; xử lý lỗi theo contract.
- [ ] Trang đăng nhập: kiểm tra nhập liệu, thông báo sai tài khoản, xử lý hết phiên và logout.
- [ ] Dashboard: tổng ticket, đang mở, đang xử lý, hoàn thành; lấy từ API, thống nhất ý nghĩa số liệu với TV1/TV3.
- [ ] Danh sách: mã, tiêu đề, ưu tiên, trạng thái, người phụ trách, cập nhật; tìm kiếm và lọc trạng thái.
- [ ] Tạo ticket: tiêu đề/mô tả/ưu tiên, validation, báo kết quả và cập nhật danh sách.
- [ ] Chi tiết: thông tin, bình luận, lịch sử trạng thái, thao tác đổi trạng thái theo quyền.
- [ ] Giao diện ADMIN phân công; bổ sung sửa/xóa theo phạm vi CRUD và quyền được TV1 chốt.
- [ ] Mọi màn hình dữ liệu có loading/empty/error; không để API lỗi làm trang trắng/đứng.
- [ ] Hiển thị thao tác phù hợp vai trò; hiểu rằng backend vẫn phải kiểm tra quyền.
- [ ] Kết nối API thật, kiểm tra qua Nginx và domain HTTPS; bỏ dữ liệu mock khỏi luồng nghiệm thu.
- [ ] Viết hướng dẫn chạy local, cấu hình mẫu không secret, ảnh minh họa từng luồng.

## 2. Đầu vào cần nhận và bàn giao

| Người phối hợp | Nội dung |
|---|---|
| TV1 | Luồng nghiệp vụ, ma trận quyền, tiêu chí nghiệm thu |
| TV3 | Contract, JSON mẫu, mã lỗi, xác thực, API danh sách/chi tiết/lịch sử/phân công |
| TV4 qua TV3 | Tài khoản demo và dữ liệu nhiều trạng thái để kiểm thử |
| TV5 | Lệnh build/start, biến môi trường build/runtime, cổng và yêu cầu đóng gói |

Có thể dùng mock đúng contract trong lúc BE đang phát triển; nghiệm thu phải chạy API thật. Khi API thay đổi, cập nhật theo phiên bản contract và báo lỗi qua issue.

## 3. Checklist tự kiểm tra

- [ ] Login/logout chạy được; lỗi xác thực không gây vòng lặp hoặc trang trắng.
- [ ] Tạo ticket thiếu tiêu đề/mô tả quá dài hiển thị lỗi hợp lý.
- [ ] Danh sách và dashboard cập nhật sau thao tác, không hard-code.
- [ ] Xem chi tiết, bình luận, lịch sử, phân công, sửa/xóa và đổi trạng thái đúng scope đã chốt.
- [ ] Mất kết nối API, 401/403/404/5xx và dữ liệu rỗng đều có trạng thái giao diện.
- [ ] Build thành công; thao tác qua domain HTTPS không gặp lỗi gọi API do cấu hình URL.
- [ ] Có ảnh/video hoặc test log để PM tái kiểm tra; ghi lỗi còn tồn tại.

## 4. Trình tự và PR đề xuất

1. Sau khi chốt contract: dựng project, lớp API và đăng nhập.
2. Làm danh sách/tạo/chi tiết, bình luận/lịch sử và các thao tác theo quyền.
3. Làm dashboard; hoàn thiện trạng thái lỗi, tích hợp API thật.
4. Phối hợp deploy, smoke test trên domain, bàn giao ảnh và hướng dẫn.
- **PR 1:** khung FE, xác thực, lớp API, danh sách/tạo ticket.
- **PR 2:** chi tiết, dashboard, phân công/CRUD còn lại, xử lý lỗi, tài liệu và evidence.
- Reviewer: TV1 kiểm tra nghiệp vụ, TV3 kiểm tra contract; TV5 kiểm tra build.

## 5. Phần tham gia sự cố và đầu ra

- Release lỗi: tái hiện từ UI, ghi URL/thao tác/thời điểm và request/response đã che bí mật; hỗ trợ TV3/TV5 retest.
- 502 hoặc HTTPS lỗi: cung cấp bằng chứng từ trình duyệt, phân biệt trang FE tải được với API thất bại.
- Đầu ra: source FE; README FE; danh sách biến môi trường; screenshots/evidence; ≥2 PR được review.
- Hoàn thành khi các luồng P0 dùng được qua domain với API thật và có bằng chứng kiểm thử.

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

