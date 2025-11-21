/**
 * 에러 처리 및 결과 타입 정의
 * 타입 안전한 에러 처리와 성공/실패 결과 관리
 */

import { ErrorType } from './enums';

/**
 * 기본 에러 인터페이스
 */
export interface AppError {
  type: ErrorType;
  message: string;
  code?: string;
  details?: Record<string, unknown>;
}

/**
 * 결과 타입 (성공 또는 실패)
 */
export type Result<T, E = AppError> = 
  | { success: true; data: T }
  | { success: false; error: E };

/**
 * 유효성 검증 에러
 */
export interface ValidationError extends AppError {
  type: ErrorType.VALIDATION_ERROR;
  field?: string;
}

/**
 * 에러 생성 헬퍼 함수들
 */
export const ErrorFactory = {
  validation(message: string, field?: string): ValidationError {
    return {
      type: ErrorType.VALIDATION_ERROR,
      message,
      field,
    };
  },

  notFound(resource: string, id: string): AppError {
    return {
      type: ErrorType.NOT_FOUND,
      message: `${resource} with id ${id} not found`,
      details: { resource, id },
    };
  },

  permissionDenied(action: string): AppError {
    return {
      type: ErrorType.PERMISSION_DENIED,
      message: `Permission denied for action: ${action}`,
      details: { action },
    };
  },

  network(message: string): AppError {
    return {
      type: ErrorType.NETWORK_ERROR,
      message,
    };
  },

  unknown(message: string): AppError {
    return {
      type: ErrorType.UNKNOWN_ERROR,
      message,
    };
  },
} as const;

/**
 * 결과 생성 헬퍼 함수들
 */
export const ResultFactory = {
  success<T>(data: T): Result<T> {
    return { success: true, data };
  },

  failure<T>(error: AppError): Result<T> {
    return { success: false, error };
  },
} as const;
