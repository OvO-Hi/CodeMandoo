/**
 * API 데이터 관리를 위한 커스텀 훅
 * 로딩, 에러, 재시도 등의 공통 로직을 제공
 */

import { useEffect, useCallback } from 'react';
import { useAtom } from 'jotai';
import { Alert } from 'react-native';
import { networkStatusAtom, globalLoadingAtom, globalErrorAtom } from '../atoms/apiAtoms';
import { Result } from '../types/errors';

/**
 * API 데이터 훅의 옵션
 */
export interface UseApiDataOptions {
  /** 컴포넌트 마운트 시 자동으로 데이터를 가져올지 여부 */
  fetchOnMount?: boolean;
  /** 에러 발생 시 자동으로 Alert를 표시할지 여부 */
  showErrorAlert?: boolean;
  /** 재시도 가능 여부 */
  enableRetry?: boolean;
  /** 최대 재시도 횟수 */
  maxRetries?: number;
  /** 재시도 간격 (ms) */
  retryDelay?: number;
}

/**
 * API 데이터 관리 훅
 */
export function useApiData<T>(
  fetchAtom: any,
  dataAtom: any,
  loadingAtom: any,
  errorAtom: any,
  options: UseApiDataOptions = {}
) {
  const {
    fetchOnMount = true,
    showErrorAlert = true,
    enableRetry = true,
    maxRetries = 3,
    retryDelay = 1000,
  } = options;

  const [, fetch] = useAtom(fetchAtom);
  const [data] = useAtom(dataAtom);
  const [loading] = useAtom(loadingAtom);
  const [error] = useAtom(errorAtom);
  const [networkStatus] = useAtom(networkStatusAtom);
  const [, setGlobalLoading] = useAtom(globalLoadingAtom);
  const [, setGlobalError] = useAtom(globalErrorAtom);

  /**
   * 데이터 새로고침
   */
  const refresh = useCallback(async (force: boolean = true): Promise<Result<T> | undefined> => {
    if (!networkStatus) {
      if (showErrorAlert) {
        Alert.alert('네트워크 오류', '인터넷 연결을 확인해주세요.');
      }
      return;
    }

    try {
      setGlobalLoading(true);
      const result = await fetch(force) as Result<T>;
      
      if (!result.success && showErrorAlert) {
        Alert.alert('오류', result.error?.message || '데이터를 불러오는데 실패했습니다.');
      }
      
      return result;
    } catch (err) {
      const errorMessage = err instanceof Error ? err.message : '알 수 없는 오류가 발생했습니다';
      setGlobalError(errorMessage);
      
      if (showErrorAlert) {
        Alert.alert('오류', errorMessage);
      }
      
      return { success: false, error: { type: 'UNKNOWN_ERROR' as any, message: errorMessage } };
    } finally {
      setGlobalLoading(false);
    }
  }, [fetch, networkStatus, showErrorAlert, setGlobalLoading, setGlobalError]);

  /**
   * 재시도 로직
   */
  const retry = useCallback(async (retryCount: number = 0) => {
    if (!enableRetry || retryCount >= maxRetries) {
      return;
    }

    setTimeout(async () => {
      const result = await refresh(true);
      
      if (!result?.success) {
        await retry(retryCount + 1);
      }
    }, retryDelay * Math.pow(2, retryCount)); // 지수 백오프
  }, [enableRetry, maxRetries, retryDelay, refresh]);

  /**
   * 에러 발생 시 재시도 제안
   */
  const handleError = useCallback(() => {
    if (error && enableRetry && showErrorAlert) {
      const errorMessage = typeof error === 'string' ? error : '알 수 없는 오류가 발생했습니다';
      Alert.alert(
        '오류',
        errorMessage,
        [
          { text: '취소', style: 'cancel' },
          { 
            text: '재시도', 
            onPress: () => retry(0),
            style: 'default'
          }
        ]
      );
    }
  }, [error, enableRetry, showErrorAlert, retry]);

  /**
   * 컴포넌트 마운트 시 데이터 가져오기
   */
  useEffect(() => {
    if (fetchOnMount && networkStatus) {
      refresh(false); // 캐시된 데이터가 있으면 사용
    }
  }, [fetchOnMount, networkStatus, refresh]);

  /**
   * 에러 처리
   */
  useEffect(() => {
    if (error) {
      handleError();
    }
  }, [error, handleError]);

  return {
    data,
    loading,
    error,
    refresh,
    retry: () => retry(0),
    networkStatus,
  };
}

/**
 * 친구 데이터 관리 훅
 */
export function useFriendsData(options?: UseApiDataOptions) {
  const { 
    fetchFriendsAtom, 
    friendsAtom, 
    friendsLoadingAtom, 
    friendsErrorAtom 
  } = require('../atoms/friendsAtomsApi');

  return useApiData(
    fetchFriendsAtom,
    friendsAtom,
    friendsLoadingAtom,
    friendsErrorAtom,
    options
  );
}

/**
 * 친구 요청 데이터 관리 훅
 */
export function useFriendRequestsData(options?: UseApiDataOptions) {
  const { 
    fetchReceivedRequestsAtom, 
    receivedFriendRequestsAtom, 
    receivedRequestsLoadingAtom, 
    receivedRequestsErrorAtom 
  } = require('../atoms/friendsAtomsApi');

  return useApiData(
    fetchReceivedRequestsAtom,
    receivedFriendRequestsAtom,
    receivedRequestsLoadingAtom,
    receivedRequestsErrorAtom,
    options
  );
}

/**
 * 내 티켓 데이터 관리 훅
 */
export function useMyTicketsData(options?: UseApiDataOptions) {
  const { 
    fetchMyTicketsAtom, 
    myTicketsAtom, 
    myTicketsLoadingAtom, 
    myTicketsErrorAtom 
  } = require('../atoms/ticketsAtomsApi');

  return useApiData(
    fetchMyTicketsAtom,
    myTicketsAtom,
    myTicketsLoadingAtom,
    myTicketsErrorAtom,
    options
  );
}

/**
 * 사용자 프로필 데이터 관리 훅
 */
export function useUserProfileData(options?: UseApiDataOptions) {
  const { 
    fetchMyProfileAtom, 
    userProfileAtom, 
    userProfileLoadingAtom, 
    userProfileErrorAtom 
  } = require('../atoms/userAtomsApi');

  return useApiData(
    fetchMyProfileAtom,
    userProfileAtom,
    userProfileLoadingAtom,
    userProfileErrorAtom,
    options
  );
}

/**
 * 네트워크 상태 관리 훅
 */
export function useNetworkStatus() {
  const [networkStatus, setNetworkStatus] = useAtom(networkStatusAtom);

  const checkNetworkStatus = useCallback(async () => {
    try {
      // React Native에서는 NetInfo를 사용해야 함
      // 여기서는 간단한 fetch 테스트로 대체
      const response = await fetch('https://www.google.com', {
        method: 'HEAD',
        mode: 'no-cors',
      });
      setNetworkStatus(true);
      return true;
    } catch {
      setNetworkStatus(false);
      return false;
    }
  }, [setNetworkStatus]);

  useEffect(() => {
    checkNetworkStatus();
    
    // 주기적으로 네트워크 상태 확인 (30초마다)
    const interval = setInterval(checkNetworkStatus, 30000);
    
    return () => clearInterval(interval);
  }, [checkNetworkStatus]);

  return {
    networkStatus,
    checkNetworkStatus,
  };
}

/**
 * 전역 로딩 상태 관리 훅
 */
export function useGlobalLoading() {
  const [globalLoading] = useAtom(globalLoadingAtom);
  const [globalError, setGlobalError] = useAtom(globalErrorAtom);

  const clearGlobalError = useCallback(() => {
    setGlobalError(null);
  }, [setGlobalError]);

  return {
    globalLoading,
    globalError,
    clearGlobalError,
  };
}
