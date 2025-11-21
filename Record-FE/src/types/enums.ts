/**
 * 앱 전체에서 사용되는 열거형과 상수 정의
 * 타입 안정성과 코드 일관성을 위한 중앙 집중식 관리
 */

// 티켓 공개 범위 열거형 (API/DB 저장용)
export enum TicketStatus {
  PUBLIC = 'public',
  PRIVATE = 'private',
}

// 티켓 공개 범위 UI 표시용 레이블
export const TICKET_STATUS_LABELS: Record<TicketStatus, string> = {
  [TicketStatus.PUBLIC]: '전체 공개',
  [TicketStatus.PRIVATE]: '나만 보기',
};

// 사용자 역할 열거형 (향후 확장 가능)
export enum UserRole {
  USER = 'user',
  ADMIN = 'admin',
}

// 친구 요청 상태 열거형
export enum FriendRequestStatus {
  PENDING = 'pending',
  ACCEPTED = 'accepted',
  REJECTED = 'rejected',
}

// 계정 프로필 공개 설정 열거형 (프로필 페이지 전용)
export enum AccountVisibility {
  PUBLIC = 'public',
  PRIVATE = 'private',
  FRIENDS_ONLY = 'friends_only',
}

// 계정 공개 설정 UI 표시용 레이블
export const ACCOUNT_VISIBILITY_LABELS: Record<AccountVisibility, string> = {
  [AccountVisibility.PUBLIC]: '전체 공개',
  [AccountVisibility.PRIVATE]: '비공개',
  [AccountVisibility.FRIENDS_ONLY]: '친구만',
};

// 티켓 카테고리 열거형
export enum TicketCategory {
  CONCERT = 'concert',
  MUSICAL = 'musical',
  OPERA = 'opera',
  THEATER = 'theater',
  SPORTS = 'sports',
  OTHER = 'other',
}

// 에러 타입 열거형
export enum ErrorType {
  VALIDATION_ERROR = 'validation_error',
  NETWORK_ERROR = 'network_error',
  NOT_FOUND = 'not_found',
  PERMISSION_DENIED = 'permission_denied',
  UNKNOWN_ERROR = 'unknown_error',
}

// 상수 정의
export const CONSTANTS = {
  // ID 생성 관련
  ID_PREFIX: {
    USER: 'user_',
    TICKET: 'ticket_',
    FRIEND: 'friend_',
    REQUEST: 'request_',
  },
  
  // 제한값
  LIMITS: {
    MAX_TICKETS_PER_USER: 1000,
    MAX_FRIENDS_PER_USER: 500,
    MAX_REVIEW_LENGTH: 6000, // 이유: 실제 사용자 후기(특히 STT 결과)가 1000자를 쉽게 넘기므로 저장 실패를 막기 위해 완화
    MAX_TITLE_LENGTH: 100,
  },
  
  // 기본값
  DEFAULTS: {
    TICKET_STATUS: TicketStatus.PUBLIC,
    ACCOUNT_VISIBILITY: AccountVisibility.PUBLIC,
    USER_ROLE: UserRole.USER,
  },
} as const;
