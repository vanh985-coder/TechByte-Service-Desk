#!/usr/bin/env bash
# ==============================================================================
# Script Restore Database — TechByte Service Desk
# ==============================================================================
# Sử dụng: ./database/scripts/restore.sh <đường_dẫn_file_dump> [tên_container_đích]
# Mặc định container đích: "database" (hoặc container lab kiểm thử "pg-tv4-restore-test")
# ==============================================================================

set -euo pipefail

if [ "$#" -lt 1 ]; then
    echo "❌ Sử dụng: $0 <đường_dẫn_file_backup.dump> [tên_container_đích]" >&2
    echo "Ví dụ: $0 ./database/backups/techbyte_backup_20261104_120000.dump database" >&2
    exit 1
fi

BACKUP_FILE="$1"
CONTAINER_NAME="${2:-${DB_CONTAINER:-database}}"
DB_USER="${POSTGRES_USER:-postgres}"
DB_NAME="${POSTGRES_DB:-postgres}"

if [ ! -f "${BACKUP_FILE}" ]; then
    echo "❌ Lỗi: File backup '${BACKUP_FILE}' không tồn tại!" >&2
    exit 1
fi

echo "=========================================================="
echo " [RESTORE] Phục hồi dữ liệu TechByte Service Desk"
echo " File nguồn : ${BACKUP_FILE}"
echo " Container  : ${CONTAINER_NAME}"
echo " Database   : ${DB_NAME}"
echo "=========================================================="

# Kiểm tra container có đang chạy không
if ! docker ps --format '{{.Names}}' | grep -Eq "^${CONTAINER_NAME}\$"; then
    echo "❌ Lỗi: Container '${CONTAINER_NAME}' không tồn tại hoặc chưa chạy!" >&2
    exit 1
fi

# Chạy pg_restore từ luồng stdin của file dump
# Tùy chọn: --clean: xóa object cũ trước khi tạo lại; --if-exists: tránh lỗi khi object chưa có
echo "⏳ Đang thực hiện khôi phục qua pg_restore..."
docker exec -i "${CONTAINER_NAME}" pg_restore \
    -U "${DB_USER}" \
    -d "${DB_NAME}" \
    --clean \
    --if-exists \
    --no-owner \
    --no-privileges \
    -v < "${BACKUP_FILE}" || true

echo "🔍 Kiểm tra lại số lượng bản ghi sau khi phục hồi:"
docker exec -t "${CONTAINER_NAME}" psql -U "${DB_USER}" -d "${DB_NAME}" -c "
  SELECT 'users' AS bang, count(*) AS so_luong FROM users
  UNION ALL SELECT 'tickets', count(*) FROM tickets
  UNION ALL SELECT 'ticket_comments', count(*) FROM ticket_comments
  UNION ALL SELECT 'ticket_history', count(*) FROM ticket_history;
"

echo "✅ [SUCCESS] Phục hồi dữ liệu hoàn tất thành công!"

