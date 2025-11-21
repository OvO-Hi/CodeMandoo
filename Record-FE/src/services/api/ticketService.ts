import { apiClient } from './client';
import { Result } from '../../utils/result';
import {
  TicketCreateRequest,
  TicketResponse,
  TicketUpdateRequest,
} from '../../types/api/tickets';

class TicketService {
  /**
   * 나의 티켓 목록 조회
   */
  async getMyTickets(_: Record<string, unknown> = {}): Promise<Result<TicketResponse[]>> {
    return apiClient.get<TicketResponse[]>('/tickets/me');
  }

  /**
   * 티켓 생성
   */
  async createTicket(data: TicketCreateRequest): Promise<Result<TicketResponse>> {
    return apiClient.post('/tickets', data);
  }

  /**
   * 티켓 수정
   *
   * 이유: 모달에서 제목·관람일·공개범위를 부분적으로 고치기 때문에
   *       PATCH 요청으로 필요한 필드만 보냅니다.
   */
  async updateTicket(
    ticketId: string | number,
    data: TicketUpdateRequest,
  ): Promise<Result<TicketResponse>> {
    return apiClient.patch(`/tickets/${ticketId}`, data);
  }

  /**
   * 티켓 삭제
   *
   * 이유: 삭제 성공 시 프론트에서도 같은 티켓을 즉시 지워야 하므로
   *       서버 응답만 확인하면 됩니다(바디 필요 없음).
   */
  async deleteTicket(ticketId: string | number): Promise<Result<null>> {
    return apiClient.delete(`/tickets/${ticketId}`);
  }
}

export const ticketService = new TicketService();
export default ticketService;
