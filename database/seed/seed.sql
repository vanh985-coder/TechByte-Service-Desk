-- ============================================================================
-- Seed Data — TechByte Service Desk
-- ============================================================================
-- Chạy: psql -v demo_hash="$DEMO_HASH" -f database/seed/seed.sql
-- DEMO_HASH là chuỗi BCrypt hash hợp lệ (ví dụ hash của 'Password123!')
-- Tạo hash: htpasswd -nbBC 10 "" 'Password123!' | cut -d: -f2
-- ============================================================================

BEGIN;

-- Dọn dữ liệu cũ nếu chạy lại
TRUNCATE ticket_history, ticket_comments, tickets, users RESTART IDENTITY CASCADE;

-- ============================================================================
-- 1. USERS (8 người: 1 ADMIN, 2 TECHNICIAN, 5 STAFF)
-- ============================================================================
INSERT INTO users (id, email, password_hash, full_name, role, created_at)
OVERRIDING SYSTEM VALUE
VALUES
  (1, 'admin@techbyte.local',  :'demo_hash', 'Nguyễn Văn Admin',      'ADMIN',      now() - INTERVAL '30 days'),
  (2, 'tech1@techbyte.local',  :'demo_hash', 'Trần Thị Kỹ Thuật',    'TECHNICIAN', now() - INTERVAL '28 days'),
  (3, 'tech2@techbyte.local',  :'demo_hash', 'Lê Văn Kỹ Thuật',      'TECHNICIAN', now() - INTERVAL '27 days'),
  (4, 'staff1@techbyte.local', :'demo_hash', 'Phạm Thị Nhân Viên',   'STAFF',      now() - INTERVAL '25 days'),
  (5, 'staff2@techbyte.local', :'demo_hash', 'Hoàng Văn Nhân Viên',  'STAFF',      now() - INTERVAL '24 days'),
  (6, 'staff3@techbyte.local', :'demo_hash', 'Đỗ Thị Nhân Viên',     'STAFF',      now() - INTERVAL '23 days'),
  (7, 'staff4@techbyte.local', :'demo_hash', 'Vũ Văn Nhân Viên',     'STAFF',      now() - INTERVAL '22 days'),
  (8, 'staff5@techbyte.local', :'demo_hash', 'Bùi Thị Nhân Viên',    'STAFF',      now() - INTERVAL '21 days');

SELECT setval(pg_get_serial_sequence('users', 'id'), (SELECT MAX(id) FROM users));

-- ============================================================================
-- 2. TICKETS (20 ticket: 5 OPEN, 5 IN_PROGRESS, 5 RESOLVED, 5 CLOSED)
-- ============================================================================
-- Ghi chú: created_by = STAFF hoặc ADMIN (TECHNICIAN không tạo ticket theo PM)
--          assigned_to = TECHNICIAN (id 2 hoặc 3) hoặc NULL

INSERT INTO tickets (id, code, title, description, priority, status, created_by, assigned_to, created_at, updated_at)
OVERRIDING SYSTEM VALUE
VALUES
  -- === OPEN (5 ticket, chưa phân công) ===
  (1,  'TK-001', 'Máy in phòng kế toán không in được',
       'Máy in HP LaserJet tầng 2 phòng kế toán báo lỗi offline, đã thử khởi động lại nhưng không khắc phục được.',
       'LOW', 'OPEN', 4, NULL,
       now() - INTERVAL '5 days', now() - INTERVAL '5 days'),

  (2,  'TK-002', 'Yêu cầu cấp tài khoản email nhân viên mới',
       'Phòng nhân sự tuyển 3 nhân viên mới bắt đầu tuần sau, cần cấp email @techbyte.local cho từng người.',
       'MEDIUM', 'OPEN', 5, NULL,
       now() - INTERVAL '4 days', now() - INTERVAL '4 days'),

  (3,  'TK-003', 'Lỗi phần mềm kế toán khi xuất báo cáo',
       'Phần mềm MISA báo lỗi runtime khi xuất báo cáo tài chính quý 3, chỉ xảy ra với file lớn trên 50MB.',
       'HIGH', 'OPEN', 6, NULL,
       now() - INTERVAL '3 days', now() - INTERVAL '3 days'),

  (4,  'TK-004', 'Hệ thống mạng nội bộ bị gián đoạn',
       'Toàn bộ tầng 4 mất kết nối mạng LAN từ 14h chiều nay, switch tầng 4 đèn báo bất thường.',
       'URGENT', 'OPEN', 7, NULL,
       now() - INTERVAL '2 days', now() - INTERVAL '2 days'),

  (5,  'TK-005', 'Yêu cầu nâng cấp RAM máy trạm phòng thiết kế',
       'Máy trạm chạy AutoCAD chậm do chỉ có 8GB RAM, đề xuất nâng lên 16GB cho 5 máy.',
       'MEDIUM', 'OPEN', 1, NULL,
       now() - INTERVAL '1 day', now() - INTERVAL '1 day'),

  -- === IN_PROGRESS (5 ticket, đã phân công TECHNICIAN) ===
  (6,  'TK-006', 'Camera an ninh tầng 3 mất tín hiệu',
       'Camera hành lang tầng 3 (vị trí C3-02) mất hình từ sáng nay, đầu ghi vẫn hoạt động bình thường.',
       'MEDIUM', 'IN_PROGRESS', 4, 2,
       now() - INTERVAL '10 days', now() - INTERVAL '9 days'),

  (7,  'TK-007', 'Lỗi kết nối VPN khi làm việc từ xa',
       'Nhân viên phòng kinh doanh không kết nối được VPN từ nhà, báo lỗi authentication failed.',
       'HIGH', 'IN_PROGRESS', 5, 3,
       now() - INTERVAL '9 days', now() - INTERVAL '8 days'),

  (8,  'TK-008', 'Yêu cầu cài đặt phần mềm diệt virus',
       'Cần cài Kaspersky Endpoint Security cho 10 máy mới nhập về phòng hành chính.',
       'LOW', 'IN_PROGRESS', 6, 2,
       now() - INTERVAL '8 days', now() - INTERVAL '7 days'),

  (9,  'TK-009', 'Server email nội bộ phản hồi chậm',
       'Mail server nội bộ delay gửi/nhận 5-10 phút, ảnh hưởng toàn công ty từ 2 ngày nay.',
       'URGENT', 'IN_PROGRESS', 8, 3,
       now() - INTERVAL '7 days', now() - INTERVAL '6 days'),

  (10, 'TK-010', 'Chia sẻ thư mục dùng chung trên NAS',
       'Phòng marketing cần thư mục chia sẻ trên NAS Synology để lưu tài liệu dự án, phân quyền theo nhóm.',
       'MEDIUM', 'IN_PROGRESS', 7, 2,
       now() - INTERVAL '6 days', now() - INTERVAL '5 days'),

  -- === RESOLVED (5 ticket, đã xử lý xong) ===
  (11, 'TK-011', 'Máy chiếu phòng họp lớn hư bóng đèn',
       'Máy chiếu Epson phòng họp A bị tối, bóng đèn đã hết tuổi thọ cần thay mới.',
       'HIGH', 'RESOLVED', 4, 2,
       now() - INTERVAL '15 days', now() - INTERVAL '12 days'),

  (12, 'TK-012', 'Reset mật khẩu hệ thống nhân sự',
       'Nhân viên Nguyễn Thị B quên mật khẩu hệ thống HRM, cần reset và gửi lại qua email cá nhân.',
       'LOW', 'RESOLVED', 5, 3,
       now() - INTERVAL '14 days', now() - INTERVAL '11 days'),

  (13, 'TK-013', 'Cập nhật firmware thiết bị mạng',
       'Switch Cisco tầng 1 và 2 cần cập nhật firmware lên phiên bản mới nhất để vá lỗi bảo mật.',
       'MEDIUM', 'RESOLVED', 1, 2,
       now() - INTERVAL '13 days', now() - INTERVAL '10 days'),

  (14, 'TK-014', 'Máy chủ backup báo đầy dung lượng',
       'Server backup Veeam báo ổ cứng đạt 95%, cần dọn dẹp hoặc mở rộng dung lượng lưu trữ gấp.',
       'URGENT', 'RESOLVED', 6, 3,
       now() - INTERVAL '12 days', now() - INTERVAL '9 days'),

  (15, 'TK-015', 'Lỗi phần mềm CRM tìm kiếm khách hàng',
       'Chức năng tìm kiếm trong CRM trả về kết quả không chính xác khi lọc theo ngày giao dịch.',
       'HIGH', 'RESOLVED', 8, 2,
       now() - INTERVAL '11 days', now() - INTERVAL '8 days'),

  -- === CLOSED (5 ticket, đã đóng hoàn toàn) ===
  (16, 'TK-016', 'Thay bàn phím và chuột phòng IT',
       'Bàn phím 3 máy trong phòng IT bị liệt phím, chuột 2 máy bị hỏng nút trái, cần thay mới.',
       'MEDIUM', 'CLOSED', 4, 2,
       now() - INTERVAL '20 days', now() - INTERVAL '15 days'),

  (17, 'TK-017', 'Hỗ trợ cấu hình máy in mạng tầng 2',
       'Máy in Canon iR2625 mới lắp tầng 2 cần cấu hình in qua mạng cho cả phòng.',
       'LOW', 'CLOSED', 7, 3,
       now() - INTERVAL '19 days', now() - INTERVAL '14 days'),

  (18, 'TK-018', 'Lỗi ứng dụng nội bộ sau cập nhật',
       'Hệ thống quản lý kho báo lỗi 500 sau khi cập nhật phiên bản 2.3.1 sáng nay.',
       'HIGH', 'CLOSED', 5, 2,
       now() - INTERVAL '18 days', now() - INTERVAL '13 days'),

  (19, 'TK-019', 'Sự cố mất điện gây hỏng ổ cứng server',
       'Mất điện đột ngột lúc 3h sáng khiến 1 ổ HDD trong RAID 5 server file bị bad sector.',
       'URGENT', 'CLOSED', 8, 3,
       now() - INTERVAL '17 days', now() - INTERVAL '12 days'),

  (20, 'TK-020', 'Mở port firewall cho ứng dụng mới',
       'Phòng phát triển cần mở port 8443 trên firewall nội bộ để triển khai ứng dụng mới lên staging.',
       'MEDIUM', 'CLOSED', 6, 2,
       now() - INTERVAL '16 days', now() - INTERVAL '11 days'));

SELECT setval(pg_get_serial_sequence('tickets', 'id'), (SELECT MAX(id) FROM tickets));

-- ============================================================================
-- 3. TICKET_HISTORY (khớp trạng thái hiện tại của từng ticket)
-- ============================================================================
-- Quy tắc: Mỗi ticket tạo ra phải có bản ghi đầu tiên (NULL → OPEN).
-- Số bản ghi history tương ứng với số lần chuyển trạng thái.

INSERT INTO ticket_history (ticket_id, actor_id, old_status, new_status, changed_at)
VALUES
  -- === OPEN tickets (1 history mỗi ticket: NULL → OPEN) ===
  (1,  4, NULL,          'OPEN',        now() - INTERVAL '5 days'),
  (2,  5, NULL,          'OPEN',        now() - INTERVAL '4 days'),
  (3,  6, NULL,          'OPEN',        now() - INTERVAL '3 days'),
  (4,  7, NULL,          'OPEN',        now() - INTERVAL '2 days'),
  (5,  1, NULL,          'OPEN',        now() - INTERVAL '1 day'),

  -- === IN_PROGRESS tickets (2 history mỗi ticket) ===
  (6,  4, NULL,          'OPEN',        now() - INTERVAL '10 days'),
  (6,  1, 'OPEN',        'IN_PROGRESS', now() - INTERVAL '9 days'),

  (7,  5, NULL,          'OPEN',        now() - INTERVAL '9 days'),
  (7,  1, 'OPEN',        'IN_PROGRESS', now() - INTERVAL '8 days'),

  (8,  6, NULL,          'OPEN',        now() - INTERVAL '8 days'),
  (8,  1, 'OPEN',        'IN_PROGRESS', now() - INTERVAL '7 days'),

  (9,  8, NULL,          'OPEN',        now() - INTERVAL '7 days'),
  (9,  1, 'OPEN',        'IN_PROGRESS', now() - INTERVAL '6 days'),

  (10, 7, NULL,          'OPEN',        now() - INTERVAL '6 days'),
  (10, 1, 'OPEN',        'IN_PROGRESS', now() - INTERVAL '5 days'),

  -- === RESOLVED tickets (3 history mỗi ticket) ===
  (11, 4, NULL,          'OPEN',        now() - INTERVAL '15 days'),
  (11, 1, 'OPEN',        'IN_PROGRESS', now() - INTERVAL '14 days'),
  (11, 2, 'IN_PROGRESS', 'RESOLVED',    now() - INTERVAL '12 days'),

  (12, 5, NULL,          'OPEN',        now() - INTERVAL '14 days'),
  (12, 1, 'OPEN',        'IN_PROGRESS', now() - INTERVAL '13 days'),
  (12, 3, 'IN_PROGRESS', 'RESOLVED',    now() - INTERVAL '11 days'),

  (13, 1, NULL,          'OPEN',        now() - INTERVAL '13 days'),
  (13, 1, 'OPEN',        'IN_PROGRESS', now() - INTERVAL '12 days'),
  (13, 2, 'IN_PROGRESS', 'RESOLVED',    now() - INTERVAL '10 days'),

  (14, 6, NULL,          'OPEN',        now() - INTERVAL '12 days'),
  (14, 1, 'OPEN',        'IN_PROGRESS', now() - INTERVAL '11 days'),
  (14, 3, 'IN_PROGRESS', 'RESOLVED',    now() - INTERVAL '9 days'),

  (15, 8, NULL,          'OPEN',        now() - INTERVAL '11 days'),
  (15, 1, 'OPEN',        'IN_PROGRESS', now() - INTERVAL '10 days'),
  (15, 2, 'IN_PROGRESS', 'RESOLVED',    now() - INTERVAL '8 days'),

  -- === CLOSED tickets (4 history mỗi ticket) ===
  (16, 4, NULL,          'OPEN',        now() - INTERVAL '20 days'),
  (16, 1, 'OPEN',        'IN_PROGRESS', now() - INTERVAL '19 days'),
  (16, 2, 'IN_PROGRESS', 'RESOLVED',    now() - INTERVAL '17 days'),
  (16, 1, 'RESOLVED',    'CLOSED',      now() - INTERVAL '15 days'),

  (17, 7, NULL,          'OPEN',        now() - INTERVAL '19 days'),
  (17, 1, 'OPEN',        'IN_PROGRESS', now() - INTERVAL '18 days'),
  (17, 3, 'IN_PROGRESS', 'RESOLVED',    now() - INTERVAL '16 days'),
  (17, 1, 'RESOLVED',    'CLOSED',      now() - INTERVAL '14 days'),

  (18, 5, NULL,          'OPEN',        now() - INTERVAL '18 days'),
  (18, 1, 'OPEN',        'IN_PROGRESS', now() - INTERVAL '17 days'),
  (18, 2, 'IN_PROGRESS', 'RESOLVED',    now() - INTERVAL '15 days'),
  (18, 1, 'RESOLVED',    'CLOSED',      now() - INTERVAL '13 days'),

  (19, 8, NULL,          'OPEN',        now() - INTERVAL '17 days'),
  (19, 1, 'OPEN',        'IN_PROGRESS', now() - INTERVAL '16 days'),
  (19, 3, 'IN_PROGRESS', 'RESOLVED',    now() - INTERVAL '14 days'),
  (19, 1, 'RESOLVED',    'CLOSED',      now() - INTERVAL '12 days'),

  (20, 6, NULL,          'OPEN',        now() - INTERVAL '16 days'),
  (20, 1, 'OPEN',        'IN_PROGRESS', now() - INTERVAL '15 days'),
  (20, 2, 'IN_PROGRESS', 'RESOLVED',    now() - INTERVAL '13 days'),
  (20, 1, 'RESOLVED',    'CLOSED',      now() - INTERVAL '11 days');

-- ============================================================================
-- 4. TICKET_COMMENTS (10 bình luận)
-- ============================================================================
-- Quy tắc RBAC: STAFF comment trên ticket mình tạo, TECHNICIAN trên ticket được giao, ADMIN trên tất cả.

INSERT INTO ticket_comments (ticket_id, author_id, content, created_at)
VALUES
  -- Ticket 6 (IN_PROGRESS, created_by=4 STAFF, assigned_to=2 TECH)
  (6,  4, 'Camera bị mất tín hiệu từ khoảng 8h sáng, đã kiểm tra dây cáp bên ngoài vẫn bình thường.',
       now() - INTERVAL '9 days 6 hours'),
  (6,  2, 'Đã kiểm tra đầu ghi và phát hiện port 3 bị lỗi. Đang chuyển sang port dự phòng.',
       now() - INTERVAL '9 days 2 hours'),

  -- Ticket 7 (IN_PROGRESS, created_by=5 STAFF, assigned_to=3 TECH)
  (7,  3, 'Đã kiểm tra VPN server, certificate sắp hết hạn. Đang tiến hành gia hạn.',
       now() - INTERVAL '8 days 4 hours'),

  -- Ticket 11 (RESOLVED, created_by=4 STAFF, assigned_to=2 TECH)
  (11, 4, 'Bóng đèn máy chiếu đã yếu dần từ tuần trước, hôm nay hoàn toàn tối.',
       now() - INTERVAL '14 days 6 hours'),
  (11, 2, 'Đã thay bóng đèn mới, máy chiếu hoạt động bình thường. Kiểm tra độ sáng đạt yêu cầu.',
       now() - INTERVAL '12 days 3 hours'),

  -- Ticket 14 (RESOLVED, created_by=6 STAFF, assigned_to=3 TECH)
  (14, 1, 'Đây là vấn đề ưu tiên cao, cần xử lý trước khi hết dung lượng hoàn toàn. Đã thông báo ban giám đốc.',
       now() - INTERVAL '11 days 5 hours'),

  -- Ticket 16 (CLOSED, created_by=4 STAFF, assigned_to=2 TECH)
  (16, 4, 'Bàn phím máy số 2 và 5 bị liệt phím Enter và Space, chuột máy 3 và 7 hỏng nút trái.',
       now() - INTERVAL '19 days 6 hours'),
  (16, 2, 'Đã thay toàn bộ bàn phím và chuột theo yêu cầu. Nhân viên xác nhận hoạt động tốt.',
       now() - INTERVAL '17 days 2 hours'),

  -- Ticket 19 (CLOSED, created_by=8 STAFF, assigned_to=3 TECH)
  (19, 3, 'Ổ HDD slot 3 trong RAID 5 bị bad sector, đã thay ổ mới và rebuild RAID thành công.',
       now() - INTERVAL '14 days 4 hours'),

  -- Ticket 20 (CLOSED, created_by=6 STAFF, assigned_to=2 TECH)
  (20, 1, 'Port 8443 đã được mở trên firewall nội bộ, giới hạn truy cập từ VLAN phát triển. Đã xác minh kết nối.',
       now() - INTERVAL '12 days 3 hours');

COMMIT;

-- ============================================================================
-- Kiểm tra nhanh sau khi seed
-- ============================================================================
SELECT 'users' AS bang, count(*) AS so_luong FROM users
UNION ALL SELECT 'tickets', count(*) FROM tickets
UNION ALL SELECT 'ticket_comments', count(*) FROM ticket_comments
UNION ALL SELECT 'ticket_history', count(*) FROM ticket_history;

SELECT status, count(*) AS so_luong FROM tickets GROUP BY status ORDER BY status;

