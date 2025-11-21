/**
 * 데이터 유효성 검증 유틸리티
 * 티켓, 사용자, 친구 데이터의 유효성을 검증
 */

import { CONSTANTS, TicketStatus, AccountVisibility } from '../types/enums';
import { ValidationError, ErrorFactory } from '../types/errors';
import { IdValidator } from './idGenerator';

/**
 * 티켓 데이터 유효성 검증
 */
export const TicketValidator = {
  /**
   * 티켓 제목 유효성 검증
   */
  validateTitle(title: string): ValidationError | null {
    if (!title || title.trim().length === 0) {
      return ErrorFactory.validation('티켓 제목은 필수입니다', 'title');
    }
    if (title.length > CONSTANTS.LIMITS.MAX_TITLE_LENGTH) {
      return ErrorFactory.validation(
        `티켓 제목은 ${CONSTANTS.LIMITS.MAX_TITLE_LENGTH}자를 초과할 수 없습니다`,
        'title'
      );
    }
    return null;
  },

  /**
   * 공연 날짜 유효성 검증
   */
  validatePerformedAt(date: Date): ValidationError | null {
    if (!date || isNaN(date.getTime())) {
      return ErrorFactory.validation('유효한 공연 날짜를 입력해주세요', 'performedAt');
    }
    return null;
  },

  /**
   * 티켓 상태 유효성 검증
   */
  validateStatus(status: string): ValidationError | null {
    if (!Object.values(TicketStatus).includes(status as TicketStatus)) {
      return ErrorFactory.validation('유효하지 않은 티켓 상태입니다', 'status');
    }
    return null;
  },

  /**
   * 리뷰 텍스트 유효성 검증
   */
  validateReviewText(reviewText: string): ValidationError | null {
    if (reviewText && reviewText.length > CONSTANTS.LIMITS.MAX_REVIEW_LENGTH) {
      return ErrorFactory.validation(
        `리뷰는 ${CONSTANTS.LIMITS.MAX_REVIEW_LENGTH}자를 초과할 수 없습니다`,
        'reviewText'
      );
    }
    return null;
  },
} as const;

/**
 * 사용자 데이터 유효성 검증
 */
export const UserValidator = {
  /**
   * 사용자 이름 유효성 검증
   */
  validateName(name: string): ValidationError | null {
    if (!name || name.trim().length === 0) {
      return ErrorFactory.validation('사용자 이름은 필수입니다', 'name');
    }
    if (name.length < 2) {
      return ErrorFactory.validation('사용자 이름은 최소 2자 이상이어야 합니다', 'name');
    }
    if (name.length > 50) {
      return ErrorFactory.validation('사용자 이름은 50자를 초과할 수 없습니다', 'name');
    }
    return null;
  },

  /**
   * 사용자 ID 유효성 검증
   */
  validateUserId(userId: string): ValidationError | null {
    if (!userId || userId.trim().length === 0) {
      return ErrorFactory.validation('사용자 ID는 필수입니다', 'userId');
    }
    if (userId.length < 4) {
      return ErrorFactory.validation('사용자 ID는 최소 4자 이상이어야 합니다', 'userId');
    }
    if (userId.length > 20) {
      return ErrorFactory.validation('사용자 ID는 20자를 초과할 수 없습니다', 'userId');
    }
    if (!/^[a-zA-Z0-9_]+$/.test(userId)) {
      return ErrorFactory.validation('사용자 ID는 영문, 숫자, 언더스코어만 사용 가능합니다', 'userId');
    }
    return null;
  },

  /**
   * 이메일 유효성 검증
   */
  validateEmail(email: string): ValidationError | null {
    if (!email || email.trim().length === 0) {
      return ErrorFactory.validation('이메일은 필수입니다', 'email');
    }
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailRegex.test(email)) {
      return ErrorFactory.validation('유효한 이메일 주소를 입력해주세요', 'email');
    }
    return null;
  },

  /**
   * 계정 공개 설정 유효성 검증
   */
  validateAccountVisibility(visibility: string): ValidationError | null {
    if (!Object.values(AccountVisibility).includes(visibility as AccountVisibility)) {
      return ErrorFactory.validation('유효하지 않은 계정 공개 설정입니다', 'accountVisibility');
    }
    return null;
  },
} as const;

/**
 * 친구 데이터 유효성 검증
 */
export const FriendValidator = {
  /**
   * 친구 ID 유효성 검증
   */
  validateFriendId(friendId: string): ValidationError | null {
    if (!friendId || friendId.trim().length === 0) {
      return ErrorFactory.validation('친구 ID는 필수입니다', 'friendId');
    }
    if (!IdValidator.isValidFriendId(friendId)) {
      return ErrorFactory.validation('유효하지 않은 친구 ID 형식입니다', 'friendId');
    }
    return null;
  },
} as const;
