/**
 * OCR API 서비스 — iOS/Android 완전 대응 버전
 */

import axios from 'axios';
import AsyncStorage from '@react-native-async-storage/async-storage';
import { Asset } from 'react-native-image-picker';
import { API_BASE_URL } from './client';
import { ErrorFactory, Result, ResultFactory } from '../../utils/result';

export type OCRResult = Record<string, string>;

class OcrService {
  /** 파일 업로드용 FormData 생성 */
  private buildFormData(asset: Asset): FormData {
    if (!asset.uri) {
      throw new Error('이미지를 불러올 수 없습니다.');
    }

    const formData = new FormData();
    formData.append('file', {
      uri: asset.uri,
      name: asset.fileName || 'ticket.jpg',
      type: asset.type || 'image/jpeg',
    } as any);

    return formData;
  }

  private async getAuthHeaders(): Promise<Record<string, string>> {
    try {
      const token = await AsyncStorage.getItem('authToken');
      return token ? { Authorization: `Bearer ${token}` } : {};
    } catch (error) {
      console.warn('Failed to load auth token for OCR upload', error);
      return {};
    }
  }

  private async postForm<T>(path: string, asset: Asset): Promise<Result<T>> {
    try {
      const formData = this.buildFormData(asset);
      const authHeaders = await this.getAuthHeaders();
      const response = await axios.post(`${API_BASE_URL}${path}`, formData, {
        headers: {
          'Content-Type': 'multipart/form-data',
          ...authHeaders,
        },
        timeout: 20000,
      });

      const payload = response.data;

      if (payload?.success === false) {
        const message = payload?.message || 'OCR 처리 중 오류가 발생했습니다.';
        return ResultFactory.failure(
          ErrorFactory.api(
            payload?.error?.code || 'OCR_ERROR',
            message,
            payload?.error
          )
        );
      }

      const data = payload?.data ?? payload;
      return ResultFactory.success(data as T);
    } catch (error: any) {
      if (axios.isAxiosError(error)) {
        if (error.code === 'ECONNABORTED') {
          return ResultFactory.failure(ErrorFactory.timeout());
        }

        const message =
          error.response?.data?.message ||
          error.message ||
          'OCR 업로드 실패';

        return ResultFactory.failure(
          ErrorFactory.api(error.code || 'OCR_ERROR', message, error.response?.data)
        );
      }

      return ResultFactory.failure(
        ErrorFactory.unknown(
          error instanceof Error ? error.message : '알 수 없는 오류가 발생했습니다.'
        )
      );
    }
  }

  /** 티켓 전용 OCR */
  async extractTicket(asset: Asset): Promise<Result<OCRResult>> {
    console.log('📤 OCR 티켓 추출 요청');
    return this.postForm('/ocr/extract/ticket', asset);
  }

  /** key-value OCR */
  async extractKeyValue(asset: Asset): Promise<Result<OCRResult>> {
    console.log('📤 OCR extract 요청');
    return this.postForm('/ocr/extract', asset);
  }

  /** 공연 구조화 OCR */
  async extractStructured(asset: Asset): Promise<Result<any>> {
    console.log('📤 OCR structured 요청');
    return this.postForm('/ocr/structured', asset);
  }

  /** Raw OCR 텍스트 */
  async extractRaw(asset: Asset): Promise<Result<{ text: string }>> {
    console.log('📤 OCR raw 요청');
    return this.postForm('/ocr', asset);
  }
}

export const ocrService = new OcrService();
export default ocrService;
