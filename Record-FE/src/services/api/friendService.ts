/**
 * 친구 관련 API 서비스 (백엔드 명세와 100% 맞춘 버전)
 */

import { apiClient } from './client';
import { Result } from '../../utils/result';
import {
  Friend,
  FriendRequest,
  CreateFriendRequestData,
  RespondToFriendRequestData,
} from '../../types/friend';

class FriendService {

  /**
   * 친구 목록 조회
   * GET /friendships/{userId}/friends
   */
  async getFriends(userId: string): Promise<Result<{ friends: Friend[] }>> {
    return apiClient.get(`/friendships/${userId}/friends`);
  }

  /**
   * 친구 수 조회
   * GET /friendships/{userId}/friend-count
   */
  async getFriendCount(userId: string): Promise<Result<{ count: number }>> {
    return apiClient.get(`/friendships/${userId}/friend-count`);
  }

  /**
   * 보낸 친구 요청 목록 조회
   * GET /friendships/{userId}/sent-requests
   */
  async getSentFriendRequests(userId: string): Promise<Result<{ requests: FriendRequest[] }>> {
    return apiClient.get(`/friendships/${userId}/sent-requests`);
  }

  /**
   * 받은 친구 요청 목록 조회
   * GET /friendships/{userId}/received-requests
   */
  async getReceivedFriendRequests(userId: string): Promise<Result<{ requests: FriendRequest[] }>> {
    return apiClient.get(`/friendships/${userId}/received-requests`);
  }

  /**
   * 대기중 요청 카운트
   * GET /friendships/{userId}/pending-count
   */
  async getPendingRequestCount(userId: string): Promise<Result<{ count: number }>> {
    return apiClient.get(`/friendships/${userId}/pending-count`);
  }

  /**
   * 친구 요청 보내기
   * POST /friendships/send
   * Header: X-User-Id
   */
  async sendFriendRequest(userId: string, data: { targetId: string }): Promise<Result<any>> {
    return apiClient.post(`/friendships/send`, data, {
      headers: { 'X-User-Id': userId },
    });
  }

  /**
   * 친구 요청 수락
   * POST /friendships/{friendshipId}/accept
   * Header: X-User-Id
   */
  async acceptFriendRequest(userId: string, friendshipId: number): Promise<Result<any>> {
    return apiClient.post(`/friendships/${friendshipId}/accept`, null, {
      headers: { 'X-User-Id': userId },
    });
  }

  /**
   * 친구 요청 거절
   * POST /friendships/{friendshipId}/reject
   * Header: X-User-Id
   */
  async rejectFriendRequest(userId: string, friendshipId: number): Promise<Result<any>> {
    return apiClient.post(`/friendships/${friendshipId}/reject`, null, {
      headers: { 'X-User-Id': userId },
    });
  }

  /**
   * 친구 관계 삭제
   * DELETE /friendships/{friendshipId}
   * Header: X-User-Id
   */
  async removeFriend(userId: string, friendshipId: number): Promise<Result<any>> {
    return apiClient.delete(`/friendships/${friendshipId}`, null, {
      headers: { 'X-User-Id': userId },
    });
  }
}

export const friendService = new FriendService();
export default friendService;
