# 리뷰 CRUD 기능 테스트 가이드

## 🎯 테스트 목적
리뷰 생성, 조회, 수정, 삭제 기능이 올바르게 작동하는지 확인합니다.

**왜 이 테스트가 필요한가요?**
1. **기능 검증**: 새로 수정된 리뷰 시스템이 예상대로 작동하는지 확인
2. **데이터 무결성**: 리뷰와 관련된 모든 데이터가 올바르게 저장/수정/삭제되는지 확인
3. **권한 확인**: 사용자가 본인의 리뷰만 수정/삭제할 수 있는지 확인
4. **연관 관계**: 티켓, 질문 템플릿과의 관계가 올바르게 설정되는지 확인
5. **예외 처리**: 잘못된 데이터나 권한이 없을 때 적절한 오류가 발생하는지 확인

## 📋 사전 준비

### 1. 테스트 데이터 초기화
```bash
# 모든 테스트 데이터 초기화
POST /api/test/init/all

# 또는 개별 초기화
POST /api/test/init/user          # 테스트 사용자 생성
POST /api/test/init/question-templates  # 질문 템플릿 생성
```

### 2. 필요한 데이터 확인
- ✅ 테스트 사용자: `testuser`
- ✅ 질문 템플릿: 총 6개 (뮤지컬 3개, 밴드 2개, 공통 1개)
- ⚠️ 티켓 데이터: 수동으로 생성 필요

## 🧪 테스트 실행

### 방법 1: 전체 테스트 (권장)
```bash
POST /api/test/reviews/full-test/testuser
```
이 방법은 리뷰 생성 → 조회 → 수정 → 삭제를 순차적으로 실행합니다.

### 방법 2: 개별 테스트
```bash
# 1. 리뷰 생성
POST /api/test/reviews/create-test

# 2. 리뷰 조회
GET /api/test/reviews/list/testuser

# 3. 리뷰 수정 (reviewId는 생성된 리뷰의 ID 사용)
PATCH /api/test/reviews/update/{reviewId}?userId=testuser

# 4. 리뷰 삭제
DELETE /api/test/reviews/delete/{reviewId}?userId=testuser
```

## 🔍 예상 결과

### 성공적인 테스트 결과:
```
=== 리뷰 CRUD 테스트 시작 ===
1. 리뷰 생성 테스트...
   ✅ 리뷰 생성 성공! ID: 1
2. 리뷰 조회 테스트...
   ✅ 리뷰 조회 성공! 총 1개 리뷰
3. 리뷰 수정 테스트...
   ✅ 리뷰 수정 성공!
4. 리뷰 삭제 테스트...
   ✅ 리뷰 삭제 성공!
=== 모든 테스트 완료! ===
```

### 실패 시 확인사항:
1. **티켓 ID 오류**: 실제 존재하는 티켓 ID로 변경 필요
2. **질문 템플릿 ID 오류**: 실제 존재하는 템플릿 ID로 변경 필요
3. **사용자 권한 오류**: 올바른 사용자 ID 사용 확인
4. **데이터베이스 연결 오류**: DB 연결 상태 확인

## 🛠️ 문제 해결

### 티켓 데이터가 없는 경우:
```sql
-- 테스트용 티켓 생성
INSERT INTO tickets (user_id, performance_title, theater, genre, view_date, is_public, created_at, updated_at)
VALUES ('testuser', '테스트 공연', '테스트 극장', '뮤지컬', '2024-01-01', false, NOW(), NOW());
```

### 질문 템플릿이 없는 경우:
```bash
POST /api/test/init/question-templates
```

## 📊 테스트 체크리스트

- [ ] 테스트 데이터 초기화 완료
- [ ] 리뷰 생성 기능 정상 작동
- [ ] 리뷰 조회 기능 정상 작동
- [ ] 리뷰 수정 기능 정상 작동
- [ ] 리뷰 삭제 기능 정상 작동
- [ ] 권한 확인 기능 정상 작동 (본인 리뷰만 수정/삭제 가능)
- [ ] 질문 템플릿 연동 정상 작동
- [ ] 데이터베이스 트랜잭션 정상 작동

## ⚠️ 주의사항

1. **운영 환경에서 제거**: 테스트 컨트롤러들은 실제 서비스 배포 시 제거해야 합니다.
2. **데이터 정리**: 테스트 후 불필요한 데이터는 정리하는 것을 권장합니다.
3. **보안**: 테스트용 엔드포인트는 인증 없이 접근 가능하므로 주의하세요.

## 🔧 추가 테스트

### 실제 API 테스트:
```bash
# 실제 리뷰 생성 API 테스트
POST /api/reviews
Content-Type: application/json
X-User-Id: testuser

{
  "ticketId": 1,
  "summary": "실제 테스트 리뷰",
  "keywords": "테스트, 검증",
  "questions": [
    {
      "templateId": 1,
      "displayOrder": 1,
      "customText": "테스트 질문"
    }
  ]
}
```

이 가이드를 따라 테스트를 실행하면 리뷰 CRUD 기능이 올바르게 작동하는지 확인할 수 있습니다.
