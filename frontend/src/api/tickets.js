import { api } from './client';

export async function listTickets(params = {}) {
  const { data } = await api.get('/tickets', { params });
  return data; 
}

export async function createTicket(payload) {
  const { data } = await api.post('/tickets', payload);
  return data;
}

export async function getTicket(id) {
  const { data } = await api.get(`/tickets/${id}`);
  return data;
}

export async function updateStatus(id, status) {
  const { data } = await api.patch(`/tickets/${id}/status`, { status });
  return data;
}

export async function addComment(id, content) {
  const { data } = await api.post(`/tickets/${id}/comments`, { content });
  return data;
}
export async function getHistory(id) {
  // API này có thể chưa có trong danh sách tối thiểu của TV3, 
  // nếu chưa có bạn có thể tạm dùng mảng rỗng hoặc mock
  const { data } = await api.get(`/tickets/${id}/history`);
  return data;
}

export async function assignTicket(id, assigneeId) {
  // API phân công (nếu TV3 chưa làm, bạn có thể comment lại)
  const { data } = await api.patch(`/tickets/${id}/assign`, { assigneeId });
  return data;
}