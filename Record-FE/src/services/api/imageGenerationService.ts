import { apiClient } from './client';
import { Result } from '../../utils/result';

/**
 * 백엔드 PromptRequest 타입과 일치하도록 설계
 */
export interface ImageGenerationRequest {
  title: string;
  review: string;

  genre?: string;
  location?: string;
  date?: string;
  cast?: string[];

  imageRequest?: string;
  size?: string;
  n?: number;
  basePrompt?: string;
}

/**
 * 백엔드 ImageResponse 구조
 */
export interface ImageGenerationResponse {
  prompt: string;
  imageUrl: string;
  error?: string;
}

const USE_MOCK_DATA = false;

export const imageGenerationService = {
  /**
   * 실제 이미지 생성 API
   */
  async generateImage(
    request: ImageGenerationRequest
  ): Promise<Result<ImageGenerationResponse>> {

    if (USE_MOCK_DATA) {
      console.log('🧪 MOCK 이미지 생성 실행');

      return {
        ok: true,
        value: {
          prompt: `Mock Prompt for ${request.title}`,
          imageUrl: 'https://via.placeholder.com/1024x1024?text=Mock+Image',
        },
      };
    }

    console.log('🖼 이미지 생성 요청:', request);

    return apiClient.post<ImageGenerationResponse>('/generate-image', request, {
      timeoutMs: 60000,
    });
  },

  /**
   * 파일 포함 버전 (문서 상 존재)
   * POST /generate-image/with-file
   */
  async generateImageWithFile(
    request: ImageGenerationRequest,
    file: { uri: string; type: string; name: string }
  ): Promise<Result<ImageGenerationResponse>> {

    const formData = new FormData();
    formData.append('request', JSON.stringify(request));
    formData.append('file', file as any);

    console.log("🖼 파일 포함 이미지 생성:", request, file);

    return apiClient.postForm<ImageGenerationResponse>(
      '/generate-image/with-file',
      formData,
      { timeoutMs: 60000 }
    );
  },
};
