// Convert the backend's snake_case response fields to the view model used by React.
export function userFromApi(user) {
  return user ? { ...user, fullName: user.full_name ?? user.fullName } : null;
}

export function ticketFromApi(ticket) {
  return {
    ...ticket,
    status: ticket.status?.toLowerCase(),
    priority: ticket.priority?.toLowerCase(),
    assignee: userFromApi(ticket.assigned_to),
    createdAt: ticket.created_at,
    updatedAt: ticket.updated_at,
    comments: (ticket.comments ?? []).map(comment => ({
      ...comment, author: userFromApi(comment.author), createdAt: comment.created_at,
    })),
    history: (ticket.history ?? []).map(entry => ({
      ...entry,
      from: entry.old_status?.toLowerCase(),
      to: entry.new_status?.toLowerCase(),
      actor: userFromApi(entry.actor),
      at: entry.changed_at,
    })),
  };
}
