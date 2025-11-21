// src/utils/resolveImageUrl.ts
// 배포 시 BASE_URL을 EC2 도메인으로 변경해야 함

export const resolveImageUrl = (url?: string | null): string | null => {
  if (!url) return null;

  // 갤러리에서 선택한 로컬 파일은 그대로 사용해야 함
  if (url.startsWith('file://')) return url;

  const BASE_URL = 'http://127.0.0.1:8080';

  const finalUrl = url.startsWith('http') ? url : `${BASE_URL}${url}`;

  return `${finalUrl}?t=${Date.now()}`;
};
