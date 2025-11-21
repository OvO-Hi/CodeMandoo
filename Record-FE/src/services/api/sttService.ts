/**
 * STT (Speech-to-Text) API 서비스 — 백엔드 명세서 100% 일치 완전 수정본
 */

import { apiClient } from './client';
import { Result } from '../../utils/result';

/**
 * 백엔드의 Transcription 엔티티 구조 기반 타입
 * 
 * 백엔드 Transcription 엔티티 필드:
 * - id: Long (PK)
 * - fileName: String
 * - resultText: String (STT 변환 결과 텍스트, DB의 result_text 컬럼)
 * - createdAt: LocalDateTime
 * - user: User (ManyToOne)
 * - summary: String (요약 결과)
 * - summaryType: ReviewType (요약 타입)
 */
export interface TranscriptionResponse {
  id: number | null;
  fileName: string;
  createdAt: string;
  resultText?: string; // 백엔드 Transcription 엔티티의 resultText 필드
  transcript?: string; // 하위 호환성을 위한 별칭 (resultText와 동일)
  summary: string | null;
  summaryType?: string | null; // ReviewType enum 값
  finalReview?: string | null; // 하위 호환성
}

/**
 * STT 서비스 (명세 일치)
 */
class SttService {

  /**
   * 1) Whisper STT 변환 + DB 저장
   * POST /stt/transcribe-and-save
   */
  async transcribeAndSave(
    audioUri: string,
    fileName: string = 'recording.m4a',
    fileType: string = 'audio/m4a'
  ): Promise<Result<TranscriptionResponse>> {

    const formData = new FormData();
    formData.append('file', {
      uri: audioUri,
      type: fileType,
      name: fileName,
    } as any);

    console.log('🎤 STT transcribe-and-save 요청 → FormData 생성 완료');

    // Whisper 호출은 ffmpeg 재인코딩 + OpenAI 업로드까지 시간이 오래 걸리므로
    // 기본 20초 타임아웃을 90초로 늘려 AbortError를 방지합니다.
    return apiClient.postForm('/stt/transcribe-and-save', formData, {
      timeoutMs: 90000,
    });
  }

  /**
   * 2) STT 텍스트 기반 요약
   * POST /review/summarize
   *
   * request body: ReviewRequest {
   *   transcriptionId?: number;
   *   text?: string;
   * }
   * 
   * response: ApiResponse<string> (요약된 텍스트 문자열)
   */
  async summarizeReview(
    text: string,
    transcriptionId?: number
  ): Promise<Result<string>> {

    console.log('📝 후기 요약 요청:', { text, transcriptionId });

    // OpenAI API 호출은 시간이 오래 걸릴 수 있으므로 타임아웃을 60초로 설정
    // 백엔드는 ApiResponse<string> 형태로 요약된 텍스트 문자열을 반환합니다.
    return apiClient.post<string>(
      '/review/summarize',
      {
        text,
        transcriptionId,
      },
      { timeoutMs: 60000 } // 60초
    );
  }

  /**
   * 3) 후기 조직화 (Organize)
   * POST /review/organize
   *
   * request: ReviewRequest 같은 구조
   * 
   * response: ApiResponse<string> (정리된 텍스트 문자열)
   */
  async organizeReview(
    text: string,
    transcriptionId?: number
  ): Promise<Result<string>> {

    // OpenAI API 호출은 시간이 오래 걸릴 수 있으므로 타임아웃을 60초로 설정
    // 백엔드는 ApiResponse<string> 형태로 정리된 텍스트 문자열을 반환합니다.
    return apiClient.post<string>(
      '/review/organize',
      {
        text,
        transcriptionId,
      },
      { timeoutMs: 60000 } // 60초
    );
  }

  /**
   * 4) 후기 finalize (최종본 확정)
   * POST /reviews/finalize
   */
  async finalizeReview(
    transcriptionId: number,
    extraNotes?: string
  ): Promise<Result<TranscriptionResponse>> {

    return apiClient.post('/reviews/finalize', {
      transcriptionId,
      extraNotes,
    });
  }
}

export const sttService = new SttService();
export default sttService;
