/**
 * UUID 및 ID 생성 유틸리티
 * 안전하고 고유한 식별자 생성을 위한 중앙 집중식 관리
 */

import { CONSTANTS } from '../types/enums';

/**
 * UUID v4 생성 함수 (React Native 환경에서 crypto API 없이 동작)
 */
export function generateUUID(): string {
  return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function(c) {
    const r = Math.random() * 16 | 0;
    const v = c === 'x' ? r : (r & 0x3 | 0x8);
    return v.toString(16);
  });
}

/**
 * 타입별 ID 생성 함수들
 */
export const IdGenerator = {
  /**
   * 사용자 ID 생성
   */
  user(): string {
    return `${CONSTANTS.ID_PREFIX.USER}${generateUUID()}`;
  },

  /**
   * 티켓 ID 생성
   */
  ticket(): string {
    return `${CONSTANTS.ID_PREFIX.TICKET}${generateUUID()}`;
  },

  /**
   * 친구 ID 생성
   */
  friend(): string {
    return `${CONSTANTS.ID_PREFIX.FRIEND}${generateUUID()}`;
  },

  /**
   * 친구 요청 ID 생성
   */
  friendRequest(): string {
    return `${CONSTANTS.ID_PREFIX.REQUEST}${generateUUID()}`;
  },
} as const;

/**
 * ID 유효성 검증 함수들
 */
export const IdValidator = {
  /**
   * UUID 형식 검증
   */
  isValidUUID(id: string): boolean {
    const uuidRegex = /^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$/i;
    return uuidRegex.test(id);
  },

  /**
   * 사용자 ID 형식 검증
   */
  isValidUserId(id: string): boolean {
    return id.startsWith(CONSTANTS.ID_PREFIX.USER) && 
           this.isValidUUID(id.substring(CONSTANTS.ID_PREFIX.USER.length));
  },

  /**
   * 티켓 ID 형식 검증
   */
  isValidTicketId(id: string): boolean {
    return id.startsWith(CONSTANTS.ID_PREFIX.TICKET) && 
           this.isValidUUID(id.substring(CONSTANTS.ID_PREFIX.TICKET.length));
  },

  /**
   * 친구 ID 형식 검증
   */
  isValidFriendId(id: string): boolean {
    return id.startsWith(CONSTANTS.ID_PREFIX.FRIEND) && 
           this.isValidUUID(id.substring(CONSTANTS.ID_PREFIX.FRIEND.length));
  },
} as const;
