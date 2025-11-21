/**
 * 티켓 관련 API 연동 atoms
 * 기존 ticketsAtoms.ts를 API 연동으로 리팩토링
 */

import { atom } from 'jotai';
import { ticketService } from '../services/api/index';
import { Ticket, TicketStatus } from '../types/ticket';
import { TicketUpdateRequest } from '../types/api/tickets';
// Result 타입을 직접 정의 (임시 해결책)
type Result<T> = {
  success: true;
  data: T;
  error?: never;
} | {
  success: false;
  data?: never;
  error: { code: string; message: string; details?: any };
};

class ResultFactory {
  static success<T>(data: T): Result<T> {
    return { success: true, data };
  }
  
  static failure<T>(error: { code: string; message: string; details?: any }): Result<T> {
    return { success: false, error };
  }
}
import { ApiState, createInitialApiState, apiStateHelpers, isCacheValid } from './apiAtoms';

/**
 * 내 티켓 목록 상태
 */
export const myTicketsStateAtom = atom<ApiState<Ticket[]>>(createInitialApiState<Ticket[]>());

/**
 * 친구 티켓 맵 상태 (friendId -> tickets)
 */
export const friendTicketsMapStateAtom = atom<ApiState<Map<string, Ticket[]>>>(
  createInitialApiState<Map<string, Ticket[]>>()
);

/**
 * 티켓 검색 결과 상태
 */
export const ticketSearchStateAtom = atom<ApiState<Ticket[]>>(createInitialApiState<Ticket[]>());

/**
 * 현재 선택된 티켓 필터
 */
export const ticketFilterAtom = atom<{
  status?: TicketStatus;
  startDate?: string;
  endDate?: string;
  search?: string;
}>({});

/**
 * 내 티켓 목록 조회
 */
export const fetchMyTicketsAtom = atom(
  null,
  async (get, set, force: boolean = false) => {
    const currentState = get(myTicketsStateAtom);
    
    if (!force && currentState.data && isCacheValid(currentState.lastFetch)) {
      return ResultFactory.success(currentState.data);
    }

    set(myTicketsStateAtom, apiStateHelpers.setLoading(currentState));

    try {
      const filter = get(ticketFilterAtom);
      const result = await ticketService.getMyTickets({
        limit: 100, // 충분히 큰 값으로 설정
        ...filter,
      });
      
      if (result.success && result.data) {
        const tickets = result.data.tickets;
        set(myTicketsStateAtom, apiStateHelpers.setSuccess(currentState, tickets));
        return ResultFactory.success(tickets);
      } else {
        const errorMessage = result.error?.message || '티켓 목록을 불러오는데 실패했습니다';
        set(myTicketsStateAtom, apiStateHelpers.setError(currentState, errorMessage));
        return ResultFactory.failure(result.error!);
      }
    } catch (error) {
      const errorMessage = error instanceof Error ? error.message : '알 수 없는 오류가 발생했습니다';
      set(myTicketsStateAtom, apiStateHelpers.setError(currentState, errorMessage));
      return ResultFactory.failure({ message: errorMessage, code: 'UNKNOWN_ERROR' });
    }
  }
);

/**
 * 친구 티켓 조회
 */
export const fetchFriendTicketsAtom = atom(
  null,
  async (get, set, friendId: string, force: boolean = false) => {
    const currentMapState = get(friendTicketsMapStateAtom);
    const currentMap = currentMapState.data || new Map();
    
    // 캐시 확인
    if (!force && currentMap.has(friendId) && isCacheValid(currentMapState.lastFetch)) {
      return ResultFactory.success(currentMap.get(friendId)!);
    }

    // 로딩 상태는 전체 맵에 대해 설정하지 않고 개별적으로 처리
    try {
      const result = await ticketService.getFriendTickets({ friendId, limit: 100 });
      
      if (result.success && result.data) {
        const tickets = result.data.tickets;
        const newMap = new Map(currentMap);
        newMap.set(friendId, tickets);
        
        set(friendTicketsMapStateAtom, apiStateHelpers.setSuccess(currentMapState, newMap));
        return ResultFactory.success(tickets);
      } else {
        const errorMessage = result.error?.message || '친구 티켓을 불러오는데 실패했습니다';
        return ResultFactory.failure(result.error!);
      }
    } catch (error) {
      const errorMessage = error instanceof Error ? error.message : '알 수 없는 오류가 발생했습니다';
      return ResultFactory.failure({ message: errorMessage, code: 'UNKNOWN_ERROR' });
    }
  }
);

/**
 * 티켓 생성
 */
export const createTicketAtom = atom(
  null,
  async (get, set, ticketData: {
    title: string;
    performedAt: Date;
    place: string;
    artist: string;
    bookingSite?: string;
    genre?: string | null;
    status: TicketStatus;
    review?: {
      reviewText: string;
      rating: number;
      createdAt: Date;
      updatedAt?: Date;
    };
    images?: string[];
  }) => {
    try {
      // 낙관적 업데이트: 새 티켓을 목록에 임시 추가
      const currentState = get(myTicketsStateAtom);
      if (currentState.data) {
        const optimisticTicket: Ticket = {
          id: `temp_${Date.now()}`,
          ...ticketData,
          genre: ticketData.genre || null,
          performedAt: ticketData.performedAt,
          userId: 'current_user',
          createdAt: new Date(),
          review: ticketData.review ? {
            reviewText: ticketData.review.reviewText,
            createdAt: ticketData.review.createdAt || new Date(),
          } : undefined,
          images: ticketData.images || [],
        };
        
        const updatedTickets = [optimisticTicket, ...currentState.data];
        set(myTicketsStateAtom, apiStateHelpers.setSuccess(currentState, updatedTickets));
      }

      // 실제 API 호출
      const result = await ticketService.createTicket({
        ...ticketData,
        performedAt: ticketData.performedAt.toISOString(),
      });
      
      if (result.success) {
        // 성공 시 티켓 목록 새로고침
        set(fetchMyTicketsAtom, true);
        return result;
      } else {
        // 실패 시 낙관적 업데이트 롤백
        set(fetchMyTicketsAtom, true);
        return result;
      }
    } catch (error) {
      // 에러 시 롤백
      set(fetchMyTicketsAtom, true);
      const errorMessage = error instanceof Error ? error.message : '티켓 생성에 실패했습니다';
      return ResultFactory.failure({ message: errorMessage, code: 'CREATE_TICKET_ERROR' });
    }
  }
);

/**
 * 티켓 수정
 */
export const updateTicketAtom = atom(
  null,
  async (get, set, ticketData: {
    id: string;
    title?: string;
    performedAt?: Date;
    place?: string;
    artist?: string;
    bookingSite?: string;
    status?: TicketStatus;
    review?: {
      reviewText: string;
      rating: number;
      createdAt: Date;
      updatedAt?: Date;
    };
    images?: string[];
  }) => {
    try {
      // 낙관적 업데이트
      const currentState = get(myTicketsStateAtom);
      if (currentState.data) {
        const updatedTickets = currentState.data.map(ticket => {
          if (ticket.id === ticketData.id) {
            return {
              ...ticket,
              ...ticketData,
              performedAt: ticketData.performedAt || ticket.performedAt,
              updatedAt: new Date(),
            };
          }
          return ticket;
        });
        
        set(myTicketsStateAtom, apiStateHelpers.setSuccess(currentState, updatedTickets));
      }

      const requestPayload: TicketUpdateRequest = {
        // ✅ 백엔드 스키마에 맞춰 필요한 필드만 골라 전달합니다.
        title: ticketData.title,
        venue: ticketData.place,
        performedAt: ticketData.performedAt?.toISOString(),
        reviewText: ticketData.review?.reviewText,
        isPublic: ticketData.status
          ? ticketData.status === TicketStatus.PUBLIC
          : undefined,
      };

      // 실제 API 호출
      const result = await ticketService.updateTicket(
        ticketData.id,
        requestPayload,
      );
      
      if (result.success) {
        // 성공 시 티켓 목록 새로고침
        set(fetchMyTicketsAtom, true);
        return result;
      } else {
        // 실패 시 롤백
        set(fetchMyTicketsAtom, true);
        return result;
      }
    } catch (error) {
      // 에러 시 롤백
      set(fetchMyTicketsAtom, true);
      const errorMessage = error instanceof Error ? error.message : '티켓 수정에 실패했습니다';
      return ResultFactory.failure({ message: errorMessage, code: 'UPDATE_TICKET_ERROR' });
    }
  }
);

/**
 * 티켓 삭제
 */
export const deleteTicketAtom = atom(
  null,
  async (get, set, ticketId: string) => {
    try {
      // 낙관적 업데이트: 티켓 목록에서 제거
      const currentState = get(myTicketsStateAtom);
      if (currentState.data) {
        const updatedTickets = currentState.data.filter(ticket => ticket.id !== ticketId);
        set(myTicketsStateAtom, apiStateHelpers.setSuccess(currentState, updatedTickets));
      }

      // 실제 API 호출
      const result = await ticketService.deleteTicket(ticketId);
      
      if (result.success) {
        return result;
      } else {
        // 실패 시 롤백
        set(fetchMyTicketsAtom, true);
        return result;
      }
    } catch (error) {
      // 에러 시 롤백
      set(fetchMyTicketsAtom, true);
      const errorMessage = error instanceof Error ? error.message : '티켓 삭제에 실패했습니다';
      return ResultFactory.failure({ message: errorMessage, code: 'DELETE_TICKET_ERROR' });
    }
  }
);

/**
 * 티켓 검색
 */
export const searchTicketsAtom = atom(
  null,
  async (get, set, query: string) => {
    if (!query.trim()) {
      set(ticketSearchStateAtom, apiStateHelpers.reset<Ticket[]>());
      return ResultFactory.success([]);
    }

    const currentState = get(ticketSearchStateAtom);
    set(ticketSearchStateAtom, apiStateHelpers.setLoading(currentState));

    try {
      const result = await ticketService.searchTickets(query, { limit: 50 });
      
      if (result.success && result.data) {
        const tickets = result.data.tickets;
        set(ticketSearchStateAtom, apiStateHelpers.setSuccess(currentState, tickets));
        return ResultFactory.success(tickets);
      } else {
        const errorMessage = result.error?.message || '티켓 검색에 실패했습니다';
        set(ticketSearchStateAtom, apiStateHelpers.setError(currentState, errorMessage));
        return ResultFactory.failure(result.error!);
      }
    } catch (error) {
      const errorMessage = error instanceof Error ? error.message : '검색 중 오류가 발생했습니다';
      set(ticketSearchStateAtom, apiStateHelpers.setError(currentState, errorMessage));
      return ResultFactory.failure({ message: errorMessage, code: 'SEARCH_ERROR' });
    }
  }
);

/**
 * 날짜별 티켓 조회
 */
export const fetchTicketsByDateAtom = atom(
  null,
  async (get, set, date: string) => {
    try {
      const result = await ticketService.getTicketsByDate(date);
      
      if (result.success && result.data) {
        return ResultFactory.success(result.data);
      } else {
        return ResultFactory.failure(result.error!);
      }
    } catch (error) {
      const errorMessage = error instanceof Error ? error.message : '날짜별 티켓 조회에 실패했습니다';
      return ResultFactory.failure({ message: errorMessage, code: 'FETCH_BY_DATE_ERROR' });
    }
  }
);

/**
 * 읽기 전용 atoms (컴포넌트에서 사용)
 */
export const myTicketsAtom = atom<Ticket[]>((get) => {
  const state = get(myTicketsStateAtom);
  return state.data || [];
});

export const friendTicketsMapAtom = atom<Map<string, Ticket[]>>((get) => {
  const state = get(friendTicketsMapStateAtom);
  return state.data || new Map();
});

export const ticketSearchResultsAtom = atom<Ticket[]>((get) => {
  const state = get(ticketSearchStateAtom);
  return state.data || [];
});

/**
 * 필터링된 티켓 목록
 */
export const filteredTicketsAtom = atom<Ticket[]>((get) => {
  const tickets = get(myTicketsAtom);
  const filter = get(ticketFilterAtom);
  
  return tickets.filter(ticket => {
    // 날짜 필터
    if (filter.startDate) {
      const ticketDate = new Date(ticket.performedAt);
      const startDate = new Date(filter.startDate);
      if (ticketDate < startDate) {
        return false;
      }
    }
    
    if (filter.endDate) {
      const ticketDate = new Date(ticket.performedAt);
      const endDate = new Date(filter.endDate);
      if (ticketDate > endDate) {
        return false;
      }
    }
    
    // 검색 필터
    if (filter.search) {
      const searchLower = filter.search.toLowerCase();
      return (
        ticket.title.toLowerCase().includes(searchLower) ||
        (ticket.artist?.toLowerCase().includes(searchLower) ?? false) ||
        (ticket.place?.toLowerCase().includes(searchLower) ?? false)
      );
    }
    
    return true;
  });
});

/**
 * 로딩 상태 atoms
 */
export const myTicketsLoadingAtom = atom<boolean>((get) => {
  const state = get(myTicketsStateAtom);
  return state.loading === 'loading';
});

export const ticketSearchLoadingAtom = atom<boolean>((get) => {
  const state = get(ticketSearchStateAtom);
  return state.loading === 'loading';
});

/**
 * 에러 상태 atoms
 */
export const myTicketsErrorAtom = atom<string | null>((get) => {
  const state = get(myTicketsStateAtom);
  return state.error;
});

export const ticketSearchErrorAtom = atom<string | null>((get) => {
  const state = get(ticketSearchStateAtom);
  return state.error;
});

/**
 * 티켓 통계 atoms
 */
export const ticketStatsAtom = atom<{
  total: number;
  public: number;
  private: number;
  thisMonth: number;
  thisYear: number;
}>((get) => {
  const tickets = get(myTicketsAtom);
  const now = new Date();
  const thisMonth = new Date(now.getFullYear(), now.getMonth(), 1);
  const thisYear = new Date(now.getFullYear(), 0, 1);

  return {
    total: tickets.length,
    public: tickets.filter(t => t.status === TicketStatus.PUBLIC).length,
    private: tickets.filter(t => t.status === TicketStatus.PRIVATE).length,
    thisMonth: tickets.filter(t => new Date(t.performedAt) >= thisMonth).length,
    thisYear: tickets.filter(t => new Date(t.performedAt) >= thisYear).length,
  };
});

