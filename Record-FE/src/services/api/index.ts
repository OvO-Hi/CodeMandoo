/**
 * API 서비스 통합 export
 * 모든 API 서비스를 한 곳에서 관리
 */

export { apiClient } from './client';

export { friendService } from './friendService';
export { ticketService } from './ticketService';
export { userService } from './userService';
export { ocrService } from './ocrService';
export { imageGenerationService } from './imageGenerationService';
export { sttService } from './sttService';

// === apiClient 관련 타입 ===
export type { ApiError } from './client';

// === Friend Service 타입 ===
export type {
  SearchFriendsParams,
  SearchFriendsResponse,
  GetFriendsResponse,
  GetFriendRequestsResponse,
} from './friendService';

// === Ticket Service 타입 ===
export type {
  TicketCreateRequest,
  TicketResponse,
} from '../../types/api/tickets';

// === User Service 타입 ===
export type {
  UserProfile,
  LoginData,
  RegisterData,
  UpdateProfileData,
  ChangePasswordData,
  AuthResponse,
} from './userService';

// === OCR 타입 ===
export type { OCRResult } from './ocrService';

// === Image Generation 타입 ===
export type {
  ImageGenerationRequest,
  ImageGenerationResponse,
} from './imageGenerationService';

// === STT 타입 ===
export type {
  TranscriptionResponse,
  SummaryResponse,
} from './sttService';
