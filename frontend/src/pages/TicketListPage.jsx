import { useEffect, useState, useCallback } from 'react';
import { Link, useLocation } from 'react-router-dom';
import { listTickets } from '../api/tickets';
import { Loading, Empty, ErrorBox } from '../components/StateView';
import { fmtDateTime, statusLabel, priorityLabel } from '../utils/format';

export default function TicketListPage() {
  // Lấy thông báo (flash) nếu được chuyển hướng từ trang Tạo Ticket sang
  const location = useLocation();
  const [flash, setFlash] = useState(location.state?.flash || '');

  // State cho bộ lọc và dữ liệu
  const [search, setSearch] = useState('');
  const [status, setStatus] = useState('');
  const [data, setData] = useState({ items: [] });
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  // Tự động ẩn thông báo flash sau 3 giây
  useEffect(() => {
    if (flash) {
      const timer = setTimeout(() => setFlash(''), 3000);
      return () => clearTimeout(timer);
    }
  }, [flash]);

  // Hàm gọi API lấy danh sách (có xử lý debounce)
  const loadData = useCallback(async () => {
    setLoading(true); 
    setError('');
    try { 
      const res = await listTickets({ search, status }); 
      setData(res);
    } catch (e) { 
      setError(e.message); 
    } finally { 
      setLoading(false); 
    }
  }, [search, status]);

  // Debounce: Đợi người dùng ngừng gõ 300ms mới gọi API
  useEffect(() => {
    const t = setTimeout(() => {
      loadData();
    }, 300);
    return () => clearTimeout(t);
  }, [loadData]);

  // Xử lý dữ liệu trả về (phòng trường hợp API trả về mảng thuần thay vì object)
  const items = Array.isArray(data) ? data : data.items || [];

  return (
    <section>
      {/* 1. Hiển thị thông báo thành công */}
      {flash && (
        <div className="flash" style={{ background: '#d1fae5', color: '#065f46', padding: '12px', borderRadius: '6px', marginBottom: '16px' }}>
          ✅ {flash}
        </div>
      )}

      {/* 2. Tiêu đề & Nút tạo mới */}
      <div className="page-head">
        <h1>Danh sách ticket</h1>
        <Link to="/tickets/new" className="btn btn--primary">
          + Tạo ticket
        </Link>
      </div>

      {/* 3. Bộ lọc & Tìm kiếm */}
      <div className="filters">
        <input 
          placeholder="🔍 Tìm theo mã hoặc tiêu đề…" 
          value={search} 
          onChange={e => setSearch(e.target.value)}
        />
        <select value={status} onChange={e => setStatus(e.target.value)}>
          <option value="">Tất cả trạng thái</option>
          <option value="open">Đang mở</option>
          <option value="in_progress">Đang xử lý</option>
          <option value="done">Hoàn thành</option>
        </select>
      </div>

      {/* 4. Xử lý các trạng thái Loading / Error / Empty */}
      {loading && <Loading label="Đang tải danh sách..." />}
      
      {!loading && error && (
        <ErrorBox message={error} onRetry={loadData} />
      )}
      
      {!loading && !error && items.length === 0 && (
        <Empty label="Không có ticket nào phù hợp với tìm kiếm." />
      )}

      {/* 5. Hiển thị Bảng dữ liệu */}
      {!loading && !error && items.length > 0 && (
        <div style={{ overflowX: 'auto' }}>
          <table className="table">
            <thead>
              <tr>
                <th>Mã</th>
                <th>Tiêu đề</th>
                <th>Ưu tiên</th>
                <th>Trạng thái</th>
                <th>Người phụ trách</th>
                <th>Thời gian cập nhật</th>
              </tr>
            </thead>
            <tbody>
              {items.map(t => (
                <tr key={t.id}>
                  <td>
                    <Link to={`/tickets/${t.id}`} style={{ fontWeight: 600 }}>
                      {t.code || `#${t.id}`}
                    </Link>
                  </td>
                  <td>{t.title}</td>
                  <td>{priorityLabel(t.priority)}</td>
                  <td>
                    <span className={`badge ${t.status}`}>
                      {statusLabel(t.status)}
                    </span>
                  </td>
                  <td>{t.assignee?.fullName || t.assigneeName || '—'}</td>
                  <td>{fmtDateTime(t.updatedAt)}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </section>
  );
}