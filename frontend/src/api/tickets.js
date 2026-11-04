import { api } from './client.js';
import { ticketFromApi } from './contract.js';

export async function listTickets(params = {}) {
  const { data } = await api.get('/tickets', {
    params: { ...params, status: params.status?.toUpperCase() || undefined },
  });
  return { ...data, items: (data.items ?? []).map(ticketFromApi) };
}

export async function createTicket(payload) {
  const { data } = await api.post('/tickets', { ...payload, priority: payload.priority?.toUpperCase() });
  return ticketFromApi(data);
}

export async function getTicket(id) {
  const { data } = await api.get(`/tickets/${id}`);
  return ticketFromApi(data);
}

export async function updateStatus(id, status) {
  const { data } = await api.patch(`/tickets/${id}/status`, { status: status.toUpperCase() });
  return ticketFromApi(data);
}

export async function addComment(id, content) {
  const { data } = await api.post(`/tickets/${id}/comments`, { content });
  return data;
}
export async function assignTicket(id, assigneeId) {
  const assignedTo = Number(assigneeId);
  if (!Number.isSafeInteger(assignedTo) || assignedTo <= 0) throw new Error('ID người phụ trách không hợp lệ.');
  const { data } = await api.patch(`/tickets/${id}/assign`, { assigned_to: assignedTo });
  return ticketFromApi(data);
}
