-- ============================================================================
-- Constraint Tests — TechByte Service Desk
-- ============================================================================
-- Chạy SAU KHI đã load V001__init.sql và seed.sql
-- Mỗi test cố tình vi phạm ràng buộc → kỳ vọng bị chặn (PASS)
-- Kết quả: PASS = ràng buộc hoạt động đúng, FAIL = dữ liệu xấu lọt qua
-- ============================================================================

-- --------------------------------------------------------------------------
-- Test 1: Email trùng (case-insensitive)
-- Kỳ vọng: unique_violation vì admin@techbyte.local đã tồn tại trong seed
-- --------------------------------------------------------------------------
DO $$
BEGIN
  INSERT INTO users (email, password_hash, full_name, role)
  VALUES ('ADMIN@TECHBYTE.LOCAL', 'fakehash', 'Test Trùng Email', 'STAFF');
  RAISE NOTICE 'FAIL: [Test 1] Email trùng case-insensitive — lọt';
EXCEPTION WHEN unique_violation THEN
  RAISE NOTICE 'PASS: [Test 1] Email trùng case-insensitive — bị chặn';
END $$;

-- --------------------------------------------------------------------------
-- Test 2: FK created_by — user không tồn tại
-- Kỳ vọng: foreign_key_violation vì user 999999 không có
-- --------------------------------------------------------------------------
DO $$
BEGIN
  INSERT INTO tickets (code, title, description, priority, status, created_by)
  VALUES ('TK-ERR1', 'Test FK', 'Test', 'LOW', 'OPEN', 999999);
  RAISE NOTICE 'FAIL: [Test 2] FK created_by user không tồn tại — lọt';
EXCEPTION WHEN foreign_key_violation THEN
  RAISE NOTICE 'PASS: [Test 2] FK created_by user không tồn tại — bị chặn';
END $$;

-- --------------------------------------------------------------------------
-- Test 3: FK assigned_to — user không tồn tại
-- Kỳ vọng: foreign_key_violation vì user 888888 không có
-- --------------------------------------------------------------------------
DO $$
BEGIN
  INSERT INTO tickets (code, title, description, priority, status, created_by, assigned_to)
  VALUES ('TK-ERR2', 'Test FK', 'Test', 'LOW', 'OPEN', 1, 888888);
  RAISE NOTICE 'FAIL: [Test 3] FK assigned_to user không tồn tại — lọt';
EXCEPTION WHEN foreign_key_violation THEN
  RAISE NOTICE 'PASS: [Test 3] FK assigned_to user không tồn tại — bị chặn';
END $$;

-- --------------------------------------------------------------------------
-- Test 4: Role không hợp lệ
-- Kỳ vọng: check_violation vì 'MANAGER' không nằm trong CHECK
-- --------------------------------------------------------------------------
DO $$
BEGIN
  INSERT INTO users (email, password_hash, full_name, role)
  VALUES ('test_role@techbyte.local', 'fakehash', 'Test Role', 'MANAGER');
  RAISE NOTICE 'FAIL: [Test 4] Role không hợp lệ — lọt';
EXCEPTION WHEN check_violation THEN
  RAISE NOTICE 'PASS: [Test 4] Role không hợp lệ — bị chặn';
END $$;

-- --------------------------------------------------------------------------
-- Test 5: Status không hợp lệ
-- Kỳ vọng: check_violation vì 'PENDING' không nằm trong CHECK
-- --------------------------------------------------------------------------
DO $$
BEGIN
  INSERT INTO tickets (code, title, description, priority, status, created_by)
  VALUES ('TK-ERR3', 'Test Status', 'Test', 'LOW', 'PENDING', 1);
  RAISE NOTICE 'FAIL: [Test 5] Status không hợp lệ — lọt';
EXCEPTION WHEN check_violation THEN
  RAISE NOTICE 'PASS: [Test 5] Status không hợp lệ — bị chặn';
END $$;

-- --------------------------------------------------------------------------
-- Test 6: Priority không hợp lệ
-- Kỳ vọng: check_violation vì 'INVALID_PRIORITY' không nằm trong CHECK
-- --------------------------------------------------------------------------
DO $$
BEGIN
  INSERT INTO tickets (code, title, description, priority, status, created_by)
  VALUES ('TK-ERR4', 'Test Priority', 'Test', 'INVALID_PRIORITY', 'OPEN', 1);
  RAISE NOTICE 'FAIL: [Test 6] Priority không hợp lệ — lọt';
EXCEPTION WHEN check_violation THEN
  RAISE NOTICE 'PASS: [Test 6] Priority không hợp lệ — bị chặn';
END $$;

-- --------------------------------------------------------------------------
-- Test 7: Title NULL
-- Kỳ vọng: not_null_violation vì title là NOT NULL
-- --------------------------------------------------------------------------
DO $$
BEGIN
  INSERT INTO tickets (code, title, description, priority, status, created_by)
  VALUES ('TK-ERR5', NULL, 'Test', 'LOW', 'OPEN', 1);
  RAISE NOTICE 'FAIL: [Test 7] Title NULL — lọt';
EXCEPTION WHEN not_null_violation THEN
  RAISE NOTICE 'PASS: [Test 7] Title NULL — bị chặn';
END $$;

-- --------------------------------------------------------------------------
-- Test 8: Bình luận vào ticket không tồn tại
-- Kỳ vọng: foreign_key_violation vì ticket 999999 không có
-- --------------------------------------------------------------------------
DO $$
BEGIN
  INSERT INTO ticket_comments (ticket_id, author_id, content)
  VALUES (999999, 1, 'Bình luận vào ticket không tồn tại');
  RAISE NOTICE 'FAIL: [Test 8] FK comment → ticket không tồn tại — lọt';
EXCEPTION WHEN foreign_key_violation THEN
  RAISE NOTICE 'PASS: [Test 8] FK comment → ticket không tồn tại — bị chặn';
END $$;

-- --------------------------------------------------------------------------
-- Test 9: FK author_id comment — user không tồn tại
-- Kỳ vọng: foreign_key_violation vì user 777777 không có
-- --------------------------------------------------------------------------
DO $$
BEGIN
  INSERT INTO ticket_comments (ticket_id, author_id, content)
  VALUES (1, 777777, 'Bình luận từ user không tồn tại');
  RAISE NOTICE 'FAIL: [Test 9] FK author_id user không tồn tại — lọt';
EXCEPTION WHEN foreign_key_violation THEN
  RAISE NOTICE 'PASS: [Test 9] FK author_id user không tồn tại — bị chặn';
END $$;

-- --------------------------------------------------------------------------
-- Test 10: History new_status không hợp lệ
-- Kỳ vọng: check_violation vì 'CANCELLED' không nằm trong CHECK
-- --------------------------------------------------------------------------
DO $$
BEGIN
  INSERT INTO ticket_history (ticket_id, actor_id, old_status, new_status)
  VALUES (1, 1, 'OPEN', 'CANCELLED');
  RAISE NOTICE 'FAIL: [Test 10] History new_status không hợp lệ — lọt';
EXCEPTION WHEN check_violation THEN
  RAISE NOTICE 'PASS: [Test 10] History new_status không hợp lệ — bị chặn';
END $$;

-- ============================================================================
-- Tổng kết
-- ============================================================================
DO $$
BEGIN
  RAISE NOTICE '=== Hoàn thành 10 constraint tests ===';
  RAISE NOTICE 'Tất cả PASS = Schema bảo vệ dữ liệu đúng thiết kế.';
  RAISE NOTICE 'Có FAIL = Cần kiểm tra lại constraint trong migration.';
END $$;

