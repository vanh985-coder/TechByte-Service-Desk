import { useEffect, useState, useCallback } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { 
  getTicket, updateStatus, addComment, getHistory, assignTicket 
} from '../api/tickets';
import { useAuth } from '../auth/AuthContext';
import { Loading, ErrorBox } from '../components/StateView';
import { fmtDateTime, statusLabel, priorityLabel } from '../utils/format';
import { api } from '../api/client'; // Thêm dòng này để dùng api.get

export default function TicketDetailPage() {
  const { id } = useParams();
  const nav = useNavigate();
  const { user } = useAuth(); // Lấy thông tin user để phân quyền giao diện

  // State dữ liệu
  const [ticket, setTicket] = useState(null);
  const [history, setHistory] = useState([]);
  const [comments, setComments] = useState([]);
  
  // State UI
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [busy, setBusy] = useState(false); // Trạng thái đang gửi request
  const [flash, setFlash] = useState(''); // Thông báo thành công

  // State cho form bình luận
  const [commentDraft, setCommentDraft] = useState('');

  // Hàm tải toàn bộ dữ liệu của trang
  const loadData = useCallback(async () => {
    setLoading(true); setError('');
    try {
      // Chạy song song 3 API để tăng tốc độ tải trang
      const [ticketData, commentsData, historyData] = await Promise.all([
        getTicket(id),
        api.get(`/tickets/${id}/comments`).then(res => res.data).catch(() => []), // Gọi nhanh API comment
        getHistory(id).catch(() => []) // Nếu API history lỗi, trả về mảng rỗng
      ]);
      setTicket(ticketData);
      setComments(Array.isArray(commentsData) ? commentsData : commentsData.items || []);
      setHistory(Array.isArray(historyData) ? historyData : historyData.items || []);
    } catch (e) {
      setError(e.message);
    } finally {
      setLoading(false);
    }
  }, [id]);

  useEffect(() => { loadData(); }, [loadData]);

  // Xử lý đổi trạng thái
  async function handleStatusChange(e) {
    const newStatus = e.target.value;
    if (!newStatus) return;
    
    setBusy(true);
    try {
      await updateStatus(id, newStatus);
      setFlash('Cập nhật trạng thái thành công!');
      await loadData(); // Tải lại để cập nhật lịch sử
    } catch (err) {
      setFlash('Lỗi: ' + err.message);
    } finally {
      setBusy(false);
    }
  }

  // Xử lý gửi bình luận
  async function handleAddComment(e) {
    e.preventDefault();
    if (!commentDraft.trim()) return;

    setBusy(true);
    try {
      await addComment(id, commentDraft.trim());
      setCommentDraft('');
      await loadData(); // Tải lại để hiển thị bình luận mới
    } catch (err) {
      setFlash('Lỗi bình luận: ' + err.message);
    } finally {
      setBusy(false);
    }
  }

  // Xử lý phân công
  async function handleAssign(assigneeId) {
    if (!assigneeId) return;
    setBusy(true);
    try {
      await assignTicket(id, assigneeId);
      setFlash('Phân công thành công!');
      await loadData();
    } catch (err) {
      setFlash('Lỗi phân công: ' + err.message);
    } finally {
      setBusy(false);
    }
  }

  if (loading) return <Loading label="Đang tải chi tiết ticket..." />;
  if (error) return <ErrorBox message={error} onRetry={loadData} />;
  if (!ticket) return <ErrorBox message="Không tìm thấy ticket này." />;

  // Phân quyền UI (Backend vẫn phải check lại)
  const role = user?.role?.toLowerCase() || 'requester';
  const canChangeStatus = ['admin', 'agent'].includes(role);
  const canAssign = ['admin'].includes(role);

  return (
    <section>
      {/* Thông báo Flash */}
      {flash && <div className="flash">{flash}</div>}

      <div className="page-head">
        <div style={{ display: 'flex', alignItems: 'center', gap: '16px' }}>
          <button className="btn" onClick={() => nav('/tickets')}>← Quay lại</button>
          <h1 style={{ margin: 0 }}>{ticket.code || `#${ticket.id}`} - {ticket.title}</h1>
        </div>
      </div>

      <div className="detail-grid">
        
        {/* CỘT TRÁI: Thông tin & Thao tác */}
        <div className="card">
          <h2>Thông tin chung</h2>
          <dl className="kv">
            <dt>Trạng thái</dt>
            <dd>
              <span className={`badge ${ticket.status}`}>{statusLabel(ticket.status)}</span>
            </dd>
            
            <dt>Mức ưu tiên</dt>
            <dd>{priorityLabel(ticket.priority)}</dd>
            
            <dt>Người phụ trách</dt>
            <dd>{ticket.assignee?.fullName || ticket.assigneeName || 'Chưa phân công'}</dd>
            
            <dt>Ngày tạo</dt>
            <dd>{fmtDateTime(ticket.createdAt)}</dd>
          </dl>

          <h3>Mô tả</h3>
          <p className="pre" style={{ background: '#f9fafb', padding: '12px', borderRadius: '6px' }}>
            {ticket.description}
          </p>

          {/* Vùng thao tác dành cho Agent/Admin */}
          {(canChangeStatus || canAssign) && (
            <div className="actions-box">
              <h3>Thao tác</h3>
              
              {canChangeStatus && (
                <div className="form-group">
                  <label>Chuyển trạng thái:</label>
                  <select disabled={busy} onChange={handleStatusChange} value="">
                    <option value="">-- Chọn trạng thái mới --</option>
                    <option value="open">Đang mở</option>
                    <option value="in_progress">Đang xử lý</option>
                    <option value="done">Hoàn thành</option>
                  </select>
                </div>
              )}

              {canAssign && (
                <div className="form-group">
                  <label>Phân công cho (ID):</label>
                  <input 
                    type="number" 
                    placeholder="Nhập ID nhân viên..." 
                    disabled={busy}
                    onKeyDown={(e) => {
                      if (e.key === 'Enter') handleAssign(e.target.value);
                    }}
                  />
                  <span className="form-hint">Nhấn Enter để xác nhận (Tạm thời nhập ID thay vì Dropdown)</span>
                </div>
              )}
            </div>
          )}
        </div>

        {/* CỘT PHẢI: Bình luận & Lịch sử */}
        <div style={{ display: 'flex', flexDirection: 'column', gap: '24px' }}>
          
          {/* Khung Bình luận */}
          <div className="card">
            <h2>Bình luận ({comments.length})</h2>
            <div className="comment-list">
              {comments.length === 0 ? (
                <p style={{ color: '#6b7280', fontStyle: 'italic' }}>Chưa có bình luận nào.</p>
              ) : (
                comments.map(c => (
                  <div key={c.id} className="comment-item">
                    <div className="comment-header">
                      <strong>{c.author?.fullName || 'Người dùng'}</strong>
                      <span>{fmtDateTime(c.createdAt)}</span>
                    </div>
                    <p className="pre">{c.content}</p>
                  </div>
                ))
              )}
            </div>

            <form onSubmit={handleAddComment} className="comment-form">
              <textarea 
                rows={3} 
                placeholder="Viết bình luận... (Ctrl+Enter để gửi)"
                value={commentDraft}
                onChange={e => setCommentDraft(e.target.value)}
                onKeyDown={e => {
                  if (e.ctrlKey && e.key === 'Enter') handleAddComment(e);
                }}
              />
              <button type="submit" className="btn btn--primary" disabled={busy || !commentDraft.trim()}>
                Gửi bình luận
              </button>
            </form>
          </div>

          {/* Khung Lịch sử */}
          <div className="card">
            <h2>Lịch sử thay đổi</h2>
            {history.length === 0 ? (
              <p style={{ color: '#6b7280', fontStyle: 'italic' }}>Chưa có lịch sử thay đổi.</p>
            ) : (
              <ul className="timeline">
                {history.map((h, i) => (
                  <li key={h.id || i}>
                    <div className="timeline-dot"></div>
                    <div className="timeline-content">
                      <strong>{statusLabel(h.from)} → {statusLabel(h.to)}</strong>
                      <div style={{ fontSize: '13px', color: '#6b7280' }}>
                        Bởi: {h.by || h.actor?.fullName || 'Hệ thống'} lúc {fmtDateTime(h.at || h.createdAt)}
                      </div>
                    </div>
                  </li>
                ))}
              </ul>
            )}
          </div>
        </div>

      </div>
    </section>
  );
}