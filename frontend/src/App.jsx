import { Routes, Route, Navigate } from 'react-router-dom';
import ProtectedRoute from './auth/ProtectedRoute';
import Layout from './components/Layout';

// Import tất cả các trang (Pages)
import LoginPage from './pages/LoginPage';
import DashboardPage from './pages/DashboardPage';
import TicketListPage from './pages/TicketListPage';
import CreateTicketPage from './pages/CreateTicketPage';
import TicketDetailPage from './pages/TicketDetailPage';

export default function App() {
  return (
    <Routes>
      {/* 1. Trang công khai (Không cần đăng nhập) */}
      <Route path="/login" element={<LoginPage />} />

      {/* 2. Các trang yêu cầu phải đăng nhập (Protected Routes) */}
      <Route 
        element={
          <ProtectedRoute>
            <Layout />
          </ProtectedRoute>
        }
      >
        {/* Đường dẫn mặc định: / */}
        <Route index element={<DashboardPage />} />
        
        {/* Đường dẫn danh sách: /tickets */}
        <Route path="tickets" element={<TicketListPage />} />
        
        {/* Đường dẫn tạo mới: /tickets/new (Phải để TRƯỚC đường dẫn :id) */}
        <Route path="tickets/new" element={<CreateTicketPage />} />
        
        {/* Đường dẫn chi tiết: /tickets/1, /tickets/2... */}
        <Route path="tickets/:id" element={<TicketDetailPage />} />
      </Route>

      {/* 3. Chuyển hướng các đường dẫn không tồn tại về trang chủ */}
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );
}