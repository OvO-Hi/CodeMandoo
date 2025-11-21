import { Ticket, CreateTicketData, UpdateTicketData, TicketFilterOptions } from '../types/ticket';
import { TicketStatus } from '../types/enums';
import { IdGenerator } from '../utils/idGenerator';

/**
 * 티켓 기본값
 */
export const DEFAULT_TICKET_VALUES = {
  STATUS: TicketStatus.PUBLIC,
  USER_ID: 'user_id',
  IMAGES: [] as string[],
} as const; // 읽기 전용 리터럴 타입으로 만들어줌.

/**
 * 티켓 제한값
 */
export const TICKET_LIMITS = {
  MAX_TITLE_LENGTH: 100,
  MAX_VENUE_LENGTH: 100,
  MAX_ARTIST_LENGTH: 100,
  MAX_REVIEW_LENGTH: 6000, // 이유: STT/요약 없이도 충분한 분량을 저장할 수 있도록 1000자에서 6000자로 완화
  MAX_TICKETS_PER_USER: 100,
} as const;

/**
 * 필터링 기본값
 */
export const DEFAULT_FILTER_OPTIONS: TicketFilterOptions = {
  status: undefined,
  dateRange: undefined,
  searchText: undefined,
  genre: undefined,
};

/**
 * 빈 티켓 데이터 생성 함수
 */
export const createEmptyTicket = (): Partial<Ticket> => ({
  id: '',
  user_id: '',
  title: '',
  performedAt: new Date(),
  status: DEFAULT_TICKET_VALUES.STATUS,
  venue: '',
  artist: '',
  bookingSite: '',
  genre: '밴드',
  createdAt: new Date(),
  updatedAt: new Date(),
  review: undefined,
  images: undefined,
  isPlaceholder: false,
});

/**
 * 새 티켓 생성 팩토리 함수
 */
export const createNewTicket = (
  ticketData: CreateTicketData,
  user_id: string
): Ticket => {
  const now = new Date();
  const status = ticketData.status ?? DEFAULT_TICKET_VALUES.STATUS;

  return {
    id: IdGenerator.ticket(),
    user_id,
    createdAt: now,
    updatedAt: now,
    ...ticketData,
    status,
    review: ticketData.review ? {
      ...ticketData.review,
      createdAt: now,
      updatedAt: now,
    } : undefined,
    images: ticketData.images ? [...ticketData.images] : undefined,
  };
};

/**
 * 티켓 업데이트 팩토리 함수
 */
export const createUpdatedTicket = (
  existingTicket: Ticket,
  updateData: UpdateTicketData // 바꾸고 싶은 필드만 들어있는 부분 업데이트 데이터
): Ticket => {
  const now = new Date();
  
  console.log('createUpdatedTicket 호출:', {
    existingTicket: existingTicket.title,
    updateData
  });
  
  const updatedTicket = {
    ...existingTicket,
    ...updateData,
    updatedAt: now,
    review: updateData.review ? {
      ...existingTicket.review,
      ...updateData.review,
      updatedAt: now,
    } : existingTicket.review,
  };
  
  console.log('createUpdatedTicket 결과:', updatedTicket);
  return updatedTicket;
};

/**
 * 티켓 필터링 조건 검증
 */
export const TICKET_FILTER_FIELDS = [
  'status', 
  'dateRange',
  'searchText',
  'genre',
] as const;