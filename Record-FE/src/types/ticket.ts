import { TicketStatus, TicketCategory } from './enums';
export { TicketStatus, TicketCategory } from './enums';

/**
 * 기본 티켓 인터페이스
 */
export interface BaseTicket {
  readonly id: string;
  readonly user_id: string;
  readonly createdAt: Date;
  readonly updatedAt?: Date;
  title: string;
  performedAt: Date;
  venue?: string;
  artist?: string;
  seat?: string;
  bookingSite?: string;
  genre: string;
  status?: TicketStatus;
}

/**
 * 완전한 티켓 인터페이스
 */
export interface Ticket extends BaseTicket {
  review?: TicketReview;
  images?: readonly string[];
  isPlaceholder?: boolean;
}

/**
 * 티켓 리뷰 인터페이스
 */
export interface TicketReview {
  reviewText: string;
  readonly createdAt: Date;
  readonly updatedAt?: Date;
}


/**
 * 티켓 생성용 데이터 인터페이스 (티켓 생성)
*/
export interface CreateTicketData extends Omit<BaseTicket, 'id' | 'user_id' | 'createdAt' | 'updatedAt'> {
  review?: Omit<TicketReview, 'createdAt'>;
  images?: string[];
}

/**
 * 티켓 업데이트용 데이터 인터페이스 (티켓 수정)
 */
export interface UpdateTicketData extends Partial<Omit<Ticket, 'id' | 'user_id' | 'createdAt' | 'updatedAt'>> {}

/**
 * 티켓 폼 데이터 인터페이스
 */
export interface TicketFormData extends Pick<BaseTicket, 'title' | 'performedAt' | 'venue' | 'artist' | 'genre'> {
  reviewText?: string;
  images?: string[];
}

/**
 * 티켓 필터 옵션 인터페이스
 */
export interface TicketFilterOptions {
  status?: TicketStatus;
  dateRange?: { start: Date; end: Date };
  searchText?: string;
  genre?: string;
}