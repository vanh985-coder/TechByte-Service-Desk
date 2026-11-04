import axios from 'axios';

export const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  timeout: 15000,
});

// Gắn token vào mọi request
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});

// Xử lý lỗi và hết phiên (401)
api.interceptors.response.use(
  (res) => res,
  (err) => {
    const status = err.response?.status;
    const url = err.config?.url || '';
    const isLoginCall = url.includes('/auth/login');
    const onLoginPage = window.location.pathname === '/login';

    if (status === 401 && !isLoginCall && !onLoginPage) {
      localStorage.removeItem('token');
      window.location.assign('/login');
    }

    const message =
      err.response?.data?.message ||
      err.response?.data?.error ||
      (status === 403 ? 'Bạn không có quyền thực hiện thao tác này.' :
       status === 404 ? 'Không tìm thấy dữ liệu.' :
       status >= 500  ? 'Máy chủ đang gặp sự cố, vui lòng thử lại sau.' :
       'Không kết nối được máy chủ.');

    return Promise.reject({ status, message, raw: err });
  }
);