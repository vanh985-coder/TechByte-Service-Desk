import { Navigate, useLocation } from 'react-router-dom';
import { useAuth } from './AuthContext';

export default function ProtectedRoute({ children }) {
  const { user, loading } = useAuth();
  const loc = useLocation();

  if (loading) return <div style={{ padding: 20 }}>Đang kiểm tra phiên đăng nhập...</div>;
  if (!user) return <Navigate to="/login" state={{ from: loc }} replace />;
  return children;
}