/**
 * API 호출을 위한 기본 설정 및 에러 처리 (완전 수정본)
 */
export const API_BASE_URL = __DEV__
  ? 'http://localhost:8080'
  : 'https://api.ticketbook.app';


import { Result, ResultFactory, ErrorFactory } from '../../utils/result';
import AsyncStorage from '@react-native-async-storage/async-storage';

// API 기본 설정

const API_TIMEOUT = 20000; // 20초

// API 에러 타입
export interface ApiError {
  code: string;
  message: string;
  details?: any;
}

// API 응답 타입
export interface ApiResponse<T = any> {
  success: boolean;
  data?: T;
  error?: ApiError;
  message?: string;
}

// ----------------------
// ApiClient 클래스
// ----------------------
class ApiClient {
  private authToken: string | null = null;

  constructor() {
    this.loadTokenFromStorage();
  }

  /**
   * ⭐ 외부에서 토큰 재로딩이 필요할 때 호출
   */
  async ensureAuthToken() {
    await this.loadTokenFromStorage();
  }

  /**
   * ⭐ AsyncStorage에서 토큰 불러오기
   * 메모리에 이미 토큰이 있으면 스킵 (성능 최적화)
   */
  private async loadTokenFromStorage() {
    // 이미 메모리에 토큰이 있으면 스킵
    if (this.authToken) {
      return;
    }

    try {
      const token = await AsyncStorage.getItem('authToken');
      if (token) {
        this.authToken = token;
        if (__DEV__) console.log('🔑 Token loaded from storage');
      }
    } catch (e) {
      console.warn('Failed to load auth token', e);
    }
  }

  /**
   * ⭐ 토큰 저장
   */
  async setAuthToken(token: string) {
    this.authToken = token;
    try {
      await AsyncStorage.setItem('authToken', token);
      if (__DEV__) console.log('🔐 Token saved to storage');
    } catch (e) {
      console.warn('Failed to save token', e);
    }
  }

  /**
   * ⭐ 토큰 제거
   */
  async clearAuthToken() {
    this.authToken = null;
    try {
      await AsyncStorage.removeItem('authToken');
      if (__DEV__) console.log('🗑️ Token removed from storage');
    } catch (e) {
      console.warn('Failed to remove token', e);
    }
  }

  /**
   * ⭐ 저장된 토큰 가져오기 (외부에서 사용)
   */
  async getStoredToken(): Promise<string | null> {
    // 이미 메모리에 토큰이 있으면 반환
    if (this.authToken) {
      return this.authToken;
    }

    // AsyncStorage에서 토큰 불러오기
    try {
      const token = await AsyncStorage.getItem('authToken');
      if (token) {
        this.authToken = token;
        if (__DEV__) console.log('🔑 Token loaded from storage');
      }
      return token;
    } catch (e) {
      console.warn('Failed to load auth token', e);
      return null;
    }
  }

  /**
   * ⭐ 기본 헤더 (Content-Type 강제 제거)
   */
  private getHeaders(customHeaders?: Record<string, string>): Record<string, string> {
    const headers: Record<string, string> = {
      ...(customHeaders || {}),
    };

    // ❗ multipart 요청 때는 Content-Type 자동 생성 → 절대 강제 지정하면 안됨

    // Authorization 적용
    if (this.authToken) {
      headers.Authorization = `Bearer ${this.authToken}`;
    }

    return headers;
  }

  /**
   * ⭐ 내부 공통 요청 처리
   */
  private async request<T>(
    url: string,
    options: RequestInit = {},
    timeoutMs: number = API_TIMEOUT
  ): Promise<Result<T>> {
    try {
      // 인증이 필요 없는 엔드포인트(/auth/)는 토큰 로딩 스킵
      const needsAuth = !url.startsWith('/auth/');
      if (needsAuth) {
        await this.loadTokenFromStorage();
      }

      const fullUrl = url.startsWith('http') ? url : `${API_BASE_URL}${url}`;

      if (__DEV__) {
        console.log(`API Request: ${options.method || 'GET'} ${fullUrl}`);
      }

      // 타임아웃 설정
      const controller = new AbortController();
      const timeoutId = setTimeout(() => controller.abort(), timeoutMs);

      const response = await fetch(fullUrl, {
        ...options,
        headers: this.getHeaders(options.headers as Record<string, string>),
        signal: controller.signal,
      });

      clearTimeout(timeoutId);

      const contentType = response.headers.get('content-type');
      const responseText = await response.text();

      if (__DEV__) {
        console.log(`API Response: ${response.status} ${fullUrl}`);
      }

      // JSON 응답 처리
      if (contentType?.includes('application/json')) {
        let data: ApiResponse<T>;

        try {
          data = JSON.parse(responseText);
        } catch (e) {
          return ResultFactory.failure(
            ErrorFactory.api('PARSE_ERROR', 'JSON 파싱 실패: ' + responseText)
          );
        }

        if (response.ok && data.success) {
          return ResultFactory.success(data.data as T);
        }

        return this.handleHttpError(response.status, data);
      }

      // JSON 아니면 그냥 텍스트 반환
      if (response.ok) {
        return ResultFactory.success(responseText as T);
      }

      return ResultFactory.failure(
        ErrorFactory.api(`HTTP_${response.status}`, responseText)
      );
    } catch (error: any) {
      return this.handleError(error);
    }
  }

  // ----------------------
  // ✔ JSON 전송 요청
  // ----------------------

  async get<T>(url: string, config?: { timeoutMs?: number }): Promise<Result<T>> {
    return this.request<T>(url, { method: 'GET' }, config?.timeoutMs);
  }

  async post<T>(
    url: string,
    data?: any,
    options?: { headers?: Record<string, string>; timeoutMs?: number }
  ): Promise<Result<T>> {
    return this.request<T>(url, {
      method: 'POST',
      body: data ? JSON.stringify(data) : undefined,
      headers: {
        'Content-Type': 'application/json',
        ...(options?.headers || {}),
      },
    }, options?.timeoutMs);
  }

  async put<T>(
    url: string,
    data?: any,
    options?: { headers?: Record<string, string>; timeoutMs?: number }
  ): Promise<Result<T>> {
    return this.request<T>(url, {
      method: 'PUT',
      body: data ? JSON.stringify(data) : undefined,
      headers: {
        'Content-Type': 'application/json',
      },
    }, options?.timeoutMs);
  }

  // PATCH(부분 업데이트) 요청이 없어서 직접 추가했습니다.
  async patch<T>(
    url: string,
    data?: any,
    options?: { headers?: Record<string, string>; timeoutMs?: number }
  ): Promise<Result<T>> {
    return this.request<T>(url, {
      method: 'PATCH',
      body: data ? JSON.stringify(data) : undefined,
      headers: {
        'Content-Type': 'application/json',
        ...(options?.headers || {}),
      },
    }, options?.timeoutMs);
  }

  async delete<T>(
    url: string,
    data?: any,
    options?: { headers?: Record<string, string>; timeoutMs?: number }
  ): Promise<Result<T>> {
    return this.request<T>(url, {
      method: 'DELETE',
      body: data ? JSON.stringify(data) : undefined,
      headers: data ? { 'Content-Type': 'application/json' } : {},
    }, options?.timeoutMs);
  }

  // ----------------------
  // ✔ FormData 전송 요청
  // ----------------------

  async postForm<T>(
    url: string,
    formData: FormData,
    config?: { timeoutMs?: number }
  ): Promise<Result<T>> {
    return this.request<T>(url, {
      method: 'POST',
      body: formData,
      headers: {}, // Content-Type 자동 생성
    }, config?.timeoutMs);
  }

  async putForm<T>(
    url: string,
    formData: FormData,
    config?: { timeoutMs?: number }
  ): Promise<Result<T>> {
    return this.request<T>(url, {
      method: 'PUT',
      body: formData,
      headers: {},
    }, config?.timeoutMs);
  }

  async patchForm<T>(
    url: string,
    formData: FormData,
    config?: { timeoutMs?: number }
  ): Promise<Result<T>> {
    return this.request<T>(url, {
      method: 'PATCH',
      body: formData,
      headers: {},
    }, config?.timeoutMs);
  }

  // ----------------------
  // 에러 처리
  // ----------------------

  private handleHttpError<T>(status: number, data: ApiResponse<T>): Result<T> {
    if (status === 401) {
      this.clearAuthToken();
      if (__DEV__) console.warn('🔒 Unauthorized - token cleared');
    }

    const errorMessage = data?.message || data?.error?.message || '오류가 발생했습니다';

    switch (status) {
      case 400:
        return ResultFactory.failure(ErrorFactory.validation(errorMessage));
      case 401:
        return ResultFactory.failure(ErrorFactory.unauthorized(errorMessage));
      case 403:
        return ResultFactory.failure(ErrorFactory.forbidden(errorMessage));
      case 404:
        return ResultFactory.failure(ErrorFactory.notFound('리소스', errorMessage));
      case 500:
        return ResultFactory.failure(ErrorFactory.server(errorMessage));
      default:
        return ResultFactory.failure(
          ErrorFactory.api(`HTTP_${status}`, errorMessage)
        );
    }
  }

  private handleError(error: any): Result<any> {
    if (__DEV__) console.error('API Error:', error);

    if (error.name === 'AbortError') {
      return ResultFactory.failure(ErrorFactory.timeout());
    }
    if (error.message?.includes('Network')) {
      return ResultFactory.failure(ErrorFactory.network());
    }
    return ResultFactory.failure(
      ErrorFactory.unknown(error.message || '알 수 없는 오류 발생')
    );
  }
}

// 싱글톤 인스턴스
export const apiClient = new ApiClient();
export default apiClient;
