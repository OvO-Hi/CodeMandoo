# 상태 관리 시스템 (Jotai Atoms)

현업 수준의 안정적이고 확장 가능한 React Native 앱을 위한 Jotai 기반 상태 관리 시스템입니다.

## 🏗️ 아키텍처 개요

### 핵심 설계 원칙
- **타입 안전성**: TypeScript strict mode 완전 지원
- **성능 최적화**: Map 기반 데이터 구조로 O(1) 조회
- **에러 처리**: Result 패턴으로 안전한 에러 핸들링
- **모듈화**: 관심사 분리로 유지보수성 향상
- **확장성**: 새로운 기능 추가가 용이한 구조

### 폴더 구조
```
src/
├── atoms/
│   ├── ticketAtoms.ts      # 티켓 상태 관리
│   ├── userAtoms.ts        # 사용자 상태 관리
│   ├── friendsAtoms.ts     # 친구 관계 상태 관리
│   ├── index.ts            # 중앙 집중식 내보내기
│   └── README.md           # 이 문서
├── types/
│   ├── ticket.ts           # 티켓 관련 타입
│   ├── user.ts             # 사용자 관련 타입
│   ├── friend.ts           # 친구 관련 타입
│   ├── enums.ts            # 열거형 및 상수
│   └── errors.ts           # 에러 타입
└── utils/
    ├── idGenerator.ts      # UUID 생성 유틸리티
    └── validation.ts       # 데이터 유효성 검증
```

## 📊 상태 관리 모듈

### 1. 티켓 상태 관리 (`ticketAtoms.ts`)

#### 핵심 기능
- Map 기반 티켓 데이터 관리 (성능 최적화)
- 유효성 검증이 포함된 CRUD 작업
- 필터링 및 검색 기능
- 통계 정보 제공

#### 주요 Atoms

**기본 상태**
```typescript
ticketsMapAtom: Map<string, Ticket>  // 티켓 데이터 (key: ticketId)
currentUserIdAtom: string            // 현재 사용자 ID
```

**파생 상태 (읽기 전용)**
```typescript
ticketsAtom: Ticket[]                // 정렬된 티켓 배열
ticketsCountAtom: number             // 티켓 총 개수
publicTicketsAtom: Ticket[]          // 공개 티켓만
privateTicketsAtom: Ticket[]         // 비공개 티켓만
```

**액션 (쓰기)**
```typescript
addTicketAtom: (data: CreateTicketData) => Result<Ticket>
updateTicketAtom: (id: string, data: UpdateTicketData) => Result<Ticket>
deleteTicketAtom: (id: string) => Result<boolean>
```

#### 사용 예시
```typescript
import { useAtom } from 'jotai';
import { addTicketAtom, ticketsAtom } from '../atoms';

function TicketComponent() {
  const [tickets] = useAtom(ticketsAtom);
  const [, addTicket] = useAtom(addTicketAtom);

  const handleAddTicket = async () => {
    const result = addTicket({
      title: '콘서트',
      performedAt: new Date(),
      status: TicketStatus.PUBLIC,
      place: '올림픽공원',
    });

    if (result.success) {
      console.log('티켓 추가 성공:', result.data);
    } else {
      console.error('에러:', result.error.message);
    }
  };

  return (
    <div>
      {tickets.map(ticket => (
        <div key={ticket.id}>{ticket.title}</div>
      ))}
    </div>
  );
}
```

### 2. 사용자 상태 관리 (`userAtoms.ts`)

#### 핵심 기능
- 프로필, 설정, 인증 정보 분리 관리
- 유효성 검증이 포함된 업데이트
- 프로필 완성도 계산
- 로그아웃 시 데이터 초기화

#### 주요 Atoms

**기본 상태**
```typescript
userProfileAtom: UserProfile         // 프로필 정보
userSettingsAtom: UserSettings       // 앱 설정
userAuthAtom: UserAuth               // 인증 정보
```

**파생 상태**
```typescript
userDisplayNameAtom: string          // 표시 이름
isAccountPublicAtom: boolean         // 계정 공개 여부
profileCompletenessAtom: number      // 프로필 완성도 (%)
```

#### 사용 예시
```typescript
import { useAtom } from 'jotai';
import { updateUserProfileAtom, userProfileAtom } from '../atoms';

function ProfileComponent() {
  const [profile] = useAtom(userProfileAtom);
  const [, updateProfile] = useAtom(updateUserProfileAtom);

  const handleUpdateName = (newName: string) => {
    const result = updateProfile({ name: newName });
    
    if (!result.success) {
      alert(result.error.message);
    }
  };

  return <input value={profile.name} onChange={e => handleUpdateName(e.target.value)} />;
}
```

### 3. 친구 상태 관리 (`friendsAtoms.ts`)

#### 핵심 기능
- Map 기반 친구 데이터 관리
- 친구 요청 시스템
- 친구별 티켓 캐싱
- 친구 검색 및 통계

#### 주요 Atoms

**기본 상태**
```typescript
friendsMapAtom: Map<string, Friend>              // 친구 목록
friendRequestsMapAtom: Map<string, FriendRequest> // 친구 요청
friendTicketsMapAtom: FriendTicketsMap           // 친구별 티켓 캐시
```

**액션**
```typescript
sendFriendRequestAtom: (data: CreateFriendRequestData) => Result<FriendRequest>
respondToFriendRequestAtom: (data: RespondToFriendRequestData) => Result<boolean>
removeFriendAtom: (friendId: string) => Result<boolean>
```

## 🔧 유틸리티

### ID 생성 (`idGenerator.ts`)
```typescript
import { IdGenerator } from '../utils/idGenerator';

const ticketId = IdGenerator.ticket();    // "ticket_uuid-here"
const userId = IdGenerator.user();        // "user_uuid-here"
const friendId = IdGenerator.friend();    // "friend_uuid-here"
```

### 유효성 검증 (`validation.ts`)
```typescript
import { TicketValidator } from '../utils/validation';

const titleError = TicketValidator.validateTitle('');
if (titleError) {
  console.error(titleError.message); // "티켓 제목은 필수입니다"
}
```

### 에러 처리 (`errors.ts`)
```typescript
import { Result, ErrorFactory, ResultFactory } from '../types/errors';

// 성공 결과
const success: Result<string> = ResultFactory.success('데이터');

// 실패 결과
const failure: Result<string> = ResultFactory.failure(
  ErrorFactory.validation('유효하지 않은 입력')
);

// 결과 처리
if (success.success) {
  console.log(success.data);
} else {
  console.error(success.error.message);
}
```

## 🎯 타입 안전성

### 열거형 사용
```typescript
import { TicketStatus, AccountVisibility } from '../types/enums';

// 문자열 리터럴 대신 열거형 사용
const ticket: Ticket = {
  status: TicketStatus.PUBLIC,  // ✅ 타입 안전
  // status: '공개',            // ❌ 지양
};
```

### Result 패턴
```typescript
// 모든 atom 액션은 Result<T> 반환
const result = addTicket(ticketData);

if (result.success) {
  // result.data는 Ticket 타입
  console.log(result.data.title);
} else {
  // result.error는 AppError 타입
  console.error(result.error.message);
}
```

## 🚀 성능 최적화

### Map 기반 데이터 구조
- O(1) 조회 성능
- 메모리 효율성
- 불변성 보장

### 파생 상태 최적화
```typescript
// 자동 메모이제이션으로 불필요한 재계산 방지
const publicTickets = useAtom(publicTicketsAtom);
```

### 선택적 구독
```typescript
// 특정 티켓만 구독
const getTicket = useAtom(getTicketByIdAtom);
const ticket = getTicket('ticket-id');
```

## 📝 모범 사례

### 1. Atom 사용
```typescript
// ✅ 좋은 예
const [tickets] = useAtom(ticketsAtom);
const [, addTicket] = useAtom(addTicketAtom);

// ❌ 나쁜 예 - 직접 상태 변경
const [ticketsMap, setTicketsMap] = useAtom(ticketsMapAtom);
setTicketsMap(new Map()); // 유효성 검증 없이 직접 변경
```

### 2. 에러 처리
```typescript
// ✅ 좋은 예
const result = addTicket(data);
if (!result.success) {
  showErrorMessage(result.error.message);
  return;
}

// ❌ 나쁜 예 - 에러 무시
addTicket(data); // 결과 확인 없음
```

### 3. 타입 사용
```typescript
// ✅ 좋은 예
import { TicketStatus } from '../types/enums';
const status = TicketStatus.PUBLIC;

// ❌ 나쁜 예
const status = '공개'; // 문자열 리터럴
```

## 🔄 마이그레이션 가이드

기존 코드에서 새로운 상태 관리 시스템으로 마이그레이션하는 방법:

### 1. Import 변경
```typescript
// 이전
import { ticketsAtom, addTicketAtom } from '../atoms/ticketAtoms';

// 이후
import { ticketsAtom, addTicketAtom } from '../atoms';
```

### 2. 상태 업데이트 방식 변경
```typescript
// 이전
const [tickets, setTickets] = useAtom(ticketsAtom);
setTickets([...tickets, newTicket]);

// 이후
const [, addTicket] = useAtom(addTicketAtom);
const result = addTicket(newTicketData);
```

### 3. 에러 처리 추가
```typescript
// 이전
addTicket(data);

// 이후
const result = addTicket(data);
if (!result.success) {
  handleError(result.error);
}
```

이 상태 관리 시스템은 확장 가능하고 유지보수가 용이하며, 타입 안전성을 보장합니다. 새로운 기능을 추가할 때는 기존 패턴을 따라 구현하시기 바랍니다.
