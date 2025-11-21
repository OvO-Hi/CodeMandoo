/**
 * User Service – FIXED VERSION
 */
import { Result, ResultFactory, ErrorFactory } from '../../utils/result';
import { apiClient } from './client';
import { userProfileAtom } from '../../atoms/userAtoms';
import { getDefaultStore } from 'jotai';

const store = getDefaultStore();

export interface UserProfile {
  id: string;
  nickname: string;
  email: string;
  profileImage?: string;
  isAccountPrivate: boolean;
  createdAt: string | null;
  updatedAt: string | null;
}

class UserService {
  private profile: UserProfile | null = null;

  async fetchMyProfile(): Promise<Result<UserProfile>> {
    try {
      const result = await apiClient.get<UserProfile>('/users/me');

      if (result.success && result.data) {
        this.profile = result.data;
        store.set(userProfileAtom, result.data);
      }
      return result;
    } catch {
      return ResultFactory.failure(
        ErrorFactory.unknown('프로필 정보를 불러올 수 없습니다.')
      );
    }
  }

  async updateProfileImage(file: {
    uri: string;
    type: string;
    name: string;
  }): Promise<Result<UserProfile>> {

    const formData = new FormData();
    formData.append('file', file as any);

    const result = await apiClient.putForm<UserProfile>(
      '/users/me/profile-image',
      formData
    );

    if (result.success) {
      // 이미지 업로드 후 전체 프로필 다시 fetch (가장 안정적)
      await this.fetchMyProfile();
      
      // 업로드 성공 시 최신 프로필 반환
      if (result.data) {
        return ResultFactory.success(result.data);
      }
    }

    return result;
  }

  async updateProfile(
    profileData: Partial<UserProfile>
  ): Promise<Result<UserProfile>> {
    try {
      const result = await apiClient.put<UserProfile>('/users/me', profileData);

      if (result.success && result.data) {
        this.profile = result.data;
        store.set(userProfileAtom, result.data);
      }

      return result;
    } catch {
      return ResultFactory.failure(
        ErrorFactory.unknown('프로필 수정 중 오류가 발생했습니다.')
      );
    }
  }

  async deleteAccount(password: string): Promise<Result<void>> {
    // DELETE body 전달이 가능해야지만 정상 작동함
    const result = await apiClient.delete<void>(
      '/users/me',
      undefined,
      { password }
    );

    if (result.success) {
      this.clearProfile();
    }

    return result;
  }

  clearProfile() {
    this.profile = null;
    store.set(userProfileAtom, null);
  }

  getProfile(): UserProfile | null {
    return this.profile;
  }
}

export const userService = new UserService();
export default userService;
