import { Ticket } from '../types/ticket';

export const isPlaceholderTicket = (ticket: Ticket) => {
  return !ticket.id || !ticket.performedAt;
};
