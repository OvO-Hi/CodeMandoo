/**
 * 친구 관련 타입 정의
 * 친구 정보, 요청, 관계 상태를 관리
 */

import { FriendRequestStatus } from './enums';
import { Ticket } from './ticket';

/**
 * 친구 기본 정보
 */
export interface Friend {
  readonly id: string;
  user_id: string;
  nickname: string;
  profileImage?: string;
  readonly createdAt: Date;
  updatedAt: Date;
}

/**
 * 친구 요청 정보
 */
export interface FriendRequest {
  readonly id: string;
  readonly fromUserId: string;
  readonly toUserId: string;
  nickname: string;
  user_id: string;
  profileImage?: string;
  status: FriendRequestStatus;
  message?: string;
  readonly createdAt: Date;
  updatedAt: Date;
}

/**
 * 친구 관계 정보
 */
export interface Friendship {
  readonly id: string;
  readonly user_id: string;
  readonly friend_id: string;
  readonly createdAt: Date;
  isBlocked: boolean;
}

/**
 * 친구의 티켓 데이터 (Map 구조)
 */
export interface FriendTicketsData {
  tickets: Ticket[];
  lastUpdated: string; // ISO string for serialization
}

/**
 * 친구 티켓 Map 타입
 */
export type FriendTicketsMap = Map<string, FriendTicketsData>;

/**
 * 친구 티켓 업데이트 파라미터
 */
export interface UpdateFriendTicketsParams {
  friendId: string;
  tickets: Ticket[];
}

/**
 * 친구 삭제 파라미터
 */
export interface RemoveFriendParams {
  friendId: string;
}

/**
 * 친구 검색 결과
 */
export interface FriendSearchResult {
  readonly id: string;
  nickname: string;
  user_id: string;
  profileImage?: string;
  mutualFriendsCount: number;
  isAlreadyFriend: boolean;
  hasPendingRequest: boolean;
}

/**
 * 친구 요청 생성용 데이터
 */
export interface CreateFriendRequestData {
  toUserId: string;
  nickname: string;
  user_id: string;
  message?: string;
}

/**
 * 친구 요청 응답용 데이터
 */
export interface RespondToFriendRequestData {
  requestId: string;
  accept: boolean;
}
