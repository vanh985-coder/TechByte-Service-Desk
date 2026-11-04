export function fmtDateTime(iso) {
  if (!iso) return '—';
  const d = new Date(iso);
  if (Number.isNaN(d.getTime())) return '—';
  return d.toLocaleString('vi-VN', { dateStyle: 'short', timeStyle: 'short' });
}

const STATUS_MAP = { open: 'Đang mở', in_progress: 'Đang xử lý', done: 'Hoàn thành' };
export const statusLabel = (s) => STATUS_MAP[s] || s || '—';

const PRIORITY_MAP = { low: 'Thấp', medium: 'Trung bình', high: 'Cao', urgent: 'Khẩn cấp' };
export const priorityLabel = (p) => PRIORITY_MAP[p] || p || '—';