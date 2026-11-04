import { afterEach, beforeEach, mock, test } from 'node:test';
import assert from 'node:assert/strict';
import { api } from './client.js';
import { login } from './auth.js';
import { assignTicket, createTicket, getTicket, listTickets, updateStatus } from './tickets.js';

const values = new Map();
globalThis.localStorage = {
  getItem: key => values.get(key) ?? null,
  setItem: (key, value) => values.set(key, value),
  removeItem: key => values.delete(key),
};
beforeEach(() => values.clear());
afterEach(() => mock.restoreAll());

test('login sends email and stores accessToken returned by Spring Boot', async () => {
  mock.method(api, 'post', async (path, body) => {
    assert.equal(path, '/auth/login');
    assert.deepEqual(body, { email: 'staff@example.test', password: 'example-password' });
    return { data: { accessToken: 'example-token', user: { id: 1, full_name: 'Staff', role: 'STAFF' } } };
  });
  const user = await login(' staff@example.test ', 'example-password');
  assert.equal(values.get('token'), 'example-token');
  assert.equal(user.fullName, 'Staff');
});

test('malformed login responses do not create an authenticated session', async () => {
  mock.method(api, 'post', async () => ({ data: { user: { id: 1 } } }));
  await assert.rejects(login('staff@example.test', 'password'), /không hợp lệ/);
  assert.equal(values.has('token'), false);
});

test('ticket filter uses backend enums and renders snake_case response fields', async () => {
  mock.method(api, 'get', async (path, options) => {
    assert.equal(path, '/tickets');
    assert.equal(options.params.status, 'IN_PROGRESS');
    return { data: { items: [{ id: 2, status: 'IN_PROGRESS', priority: 'HIGH',
      assigned_to: { full_name: 'Technician' }, updated_at: '2026-11-05T00:00:00Z' }] } };
  });
  const result = await listTickets({ status: 'in_progress' });
  assert.equal(result.items[0].status, 'in_progress');
  assert.equal(result.items[0].assignee.fullName, 'Technician');
  assert.equal(result.items[0].updatedAt, '2026-11-05T00:00:00Z');
});

test('ticket creation sends uppercase priority', async () => {
  mock.method(api, 'post', async (_path, body) => {
    assert.equal(body.priority, 'URGENT');
    return { data: { id: 3, status: 'OPEN', priority: 'URGENT' } };
  });
  const ticket = await createTicket({ title: 'Test', description: 'Test', priority: 'urgent' });
  assert.equal(ticket.priority, 'urgent');
});

test('status update sends the exact backend enum', async () => {
  mock.method(api, 'patch', async (path, body) => {
    assert.equal(path, '/tickets/3/status');
    assert.deepEqual(body, { status: 'RESOLVED' });
    return { data: { id: 3, status: 'RESOLVED' } };
  });
  assert.equal((await updateStatus(3, 'resolved')).status, 'resolved');
});

test('assignment uses assigned_to as a numeric ID and rejects invalid IDs', async () => {
  mock.method(api, 'patch', async (_path, body) => {
    assert.deepEqual(body, { assigned_to: 7 });
    return { data: { id: 3 } };
  });
  await assignTicket(3, '7');
  await assert.rejects(assignTicket(3, 'invalid'), /không hợp lệ/);
});

test('detail reads embedded comments/history instead of calling missing endpoints', async () => {
  const get = mock.method(api, 'get', async path => {
    assert.equal(path, '/tickets/3');
    return { data: { id: 3, status: 'IN_PROGRESS',
      comments: [{ content: 'Test', author: { full_name: 'Staff' }, created_at: '2026-11-05T00:00:00Z' }],
      history: [{ old_status: 'OPEN', new_status: 'IN_PROGRESS', actor: { full_name: 'Admin' }, changed_at: '2026-11-05T00:00:00Z' }] } };
  });
  const ticket = await getTicket(3);
  assert.equal(get.mock.callCount(), 1);
  assert.equal(ticket.comments[0].author.fullName, 'Staff');
  assert.equal(ticket.history[0].to, 'in_progress');
  assert.equal(ticket.history[0].at, '2026-11-05T00:00:00Z');
});
