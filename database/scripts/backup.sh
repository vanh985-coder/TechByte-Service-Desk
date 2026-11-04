#!/usr/bin/env bash
# ==============================================================================
# Script Backup Database — TechByte Service Desk
# ==============================================================================
# Sử dụng: ./database/scripts/backup.sh [tùy_chọn_thư_mục_lưu]
# Mặc định lưu vào: database/backups/
# Yêu cầu: Container PostgreSQL "database" đang chạy
# ==============================================================================

set -euo pipefail

CONTAINER_NAME="${DB_CONTAINER:-database}"
DB_USER="${POSTGRES_USER:-postgres}"
DB_NAME="${POSTGRES_DB:-postgres}"
BACKUP_DIR="${1:-./database/backups}"

mkdir -p "$BACKUP_DIR"

TIMESTAMP=$(date +"%Y%m%d_%H%M%S")
BACKUP_FILE="${BACKUP_DIR}/techbyte_backup_${TIMESTAMP}.dump"
LOG_FILE="${BACKUP_DIR}/techbyte_backup_${TIMESTAMP}.log"

echo "=========================================================="
echo " [BACKUP] Bắt đầu sao lưu database TechByte Service Desk"
echo " Container : ${CONTAINER_NAME}"
echo " Database  : ${DB_NAME}"
echo " File đích : ${BACKUP_FILE}"
echo "=========================================================="

# Kiểm tra container có đang chạy không
if ! docker ps --format '{{.Names}}' | grep -Eq "^${CONTAINER_NAME}\$"; then
    echo "❌ Lỗi: Container '${CONTAINER_NAME}' không tồn tại hoặc chưa chạy!" >&2
    exit 1
fi

# Chạy pg_dump định dạng Custom (-Fc) có nén, hỗ trợ pg_restore linh hoạt
docker exec -t "${CONTAINER_NAME}" pg_dump \
    -U "${DB_USER}" \
    -d "${DB_NAME}" \
    -F c \
    -b \
    -v > "${BACKUP_FILE}" 2> "${LOG_FILE}"

# Kiểm tra kích thước file sao lưu
FILESIZE=$(wc -c <"${BACKUP_FILE}" | tr -d ' ')

if [ "${FILESIZE}" -gt 100 ]; then
    echo "✅ [SUCCESS] Sao lưu thành công!"
    echo " Dung lượng: ${FILESIZE} bytes"
    echo " File dump : ${BACKUP_FILE}"
    echo " File log  : ${LOG_FILE}"
else
    echo "❌ [FAILED] File sao lưu rỗng hoặc kích thước bất thường!" >&2
    cat "${LOG_FILE}" >&2
    exit 1
fi

