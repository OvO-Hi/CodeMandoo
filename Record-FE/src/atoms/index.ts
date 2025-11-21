/**
 * 중앙 집중식 Atoms 내보내기
 * 모든 상태 관리 atoms를 한 곳에서 관리하여 import 편의성 제공
 */

// 티켓 관련 atoms
export {
  // 기본 상태
  ticketsMapAtom,
  
  // 파생 atoms (읽기 전용)
  ticketsAtom,
  ticketsCountAtom,
  publicTicketsAtom,
  privateTicketsAtom,
  filteredTicketsAtom,
  getTicketByIdAtom,
  
  // 쓰기 atoms (액션)
  addTicketAtom,
  updateTicketAtom,
  deleteTicketAtom,
  
  // 유틸리티 atoms
  createFilteredTicketsAtom,
  ticketStatsAtom,
  ticketFilterOptionsAtom,
  updateFilterOptionsAtom,
  resetFiltersAtom,
  searchTicketsAtom,
  recentTicketsAtom,
  ticketsWithReviewsAtom,
  ticketsWithImagesAtom,
  createTicketsByStatusAtom,
  
  // AI 이미지 생성 관련
  basePromptAtom,
} from './ticketAtoms';

// 사용자 관련 atoms
export {
  // 기본 상태
  userProfileAtom,
  userSettingsAtom,
  userAuthAtom,
  
  // 파생 atoms (읽기 전용)
  currentUserIdAtom as userCurrentIdAtom, // 이름 충돌 방지
  userDisplayNameAtom,
  isAccountPublicAtom,
  profileCompletenessAtom,
  
  // 쓰기 atoms (액션)
  updateUserProfileAtom,
  updateUserSettingsAtom,
  updateEmailVerificationAtom,
  updateLastLoginAtom,
  
  // 유틸리티 atoms
  resetUserDataAtom,
} from './userAtoms';

// 친구 관련 atoms
export {
  // 기본 상태
  friendsMapAtom,
  friendRequestsMapAtom,
  friendshipsMapAtom,
  friendTicketsMapAtom,
  
  // 파생 atoms (읽기 전용)
  friendsAtom,
  friendsCountAtom,
  receivedFriendRequestsAtom,
  sentFriendRequestsAtom,
  getFriendByIdAtom,
  getFriendTicketsAtom,
  friendTicketsAtom,
  
  // 쓰기 atoms (액션)
  sendFriendRequestAtom,
  respondToFriendRequestAtom,
  removeFriendAtom,
  updateFriendTicketsAtom,
  
  // 유틸리티 atoms
  searchFriendsAtom,
  friendStatsAtom,
} from './friendsAtoms';

// 타입 내보내기 (편의성을 위해)
export type {
  // 티켓 타입
  Ticket,
  CreateTicketData,
  UpdateTicketData,
  TicketFilterOptions,
  TicketReview,
  TicketFormData,
} from '../types/ticket';

export type {
  // 사용자 타입
  UserProfile,
  UserSettings,
  UserAuth,
  UpdateUserProfileData,
  UpdateUserSettingsData,
} from '../types/user';

export type {
  // 친구 타입
  Friend,
  FriendRequest,
  Friendship,
  FriendTicketsMap,
  FriendSearchResult,
  CreateFriendRequestData,
  RespondToFriendRequestData,
} from '../types/friend';

export type {
  // 에러 및 결과 타입
  AppError,
  ValidationError,
  Result,
} from '../types/errors';

export {
  // 열거형
  TicketStatus,
  UserRole,
  FriendRequestStatus,
  AccountVisibility,
  TicketCategory,
  ErrorType,
  CONSTANTS,
  // UI 레이블
  TICKET_STATUS_LABELS,
  ACCOUNT_VISIBILITY_LABELS,
} from '../types/enums';
