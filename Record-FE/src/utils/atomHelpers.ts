/**
 * Atom 헬퍼 유틸리티
 * 공통 패턴과 에러 처리를 표준화
 * 
 * @author TicketBookApp Team
 * @version 1.0.0
 * @since 2025-09-15
 */

import { Result, ErrorFactory, ResultFactory } from '../types/errors';

/**
 * 공통 try-catch + ResultFactory 패턴을 추상화한 헬퍼
 */
export const withErrorHandling = <T, P extends any[]>(
  operation: (...args: P) => T,
  errorMessage: string
) => {
  return (...args: P): Result<T> => {
    try {
      const result = operation(...args);
      return ResultFactory.success(result);
    } catch (error) {
      return ResultFactory.failure(
        ErrorFactory.unknown(
          error instanceof Error ? error.message : errorMessage
        )
      );
    }
  };
};

/**
 * 비동기 작업을 위한 에러 핸들링 헬퍼
 */
export const withAsyncErrorHandling = <T, P extends any[]>(
  operation: (...args: P) => Promise<T>,
  errorMessage: string
) => {
  return async (...args: P): Promise<Result<T>> => {
    try {
      const result = await operation(...args);
      return ResultFactory.success(result);
    } catch (error) {
      return ResultFactory.failure(
        ErrorFactory.unknown(
          error instanceof Error ? error.message : errorMessage
        )
      );
    }
  };
};

/**
 * 유효성 검증 헬퍼 타입
 */
export type ValidationRule<T> = {
  field: keyof T;
  validator: (value: any) => Error | null;
};

/**
 * 여러 필드에 대한 유효성 검증을 수행하는 헬퍼
 */
export const validateFields = <T extends Record<string, any>>(
  data: Partial<T>,
  rules: ValidationRule<T>[]
): Error | null => {
  for (const rule of rules) {
    const value = data[rule.field];
    if (value !== undefined) {
      const error = rule.validator(value);
      if (error) return error;
    }
  }
  return null;
};

/**
 * 객체의 정의된 필드만 필터링하는 헬퍼
 */
export const filterDefinedFields = <T extends Record<string, any>>(
  obj: T
): Partial<T> => {
  const result: Partial<T> = {};
  
  for (const [key, value] of Object.entries(obj)) {
    if (value !== undefined && value !== null && value !== '') {
      result[key as keyof T] = value;
    }
  }
  
  return result;
};

/**
 * Map 최적화 유틸리티
 */
export const optimizedMapUpdate = <K, V>(
  originalMap: Map<K, V>,
  key: K,
  value: V
): Map<K, V> => {
  // 값이 동일하면 원본 Map 반환 (불필요한 복사 방지)
  if (originalMap.get(key) === value) {
    return originalMap;
  }
  
  const newMap = new Map(originalMap);
  newMap.set(key, value);
  return newMap;
};

/**
 * Map에서 여러 키 삭제 (최적화된 버전)
 */
export const optimizedMapBulkDelete = <K, V>(
  originalMap: Map<K, V>,
  keysToDelete: K[]
): { newMap: Map<K, V>; deletedKeys: K[]; failedKeys: K[] } => {
  const newMap = new Map(originalMap);
  const deletedKeys: K[] = [];
  const failedKeys: K[] = [];
  
  for (const key of keysToDelete) {
    if (newMap.has(key)) {
      newMap.delete(key);
      deletedKeys.push(key);
    } else {
      failedKeys.push(key);
    }
  }
  
  return { newMap, deletedKeys, failedKeys };
};

/**
 * 권한 검증 헬퍼
 */
export const validateOwnership = (
  resourceUserId: string,
  currentUserId: string,
  action: string
): Error | null => {
  if (resourceUserId !== currentUserId) {
    return new Error(`권한이 없습니다: ${action}`);
  }
  return null;
};

/**
 * 배열 필터링 최적화 헬퍼
 */
export const createOptimizedFilter = <T>(
  items: T[],
  filters: Array<(item: T) => boolean>
): T[] => {
  return items.filter(item => filters.every(filter => filter(item)));
};

/**
 * 객체 필드 개수를 세는 헬퍼 (완성도 계산용)
 */
export const countDefinedFields = <T extends Record<string, any>>(
  obj: T,
  fieldsToCheck?: (keyof T)[]
): { defined: number; total: number } => {
  const fields = fieldsToCheck || Object.keys(obj) as (keyof T)[];
  
  const defined = fields.filter(field => {
    const value = obj[field];
    return value !== undefined && value !== null && value !== '';
  }).length;
  
  return {
    defined,
    total: fields.length
  };
};

/**
 * 가중치를 고려한 완성도 계산 헬퍼
 */
export const calculateWeightedCompleteness = <T extends Record<string, any>>(
  obj: T,
  weights: Partial<Record<keyof T, number>>,
  fieldsToCheck?: (keyof T)[]
): number => {
  const fields = fieldsToCheck || Object.keys(obj) as (keyof T)[];
  
  let totalWeight = 0;
  let completedWeight = 0;
  
  for (const field of fields) {
    const weight = weights[field] || 1;
    totalWeight += weight;
    
    const value = obj[field];
    if (value !== undefined && value !== null && value !== '') {
      completedWeight += weight;
    }
  }
  
  return totalWeight > 0 ? Math.round((completedWeight / totalWeight) * 100) : 0;
};

/**
 * 선택적 업데이트를 위한 변경 감지 헬퍼
 */
export const hasChanges = <T extends Record<string, any>>(
  current: T,
  updates: Partial<T>
): boolean => {
  for (const [key, value] of Object.entries(updates)) {
    if (current[key as keyof T] !== value) {
      return true;
    }
  }
  return false;
};

/**
 * 깊은 비교를 통한 변경 감지 (중첩 객체용)
 */
export const deepHasChanges = <T>(current: T, updates: Partial<T>): boolean => {
  for (const [key, value] of Object.entries(updates)) {
    const currentValue = (current as any)[key];
    
    if (typeof value === 'object' && value !== null && !Array.isArray(value)) {
      if (typeof currentValue !== 'object' || currentValue === null) {
        return true;
      }
      if (deepHasChanges(currentValue, value)) {
        return true;
      }
    } else if (currentValue !== value) {
      return true;
    }
  }
  return false;
};

/**
 * 타임스탬프 업데이트 헬퍼
 */
export const withTimestamp = <T extends { updatedAt?: Date }>(
  obj: T
): T => ({
  ...obj,
  updatedAt: new Date(),
});

/**
 * 안전한 객체 병합 헬퍼 (undefined 값 제외)
 */
export const safeMerge = <T extends Record<string, any>>(
  target: T,
  source: Partial<T>
): T => {
  const filtered = filterDefinedFields(source);
  return {
    ...target,
    ...filtered,
  };
};
