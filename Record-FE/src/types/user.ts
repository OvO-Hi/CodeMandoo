import { UserRole, AccountVisibility } from './enums';

/**
 * 사용자 기본 프로필 정보
 */
export interface UserProfile {
  id: string;
  nickname: string;
  email: string;
  profileImage?: string | null;
  isAccountPrivate?: boolean;
  createdAt?: string | null;
  updatedAt?: string | null;
}


/**
 * 사용자 계정 설정
 */
export interface UserSettings {
  readonly id: string;
  accountVisibility: AccountVisibility;
  //TODO: 친구 요청 기능 구현 시 사용될 예정
  //allowFriendRequests: boolean;
  language: 'ko' | 'en';
  updatedAt: Date;
}

/**
 * 사용자 인증 정보
 */
export interface UserAuth {
  readonly id: string;
  readonly role: UserRole;
  lastLoginAt?: Date;
  isEmailVerified: boolean;
  readonly createdAt: Date;
}

/**
 * 완전한 사용자 정보
 */
export interface User {
  profile: UserProfile;
  settings: UserSettings;
  auth: UserAuth;
}

/**
 * 사용자 프로필 업데이트용 데이터
 */
export interface UpdateUserProfileData extends Partial<Omit<UserProfile, 'createdAt' | 'updatedAt'>> {}

/**
 * 사용자 설정 업데이트용 데이터
 */
export interface UpdateUserSettingsData extends Partial<Omit<UserSettings, 'user_id' | 'updatedAt'>> {}

/**
 * 새로운 사용자 생성용 데이터
 */
export interface CreateUserData extends Omit<UserProfile, 'id' | 'createdAt' | 'updatedAt'> {
  password: string;
  settings?: Partial<Omit<UserSettings, 'id' | 'updatedAt'>>;
}
