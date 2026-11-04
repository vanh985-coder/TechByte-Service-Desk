import { useEffect, useState, useCallback } from 'react';
import { getDashboardSummary } from '../api/dashboard';
import { Loading, ErrorBox } from '../components/StateView';

export default function DashboardPage() {
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const load = useCallback(async () => {
    setLoading(true); 
    setError('');
    try { 
      const res = await getDashboardSummary(); 
      setData(res);
    } catch (e) { 
      setError(e.message); 
    } finally { 
      setLoading(false); 
    }
  }, []);

  useEffect(() => {
    let active = true;
    getDashboardSummary()
      .then(data => { if (active) setData(data); })
      .catch(error => { if (active) setError(error.message); })
      .finally(() => { if (active) setLoading(false); });
    return () => { active = false; };
  }, []);

  if (loading) return <Loading label="Đang tải số liệu..." />;
  if (error)   return <ErrorBox message={error} onRetry={load} />;

  // Mapping dữ liệu từ API trả về
  const cards = [
    { key: 'total',       label: 'Tổng ticket',  value: data?.total ?? 0,       color: '#1f2937' },
    { key: 'open',        label: 'Đang mở',      value: data?.open ?? 0,        color: '#1d4ed8' },
    { key: 'in_progress', label: 'Đang xử lý',   value: data?.in_progress ?? 0, color: '#d97706' },
    { key: 'completed',   label: 'Hoàn thành',   value: data?.completed ?? 0,   color: '#059669' },
  ];

  return (
    <section>
      <div className="page-head">
        <h1>Dashboard</h1>
        <button className="btn" onClick={load}>🔄 Làm mới</button>
      </div>
      
      <div className="grid">
        {cards.map(c => (
          <div key={c.key} className="card stat">
            <span className="stat__label">{c.label}</span>
            <span className="stat__value" style={{ color: c.color }}>{c.value}</span>
          </div>
        ))}
      </div>
    </section>
  );
}
