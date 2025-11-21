/**
 * 사용자 관련 API 연동 atoms
 * 프로필, 인증, 설정 등의 API 연동
 */

import { atom } from 'jotai';
import { atomWithStorage } from 'jotai/utils';
import { userService, apiClient } from '../services/api/index';
import { UserProfile, LoginData, RegisterData, UpdateProfileData, ChangePasswordData } from '../services/api/userService';
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
 * 사용자 프로필 상태
 */
export const userProfileStateAtom = atom<ApiState<UserProfile>>(createInitialApiState<UserProfile>());

/**
 * 인증 토큰 (보안 저장소)
 */
export const authTokenAtom = atomWithStorage<string | null>('auth_token', null);

/**
 * 리프레시 토큰
 */
export const refreshTokenAtom = atomWithStorage<string | null>('refresh_token', null);

/**
 * 인증 상태
 */
export const isAuthenticatedAtom = atom<boolean>((get) => {
  const token = get(authTokenAtom);
  return token !== null && token.length > 0;
});

/**
 * 사용자 설정 타입
 */
export interface UserSettings {
  notifications: {
    friendRequests: boolean;
    newTickets: boolean;
    reminders: boolean;
  };
  privacy: {
    profileVisibility: 'public' | 'friends' | 'private';
    ticketVisibility: 'public' | 'friends' | 'private';
  };
}

/**
 * 사용자 설정 상태
 */
export const userSettingsStateAtom = atom<ApiState<UserSettings>>(createInitialApiState<UserSettings>());

/**
 * 로그인
 */
export const loginAtom = atom(
  null,
  async (get, set, data: LoginData) => {
    try {
      const result = await userService.login(data);
      
      if (result.success && result.data) {
        const { user, token, refreshToken } = result.data;
        
        // 토큰 저장
        set(authTokenAtom, token);
        set(refreshTokenAtom, refreshToken);
        
        // API 클라이언트에 토큰 설정
        apiClient.setAuthToken(token);
        
        // 사용자 프로필 저장
        const currentState = get(userProfileStateAtom);
        set(userProfileStateAtom, apiStateHelpers.setSuccess(currentState, user));
        
        return result;
      } else {
        return result;
      }
    } catch (error) {
      const errorMessage = error instanceof Error ? error.message : '로그인에 실패했습니다';
      return ResultFactory.failure({ message: errorMessage, code: 'LOGIN_ERROR' });
    }
  }
);

/**
 * 회원가입
 */
export const registerAtom = atom(
  null,
  async (get, set, data: RegisterData) => {
    try {
      const result = await userService.register(data);
      
      if (result.success && result.data) {
        const { user, token, refreshToken } = result.data;
        
        // 토큰 저장
        set(authTokenAtom, token);
        set(refreshTokenAtom, refreshToken);
        
        // API 클라이언트에 토큰 설정
        apiClient.setAuthToken(token);
        
        // 사용자 프로필 저장
        const currentState = get(userProfileStateAtom);
        set(userProfileStateAtom, apiStateHelpers.setSuccess(currentState, user));
        
        return result;
      } else {
        return result;
      }
    } catch (error) {
      const errorMessage = error instanceof Error ? error.message : '회원가입에 실패했습니다';
      return ResultFactory.failure({ message: errorMessage, code: 'REGISTER_ERROR' });
    }
  }
);

/**
 * 로그아웃
 */
export const logoutAtom = atom(
  null,
  async (get, set) => {
    try {
      // 서버에 로그아웃 요청
      await userService.logout();
      
      // 로컬 상태 초기화
      set(authTokenAtom, null);
      set(refreshTokenAtom, null);
      set(userProfileStateAtom, apiStateHelpers.reset<UserProfile>());
      set(userSettingsStateAtom, apiStateHelpers.reset());
      
      // API 클라이언트 토큰 제거
      apiClient.clearAuthToken();
      
      return ResultFactory.success(true);
    } catch (error) {
      // 로그아웃은 로컬 상태만 초기화해도 됨
      set(authTokenAtom, null);
      set(refreshTokenAtom, null);
      set(userProfileStateAtom, apiStateHelpers.reset<UserProfile>());
      set(userSettingsStateAtom, apiStateHelpers.reset());
      apiClient.clearAuthToken();
      
      return ResultFactory.success(true);
    }
  }
);

/**
 * 내 프로필 조회
 */
export const fetchMyProfileAtom = atom(
  null,
  async (get, set, force: boolean = false) => {
    const currentState = get(userProfileStateAtom);
    
    if (!force && currentState.data && isCacheValid(currentState.lastFetch)) {
      return ResultFactory.success(currentState.data);
    }

    set(userProfileStateAtom, apiStateHelpers.setLoading(currentState));

    try {
      const result = await userService.fetchMyProfile();
      
      if (result.success && result.data) {
        set(userProfileStateAtom, apiStateHelpers.setSuccess(currentState, result.data));
        return result;
      } else {
        const errorMessage = result.error?.message || '프로필을 불러오는데 실패했습니다';
        set(userProfileStateAtom, apiStateHelpers.setError(currentState, errorMessage));
        return result;
      }
    } catch (error) {
      const errorMessage = error instanceof Error ? error.message : '알 수 없는 오류가 발생했습니다';
      set(userProfileStateAtom, apiStateHelpers.setError(currentState, errorMessage));
      return ResultFactory.failure({ message: errorMessage, code: 'UNKNOWN_ERROR' });
    }
  }
);

/**
 * 프로필 업데이트
 */
export const updateProfileAtom = atom(
  null,
  async (get, set, data: UpdateProfileData) => {
    try {
      // 낙관적 업데이트
      const currentState = get(userProfileStateAtom);
      if (currentState.data) {
        const updatedProfile = {
          ...currentState.data,
          ...data,
          updatedAt: new Date().toISOString(),
        };
        set(userProfileStateAtom, apiStateHelpers.setSuccess(currentState, updatedProfile));
      }

      // 실제 API 호출
      const result = await userService.updateProfile(data);
      
      if (result.success && result.data) {
        set(userProfileStateAtom, apiStateHelpers.setSuccess(currentState, result.data));
        return result;
      } else {
        // 실패 시 롤백
        set(fetchMyProfileAtom, true);
        return result;
      }
    } catch (error) {
      // 에러 시 롤백
      set(fetchMyProfileAtom, true);
      const errorMessage = error instanceof Error ? error.message : '프로필 업데이트에 실패했습니다';
      return ResultFactory.failure({ message: errorMessage, code: 'UPDATE_PROFILE_ERROR' });
    }
  }
);

/**
 * 비밀번호 변경
 */
export const changePasswordAtom = atom(
  null,
  async (get, set, data: ChangePasswordData) => {
    try {
      const result = await userService.changePassword(data);
      return result;
    } catch (error) {
      const errorMessage = error instanceof Error ? error.message : '비밀번호 변경에 실패했습니다';
      return ResultFactory.failure({ message: errorMessage, code: 'CHANGE_PASSWORD_ERROR' });
    }
  }
);

/**
 * 프로필 이미지 업로드
 */
export const uploadProfileImageAtom = atom(
  null,
  async (get, set, imageUri: string, fileName?: string) => {
    try {
      const result = await userService.uploadProfileImage(imageUri, fileName);
      
      if (result.success && result.data) {
        // 프로필 업데이트
        const currentState = get(userProfileStateAtom);
        if (currentState.data) {
          const updatedProfile = {
            ...currentState.data,
            profileImage: result.data.imageUrl,
            avatar: result.data.imageUrl,
            updatedAt: new Date().toISOString(),
          };
          set(userProfileStateAtom, apiStateHelpers.setSuccess(currentState, updatedProfile));
        }
        
        return result;
      } else {
        return result;
      }
    } catch (error) {
      const errorMessage = error instanceof Error ? error.message : '이미지 업로드에 실패했습니다';
      return ResultFactory.failure({ message: errorMessage, code: 'UPLOAD_IMAGE_ERROR' });
    }
  }
);

/**
 * 사용자 설정 조회
 */
export const fetchUserSettingsAtom = atom(
  null,
  async (get, set, force: boolean = false) => {
    const currentState = get(userSettingsStateAtom);
    
    if (!force && currentState.data && isCacheValid(currentState.lastFetch)) {
      return ResultFactory.success(currentState.data);
    }

    set(userSettingsStateAtom, apiStateHelpers.setLoading(currentState));

    try {
      const result = await userService.getUserSettings();
      
      if (result.success && result.data) {
        set(userSettingsStateAtom, apiStateHelpers.setSuccess(currentState, result.data));
        return result;
      } else {
        const errorMessage = result.error?.message || '설정을 불러오는데 실패했습니다';
        set(userSettingsStateAtom, apiStateHelpers.setError(currentState, errorMessage));
        return result;
      }
    } catch (error) {
      const errorMessage = error instanceof Error ? error.message : '알 수 없는 오류가 발생했습니다';
      set(userSettingsStateAtom, apiStateHelpers.setError(currentState, errorMessage));
      return ResultFactory.failure({ message: errorMessage, code: 'UNKNOWN_ERROR' });
    }
  }
);

/**
 * 사용자 설정 업데이트
 */
export const updateUserSettingsAtom = atom(
  null,
  async (get, set, settings: Parameters<typeof userService.updateUserSettings>[0]) => {
    try {
      // 낙관적 업데이트
      const currentState = get(userSettingsStateAtom);
      if (currentState.data) {
        const updatedSettings = {
          ...currentState.data,
          ...settings,
          notifications: {
            ...currentState.data.notifications,
            ...settings.notifications,
          },
          privacy: {
            ...currentState.data.privacy,
            ...settings.privacy,
          },
        };
        set(userSettingsStateAtom, apiStateHelpers.setSuccess(currentState, updatedSettings));
      }

      // 실제 API 호출
      const result = await userService.updateUserSettings(settings);
      
      if (result.success) {
        // 성공 시 설정 새로고침
        set(fetchUserSettingsAtom, true);
        return result;
      } else {
        // 실패 시 롤백
        set(fetchUserSettingsAtom, true);
        return result;
      }
    } catch (error) {
      // 에러 시 롤백
      set(fetchUserSettingsAtom, true);
      const errorMessage = error instanceof Error ? error.message : '설정 업데이트에 실패했습니다';
      return ResultFactory.failure({ message: errorMessage, code: 'UPDATE_SETTINGS_ERROR' });
    }
  }
);

/**
 * 계정 삭제
 */
export const deleteAccountAtom = atom(
  null,
  async (get, set, password: string) => {
    try {
      const result = await userService.deleteAccount(password);
      
      if (result.success) {
        // 성공 시 모든 상태 초기화
        set(logoutAtom);
        return result;
      } else {
        return result;
      }
    } catch (error) {
      const errorMessage = error instanceof Error ? error.message : '계정 삭제에 실패했습니다';
      return ResultFactory.failure({ message: errorMessage, code: 'DELETE_ACCOUNT_ERROR' });
    }
  }
);

/**
 * 토큰 갱신
 */
export const refreshTokenActionAtom = atom(
  null,
  async (get, set) => {
    try {
      const refreshToken = get(refreshTokenAtom);
      if (!refreshToken) {
        return ResultFactory.failure({ message: '리프레시 토큰이 없습니다', code: 'NO_REFRESH_TOKEN' });
      }

      const result = await userService.refreshToken(refreshToken);
      
      if (result.success && result.data) {
        const { token, refreshToken: newRefreshToken } = result.data;
        
        // 새 토큰 저장
        set(authTokenAtom, token);
        set(refreshTokenAtom, newRefreshToken);
        
        // API 클라이언트에 새 토큰 설정
        apiClient.setAuthToken(token);
        
        return result;
      } else {
        // 토큰 갱신 실패 시 로그아웃
        set(logoutAtom);
        return result;
      }
    } catch (error) {
      // 에러 시 로그아웃
      set(logoutAtom);
      const errorMessage = error instanceof Error ? error.message : '토큰 갱신에 실패했습니다';
      return ResultFactory.failure({ message: errorMessage, code: 'REFRESH_TOKEN_ERROR' });
    }
  }
);

/**
 * 읽기 전용 atoms (컴포넌트에서 사용)
 */
export const userProfileAtom = atom<UserProfile | null>((get) => {
  const state = get(userProfileStateAtom);
  return state.data;
});

export const userSettingsAtom = atom((get) => {
  const state = get(userSettingsStateAtom);
  return state.data;
});

/**
 * 로딩 상태 atoms
 */
export const userProfileLoadingAtom = atom<boolean>((get) => {
  const state = get(userProfileStateAtom);
  return state.loading === 'loading';
});

export const userSettingsLoadingAtom = atom<boolean>((get) => {
  const state = get(userSettingsStateAtom);
  return state.loading === 'loading';
});

/**
 * 에러 상태 atoms
 */
export const userProfileErrorAtom = atom<string | null>((get) => {
  const state = get(userProfileStateAtom);
  return state.error;
});

export const userSettingsErrorAtom = atom<string | null>((get) => {
  const state = get(userSettingsStateAtom);
  return state.error;
});
