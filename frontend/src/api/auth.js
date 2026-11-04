import { api } from './client.js';
import { userFromApi } from './contract.js';

export async function login(email, password) {
  const { data } = await api.post('/auth/login', { email: email.trim(), password });
  if (!data.accessToken || !data.user) throw new Error('Phản hồi đăng nhập không hợp lệ.');
  localStorage.setItem('token', data.accessToken);
  return userFromApi(data.user);
}

export async function logout() {
  try { await api.post('/auth/logout'); } catch { /* bỏ qua */ }
  localStorage.removeItem('token');
}

export async function getMe() {
  // Sửa từ '/auth/me' thành '/me' theo đúng contract
  const { data } = await api.get('/me');
  return userFromApi(data);
}
