/**
 * Result 패턴 구현
 * API 응답의 성공/실패를 타입 안전하게 처리
 */

/**
 * 에러 인터페이스
 */
export interface AppError {
  code: string;
  message: string;
  details?: any;
}

/**
 * Result 타입 - 성공 또는 실패를 나타냄
 */
export type Result<T> = {
  success: true;
  data: T;
  error?: never;
} | {
  success: false;
  data?: never;
  error: AppError;
};

/**
 * Result 생성을 위한 팩토리 클래스
 */
export class ResultFactory {
  /**
   * 성공 Result 생성
   */
  static success<T>(data: T): Result<T> {
    return {
      success: true,
      data,
    };
  }

  /**
   * 실패 Result 생성
   */
  static failure<T>(error: AppError): Result<T> {
    return {
      success: false,
      error,
    };
  }
}

/**
 * 에러 생성을 위한 팩토리 클래스
 */
export class ErrorFactory {
  /**
   * API 에러
   */
  static api(code: string, message: string, details?: any): AppError {
    return {
      code,
      message,
      details,
    };
  }

  /**
   * 네트워크 에러
   */
  static network(message: string = '네트워크 연결을 확인해주세요'): AppError {
    return {
      code: 'NETWORK_ERROR',
      message,
    };
  }

  /**
   * 인증 에러
   */
  static unauthorized(message: string = '인증이 필요합니다'): AppError {
    return {
      code: 'UNAUTHORIZED',
      message,
    };
  }

  /**
   * 권한 에러
   */
  static forbidden(message: string = '접근 권한이 없습니다'): AppError {
    return {
      code: 'FORBIDDEN',
      message,
    };
  }

  /**
   * 찾을 수 없음 에러
   */
  static notFound(resource: string, identifier?: string): AppError {
    const message = identifier 
      ? `${resource}(${identifier})을(를) 찾을 수 없습니다`
      : `${resource}을(를) 찾을 수 없습니다`;
    
    return {
      code: 'NOT_FOUND',
      message,
    };
  }

  /**
   * 유효성 검사 에러
   */
  static validation(message: string, details?: any): AppError {
    return {
      code: 'VALIDATION_ERROR',
      message,
      details,
    };
  }

  /**
   * 서버 에러
   */
  static server(message: string = '서버 오류가 발생했습니다'): AppError {
    return {
      code: 'SERVER_ERROR',
      message,
    };
  }

  /**
   * 알 수 없는 에러
   */
  static unknown(message: string = '알 수 없는 오류가 발생했습니다'): AppError {
    return {
      code: 'UNKNOWN_ERROR',
      message,
    };
  }

  /**
   * 타임아웃 에러
   */
  static timeout(message: string = '요청 시간이 초과되었습니다'): AppError {
    return {
      code: 'TIMEOUT_ERROR',
      message,
    };
  }

  /**
   * 중복 에러
   */
  static duplicate(resource: string): AppError {
    return {
      code: 'DUPLICATE_ERROR',
      message: `이미 존재하는 ${resource}입니다`,
    };
  }
}

/**
 * Result 유틸리티 함수들
 */
export class ResultUtils {
  /**
   * Result가 성공인지 확인
   */
  static isSuccess<T>(result: Result<T>): result is { success: true; data: T } {
    return result.success;
  }

  /**
   * Result가 실패인지 확인
   */
  static isFailure<T>(result: Result<T>): result is { success: false; error: AppError } {
    return !result.success;
  }

  /**
   * Result에서 데이터 추출 (실패 시 기본값 반환)
   */
  static getData<T>(result: Result<T>, defaultValue: T): T {
    return result.success ? result.data : defaultValue;
  }

  /**
   * Result에서 에러 추출
   */
  static getError<T>(result: Result<T>): AppError | null {
    return result.success ? null : result.error;
  }

  /**
   * 여러 Result를 결합
   */
  static combine<T extends readonly unknown[]>(
    ...results: { [K in keyof T]: Result<T[K]> }
  ): Result<T> {
    for (const result of results) {
      if (!result.success) {
        return result as Result<T>;
      }
    }
    
    const data = results.map(result => (result as any).data) as unknown as T;
    return ResultFactory.success(data);
  }

  /**
   * Promise를 Result로 변환
   */
  static async fromPromise<T>(
    promise: Promise<T>,
    errorHandler?: (error: any) => AppError
  ): Promise<Result<T>> {
    try {
      const data = await promise;
      return ResultFactory.success(data);
    } catch (error) {
      const appError = errorHandler 
        ? errorHandler(error)
        : ErrorFactory.unknown(error instanceof Error ? error.message : '알 수 없는 오류');
      
      return ResultFactory.failure(appError);
    }
  }
}
