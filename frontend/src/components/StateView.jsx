export function Loading({ label = 'Đang tải…' }) {
  return <div style={{ padding: 20, textAlign: 'center' }}>⏳ {label}</div>;
}

export function Empty({ label = 'Chưa có dữ liệu.' }) {
  return <div style={{ padding: 20, textAlign: 'center' }}>📭 {label}</div>;
}

export function ErrorBox({ message, onRetry }) {
  return (
    <div style={{ padding: 20, textAlign: 'center', color: 'red' }}>
      ⚠️ {message || 'Đã có lỗi xảy ra.'}
      {onRetry && <button onClick={onRetry} style={{ marginLeft: 10 }}>Thử lại</button>}
    </div>
  );
}