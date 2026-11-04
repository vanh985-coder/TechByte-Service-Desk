-- Migration: V001__init.sql
-- Description: Khởi tạo schema cơ bản gồm 4 bảng: users, tickets, ticket_comments, ticket_history

-- ============================================================================
-- 1. BẢNG USERS
-- ============================================================================
CREATE TABLE users (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    email VARCHAR(255) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    role VARCHAR(20) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    -- Ràng buộc vai trò người dùng chỉ nhận 1 trong 3 giá trị STAFF, TECHNICIAN, ADMIN
    CONSTRAINT chk_users_role CHECK (role IN ('STAFF', 'TECHNICIAN', 'ADMIN'))
);

-- Đảm bảo email duy nhất và không phân biệt chữ hoa/thường khi xác thực tài khoản
CREATE UNIQUE INDEX uq_users_lower_email ON users (lower(email));

-- ============================================================================
-- 2. BẢNG TICKETS
-- ============================================================================
CREATE TABLE tickets (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    code VARCHAR(50) NOT NULL,
    title VARCHAR(200) NOT NULL,
    description VARCHAR(5000) NOT NULL,
    priority VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'OPEN',
    created_by BIGINT NOT NULL,
    assigned_to BIGINT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    -- Đảm bảo mã ticket là duy nhất trên toàn hệ thống phục vụ tra cứu
    CONSTRAINT uq_tickets_code UNIQUE (code),
    -- Ràng buộc mức độ ưu tiên hợp lệ: LOW, MEDIUM, HIGH, URGENT (khớp Backend Enum)
    CONSTRAINT chk_tickets_priority CHECK (priority IN ('LOW', 'MEDIUM', 'HIGH', 'URGENT')),
    -- Ràng buộc trạng thái ticket hợp lệ: OPEN, IN_PROGRESS, RESOLVED, CLOSED
    CONSTRAINT chk_tickets_status CHECK (status IN ('OPEN', 'IN_PROGRESS', 'RESOLVED', 'CLOSED')),
    -- FK lookup người tạo ticket, chặn xóa user nếu còn ticket liên kết (ON DELETE RESTRICT)
    CONSTRAINT fk_tickets_created_by FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE RESTRICT,
    -- FK lookup kỹ thuật viên xử lý, gán NULL nếu kỹ thuật viên bị xóa (ON DELETE SET NULL)
    CONSTRAINT fk_tickets_assigned_to FOREIGN KEY (assigned_to) REFERENCES users(id) ON DELETE SET NULL
);

-- Tối ưu dashboard đếm và thống kê ticket theo trạng thái
CREATE INDEX idx_tickets_status ON tickets(status);

-- Tối ưu truy vấn sắp xếp và lọc danh sách ticket theo thời gian tạo mới nhất
CREATE INDEX idx_tickets_created_at_desc ON tickets(created_at DESC);

-- Tối ưu truy vấn kết hợp lọc theo trạng thái và sắp xếp ngày tạo giảm dần
CREATE INDEX idx_tickets_status_created_at ON tickets(status, created_at DESC);

-- Tối ưu tra cứu danh sách ticket được phân công cho kỹ thuật viên (FK lookup)
CREATE INDEX idx_tickets_assigned_to ON tickets(assigned_to);

-- Tối ưu tra cứu danh sách ticket do người dùng tạo (FK lookup)
CREATE INDEX idx_tickets_created_by ON tickets(created_by);

-- ============================================================================
-- 3. BẢNG TICKET_COMMENTS
-- ============================================================================
CREATE TABLE ticket_comments (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    ticket_id BIGINT NOT NULL,
    author_id BIGINT NOT NULL,
    content VARCHAR(2000) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    -- FK lookup ticket, tự động xóa tất cả bình luận khi ticket bị xóa (ON DELETE CASCADE)
    CONSTRAINT fk_ticket_comments_ticket FOREIGN KEY (ticket_id) REFERENCES tickets(id) ON DELETE CASCADE,
    -- FK lookup tác giả bình luận, chặn xóa user nếu đã có bình luận (ON DELETE RESTRICT)
    CONSTRAINT fk_ticket_comments_author FOREIGN KEY (author_id) REFERENCES users(id) ON DELETE RESTRICT
);

-- Tối ưu tra cứu danh sách các bình luận thuộc về một ticket (FK lookup)
CREATE INDEX idx_ticket_comments_ticket_id ON ticket_comments(ticket_id);

-- ============================================================================
-- 4. BẢNG TICKET_HISTORY
-- ============================================================================
CREATE TABLE ticket_history (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    ticket_id BIGINT NOT NULL,
    actor_id BIGINT NOT NULL,
    old_status VARCHAR(20) NULL,
    new_status VARCHAR(20) NOT NULL,
    changed_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    -- FK lookup ticket, tự động xóa lịch sử trạng thái khi ticket bị xóa (ON DELETE CASCADE)
    CONSTRAINT fk_ticket_history_ticket FOREIGN KEY (ticket_id) REFERENCES tickets(id) ON DELETE CASCADE,
    -- FK lookup người thực hiện thay đổi, chặn xóa user để bảo toàn audit trail (ON DELETE RESTRICT)
    CONSTRAINT fk_ticket_history_actor FOREIGN KEY (actor_id) REFERENCES users(id) ON DELETE RESTRICT,
    -- Ràng buộc trạng thái cũ hợp lệ hoặc NULL ở lần tạo ban đầu
    CONSTRAINT chk_ticket_history_old_status CHECK (old_status IS NULL OR old_status IN ('OPEN', 'IN_PROGRESS', 'RESOLVED', 'CLOSED')),
    -- Ràng buộc trạng thái mới hợp lệ
    CONSTRAINT chk_ticket_history_new_status CHECK (new_status IN ('OPEN', 'IN_PROGRESS', 'RESOLVED', 'CLOSED'))
);

-- Tối ưu tra cứu toàn bộ lịch sử thay đổi trạng thái của một ticket (FK lookup)
CREATE INDEX idx_ticket_history_ticket_id ON ticket_history(ticket_id);

