import { api } from './client';

export async function login(username, password) {
  const { data } = await api.post('/auth/login', { username, password });
  if (data.token) localStorage.setItem('token', data.token);
  return data.user ?? data;
}

export async function logout() {
  try { await api.post('/auth/logout'); } catch { /* bỏ qua */ }
  localStorage.removeItem('token');
}

export async function getMe() {
  // Sửa từ '/auth/me' thành '/me' theo đúng contract
  const { data } = await api.get('/me');
  return data;
}