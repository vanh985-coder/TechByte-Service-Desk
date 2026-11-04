import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { createTicket } from '../api/tickets';
import { useAuth } from '../auth/context';

const PRIORITIES = [
  { value: 'low', label: 'Thấp' },
  { value: 'medium', label: 'Trung bình' },
  { value: 'high', label: 'Cao' },
  { value: 'urgent', label: 'Khẩn cấp' },
];

export default function CreateTicketPage() {
  const nav = useNavigate();
  const { user } = useAuth();
  
  // State lưu dữ liệu nhập vào
  const [title, setTitle] = useState('');
  const [description, setDescription] = useState('');
  const [priority, setPriority] = useState('medium');
  
  // State xử lý lỗi và loading
  const [errors, setErrors] = useState({});
  const [serverErr, setServerErr] = useState('');
  const [submitting, setSubmitting] = useState(false);

  // Hàm kiểm tra dữ liệu đầu vào (Validation)
  function validate() {
    const e = {};
    const t = title.trim();
    const d = description.trim();

    if (!t) e.title = 'Tiêu đề không được để trống.';
    else if (t.length > 200) e.title = 'Tiêu đề tối đa 200 ký tự.';

    if (!d) e.description = 'Mô tả không được để trống.';
    else if (d.length > 2000) e.description = 'Mô tả tối đa 2000 ký tự.';

    setErrors(e);
    return Object.keys(e).length === 0; // Trả về true nếu không có lỗi
  }

  // Hàm xử lý khi bấm nút Tạo
  async function onSubmit(ev) {
    ev.preventDefault();
    setServerErr('');
    
    if (!validate()) return; // Dừng lại nếu có lỗi validate

    setSubmitting(true);
    try {
      // Gọi API thật (hoặc mock)
      await createTicket({
        title: title.trim(), 
        description: description.trim(), 
        priority 
      });
      
      // Thành công: Chuyển hướng về danh sách kèm thông báo
      nav('/tickets', { state: { flash: 'Tạo ticket thành công!' } });
      
    } catch (err) {
      // Lỗi từ server (400, 500...)
      setServerErr(err.message || 'Có lỗi xảy ra khi tạo ticket.');
    } finally {
      setSubmitting(false);
    }
  }

  if (!['ADMIN', 'STAFF'].includes(user?.role)) {
    return <p className="err">Bạn không có quyền tạo ticket.</p>;
  }

  return (
    <section>
      <div className="page-head">
        <h1>Tạo ticket mới</h1>
      </div>

      <div className="card" style={{ maxWidth: '700px' }}>
        <form onSubmit={onSubmit} noValidate>
          
          {/* Ô nhập Tiêu đề */}
          <div className="form-group">
            <label>Tiêu đề <span style={{ color: 'red' }}>*</span></label>
            <input 
              type="text"
              value={title} 
              onChange={e => setTitle(e.target.value)} 
              placeholder="Nhập tiêu đề ngắn gọn..."
              maxLength={200}
            />
            <div className="form-hint">
              <span>{title.length}/200 ký tự</span>
            </div>
            {errors.title && <p className="err">{errors.title}</p>}
          </div>

          {/* Ô nhập Mô tả */}
          <div className="form-group">
            <label>Mô tả chi tiết <span style={{ color: 'red' }}>*</span></label>
            <textarea 
              rows={6} 
              value={description} 
              onChange={e => setDescription(e.target.value)}
              placeholder="Mô tả chi tiết sự cố hoặc yêu cầu..."
              maxLength={2000}
            />
            <div className="form-hint">
              <span>{description.length}/2000 ký tự</span>
            </div>
            {errors.description && <p className="err">{errors.description}</p>}
          </div>

          {/* Ô chọn Ưu tiên */}
          <div className="form-group">
            <label>Mức độ ưu tiên <span style={{ color: 'red' }}>*</span></label>
            <select value={priority} onChange={e => setPriority(e.target.value)}>
              {PRIORITIES.map(p => (
                <option key={p.value} value={p.value}>{p.label}</option>
              ))}
            </select>
          </div>

          {/* Hiển thị lỗi từ Server (nếu có) */}
          {serverErr && (
            <div className="err" style={{ background: '#fef2f2', padding: '10px', borderRadius: '4px', marginBottom: '16px' }}>
              ⚠️ {serverErr}
            </div>
          )}

          {/* Nút hành động */}
          <div className="actions">
            <button type="button" className="btn" onClick={() => nav(-1)} disabled={submitting}>
              Huỷ
            </button>
            <button type="submit" className="btn btn--primary" disabled={submitting}>
              {submitting ? 'Đang gửi...' : 'Tạo Ticket'}
            </button>
          </div>

        </form>
      </div>
    </section>
  );
}
