/**
 * API 연동을 위한 공통 atoms
 * 로딩 상태, 에러 처리, 캐싱 등을 관리
 */

import { atom } from 'jotai';
import { atomWithStorage } from 'jotai/utils';

/**
 * 로딩 상태 타입
 */
export type LoadingState = 'idle' | 'loading' | 'success' | 'error';

/**
 * API 상태 인터페이스
 */
export interface ApiState<T> {
  data: T | null;
  loading: LoadingState;
  error: string | null;
  lastFetch: number | null;
}

/**
 * 초기 API 상태
 */
export const createInitialApiState = <T>(): ApiState<T> => ({
  data: null,
  loading: 'idle',
  error: null,
  lastFetch: null,
});

/**
 * 네트워크 연결 상태
 */
export const networkStatusAtom = atom<boolean>(true);

/**
 * 전역 로딩 상태 (여러 API 호출이 동시에 진행될 때)
 */
export const globalLoadingAtom = atom<boolean>(false);

/**
 * 전역 에러 상태
 */
export const globalErrorAtom = atom<string | null>(null);

/**
 * 인증 토큰 (보안을 위해 암호화된 저장소 사용 권장)
 */
export const authTokenAtom = atomWithStorage<string | null>('auth_token', null);

/**
 * 사용자 인증 상태
 */
export const isAuthenticatedAtom = atom<boolean>((get) => {
  const token = get(authTokenAtom);
  return token !== null && token.length > 0;
});

/**
 * API 캐시 설정
 */
export const API_CACHE_TIME = 5 * 60 * 1000; // 5분

/**
 * 캐시 유효성 검사 함수
 */
export const isCacheValid = (lastFetch: number | null): boolean => {
  if (!lastFetch) return false;
  return Date.now() - lastFetch < API_CACHE_TIME;
};

/**
 * API 상태 업데이트 헬퍼 함수들
 */
export const apiStateHelpers = {
  setLoading: <T>(state: ApiState<T>): ApiState<T> => ({
    ...state,
    loading: 'loading',
    error: null,
  }),

  setSuccess: <T>(state: ApiState<T>, data: T): ApiState<T> => ({
    ...state,
    data,
    loading: 'success',
    error: null,
    lastFetch: Date.now(),
  }),

  setError: <T>(state: ApiState<T>, error: string): ApiState<T> => ({
    ...state,
    loading: 'error',
    error,
  }),

  reset: <T>(): ApiState<T> => createInitialApiState<T>(),
};

/**
 * 옵티미스틱 업데이트를 위한 임시 상태 저장소
 */
export const optimisticUpdatesAtom = atom<Map<string, any>>(new Map());

/**
 * 재시도 설정
 */
export const RETRY_CONFIG = {
  maxRetries: 3,
  retryDelay: 1000, // 1초
  backoffMultiplier: 2,
};

/**
 * 재시도 카운터
 */
export const retryCountersAtom = atom<Map<string, number>>(new Map());
