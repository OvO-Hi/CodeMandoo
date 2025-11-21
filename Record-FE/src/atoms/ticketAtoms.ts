import { atom } from 'jotai';
import { Ticket, CreateTicketData, UpdateTicketData, TicketFilterOptions } from '../types/ticket';
import { TicketStatus } from '../types/enums';
import { Result, ErrorFactory, ResultFactory } from '../types/errors';
import { 
  createNewTicket, 
  createUpdatedTicket, 
  DEFAULT_TICKET_VALUES 
} from '../constants/ticketDefaults';
import { 
  withErrorHandling, 
  optimizedMapUpdate, 
  optimizedMapBulkDelete, 
  validateOwnership 
} from '../utils/atomHelpers';
import { 
  validateTicketData, 
  validateTicketLimits, 
  validateReviewText, 
  applyTicketFilters, 
  filterTicketsByStatus, 
  calculateTicketStats, 
  processBulkDelete, 
  BulkDeleteResult 
} from '../utils/ticketHelpers';
import { currentUserIdAtom } from './userAtoms';

// ============= 기본 상태 Atoms =============

/**
 * key: ticketId, value: Ticket
 */
export const ticketsMapAtom = atom<Map<string, Ticket>>(new Map());

/**
 * AI 이미지 생성을 위한 basePrompt (5줄 영어 요약)
 * 로그아웃 또는 티켓 생성 완료 전까지 유지
 */
export const basePromptAtom = atom<string | null>(null);


// ============= 파생 Atoms (읽기 전용) =============

/**
 * 티켓 배열로 변환 (기존 컴포넌트 호환성)
 */
export const ticketsAtom = atom<Ticket[]>((get) => {
  const ticketsMap = get(ticketsMapAtom);
  return Array.from(ticketsMap.values()).sort((a, b) => 
    b.createdAt.getTime() - a.createdAt.getTime()
  );
});

/**
 * 티켓 총 개수
 */
export const ticketsCountAtom = atom<number>((get) => {
  return get(ticketsMapAtom).size;
});

/**
 * 상태별 티켓 필터링 (통합 및 최적화)
 */
export const createTicketsByStatusAtom = (status: TicketStatus) => atom<Ticket[]>((get) => {
  const tickets = get(ticketsAtom);
  return filterTicketsByStatus(tickets, status);
});

/**
 * 공개 티켓 atom
 */
export const publicTicketsAtom = createTicketsByStatusAtom(TicketStatus.PUBLIC);

/**
 * 비공개 티켓 atom
 */
export const privateTicketsAtom = createTicketsByStatusAtom(TicketStatus.PRIVATE);

/**
 * 필터 옵션 atom
 */
export const ticketFilterOptionsAtom = atom<TicketFilterOptions>({
  status: undefined,
  dateRange: undefined,
  searchText: undefined,
  genre: undefined,
});

/**
 * 필터링된 티켓 목록
 */
export const filteredTicketsAtom = atom<Ticket[]>((get) => {
  const tickets = get(ticketsAtom);
  const filterOptions = get(ticketFilterOptionsAtom);
  return applyTicketFilters(tickets, filterOptions);
});

/**
 * 특정 티켓 조회
 */
export const getTicketByIdAtom = atom<(id: string) => Ticket | undefined>((get) => {
  const ticketsMap = get(ticketsMapAtom);
  return (id: string) => ticketsMap.get(id);
});

// ============= 쓰기 Atoms (액션) =============
export const addTicketAtom = atom(
  null,
  (get, set, ticketData: CreateTicketData): Result<Ticket> => {
    return withErrorHandling(() => {
      // 통합 유효성 검증
      const validationError = validateTicketData(ticketData);
      if (validationError) throw validationError;

      // 리뷰 텍스트 검증
      if (ticketData.review?.reviewText) {
        const reviewError = validateReviewText(ticketData.review.reviewText);
        if (reviewError) throw reviewError;
      }

      // 티켓 수 제한 확인
      const currentCount = get(ticketsCountAtom);
      const limitError = validateTicketLimits(currentCount);
      if (limitError) throw limitError;

      // 새 티켓 생성 (팩토리 함수 사용)
      const currentUserId = get(currentUserIdAtom);
      const newTicket = createNewTicket(ticketData, currentUserId);

      // 최적화된 Map 업데이트
      const currentMap = get(ticketsMapAtom);
      const newMap = optimizedMapUpdate(currentMap, newTicket.id, newTicket);
      set(ticketsMapAtom, newMap);

      return newTicket;
    }, '티켓 추가 중 오류가 발생했습니다')();
  }
);

/**
 * 기존 티켓 수정 (리팩토링된 버전)
 * 공통 에러 핸들링, 통합 검증, 최적화된 Map 업데이트 적용
 */
export const updateTicketAtom = atom(
  null,
  (get, set, ticketId: string, updateData: UpdateTicketData): Result<Ticket> => {
    return withErrorHandling(() => {
      console.log('updateTicketAtom 시작:', { ticketId, updateData });
      
      const currentMap = get(ticketsMapAtom);
      const existingTicket = currentMap.get(ticketId);
      
      console.log('기존 티켓:', existingTicket);
      
      if (!existingTicket) {
        console.log('티켓을 찾을 수 없음:', ticketId);
        throw new Error(`티켓을 찾을 수 없습니다: ${ticketId}`);
      }

      // 권한 확인 (공통 헬퍼 사용)
      const currentUserId = get(currentUserIdAtom);
      console.log('현재 사용자 ID:', currentUserId, '티켓 소유자 ID:', existingTicket.userId);
      
      const ownershipError = validateOwnership(existingTicket.userId, currentUserId, '티켓 수정');
      if (ownershipError) {
        console.log('권한 오류:', ownershipError);
        throw ownershipError;
      }

      // 통합 유효성 검증
      const validationError = validateTicketData(updateData, true);
      if (validationError) {
        console.log('유효성 검증 오류:', validationError);
        throw validationError;
      }

      // 리뷰 텍스트 검증
      if (updateData.review?.reviewText) {
        const reviewError = validateReviewText(updateData.review.reviewText);
        if (reviewError) {
          console.log('리뷰 검증 오류:', reviewError);
          throw reviewError;
        }
      }

      // 업데이트된 티켓 생성 (팩토리 함수 사용)
      const updatedTicket = createUpdatedTicket(existingTicket, updateData);
      console.log('업데이트된 티켓:', updatedTicket);

      // 최적화된 Map 업데이트
      const newMap = optimizedMapUpdate(currentMap, ticketId, updatedTicket);
      set(ticketsMapAtom, newMap);
      
      console.log('티켓 업데이트 완료');
      return updatedTicket;
    }, '티켓 수정 중 오류가 발생했습니다')();
  }
);

/**
 * 티켓 삭제 
 */
export const deleteTicketAtom = atom(
  null,
  (get, set, ticketId: string): Result<boolean> => {
    return withErrorHandling(() => {
      const currentMap = get(ticketsMapAtom);
      const existingTicket = currentMap.get(ticketId);
      
      if (!existingTicket) {
        throw new Error(`티켓을 찾을 수 없습니다: ${ticketId}`);
      }

      // 권한 확인 (공통 헬퍼 사용)
      const currentUserId = get(currentUserIdAtom);
      const ownershipError = validateOwnership(existingTicket.userId, currentUserId, '티켓 삭제');
      if (ownershipError) throw ownershipError;

      // 최적화된 Map 삭제
      const { newMap } = optimizedMapBulkDelete(currentMap, [ticketId]);
      set(ticketsMapAtom, newMap);

      return true;
    }, '티켓 삭제 중 오류가 발생했습니다')();
  }
);


// ============= 추가 파생 Atoms =============

/**
 * 최근 티켓 (7일 이내)
 */
export const recentTicketsAtom = atom<Ticket[]>((get) => {
  const tickets = get(ticketsAtom);
  const sevenDaysAgo = new Date();
  sevenDaysAgo.setDate(sevenDaysAgo.getDate() - 7);
  
  return tickets.filter(ticket => ticket.createdAt >= sevenDaysAgo);
});

/**
 * 리뷰가 있는 티켓만
 */
export const ticketsWithReviewsAtom = atom<Ticket[]>((get) => {
  const tickets = get(ticketsAtom);
  return tickets.filter(ticket => ticket.review?.reviewText);
});

/**
 * 이미지가 있는 티켓만
 */
export const ticketsWithImagesAtom = atom<Ticket[]>((get) => {
  const tickets = get(ticketsAtom);
  return tickets.filter(ticket => ticket.images && ticket.images.length > 0);
});


/**
 * 동적 필터링 atom 생성 함수 (최적화된 버전)
 * 통합 필터링 로직 사용
 */
export const createFilteredTicketsAtom = (filterOptions: TicketFilterOptions) => atom<Ticket[]>((get) => {
  const tickets = get(ticketsAtom);
  return applyTicketFilters(tickets, filterOptions);
});

/**
 * 필터 옵션 업데이트 atom
 */
export const updateFilterOptionsAtom = atom(
  null,
  (get, set, newOptions: Partial<TicketFilterOptions>): void => {
    const currentOptions = get(ticketFilterOptionsAtom);
    set(ticketFilterOptionsAtom, { ...currentOptions, ...newOptions });
  }
);

/**
 * 티켓 통계 정보
 */
export const ticketStatsAtom = atom((get) => {
  const tickets = get(ticketsAtom);
  return calculateTicketStats(tickets);
});


/**
 * 모든 필터 초기화
 */
export const resetFiltersAtom = atom(
  null,
  (get, set): void => {
    set(ticketFilterOptionsAtom, {
      status: undefined,
      dateRange: undefined,
      searchText: undefined,
      genre: undefined,
    });
  }
);

/**
 * 티켓 검색 atom
 */
export const searchTicketsAtom = atom(
  null,
  (get, set, searchText: string): void => {
    const currentOptions = get(ticketFilterOptionsAtom);
    set(ticketFilterOptionsAtom, { ...currentOptions, searchText });
  }
);
