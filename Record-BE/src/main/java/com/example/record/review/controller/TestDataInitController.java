package com.example.record.review.controller;

import com.example.record.review.entity.QuestionTemplate;
import com.example.record.review.entity.Ticket;
import com.example.record.review.repository.QuestionTemplateRepository;
import com.example.record.review.repository.TicketRepository;
import com.example.record.user.User;
import com.example.record.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * 테스트 데이터 초기화 컨트롤러
 * 
 * 리뷰 테스트를 위한 기본 데이터를 생성합니다.
 * 실제 운영 환경에서는 제거해야 합니다.
 * 
 * 왜 이 컨트롤러가 필요한가요?
 * 1. 테스트 환경 구축: 리뷰 기능을 테스트하기 위한 기본 데이터 제공
 * 2. 개발 효율성: 매번 수동으로 데이터를 만들지 않고 자동으로 생성
 * 3. 일관성 보장: 모든 개발자가 동일한 테스트 데이터로 테스트 가능
 * 4. 초기 설정: 질문 템플릿, 사용자 등 필수 데이터를 한 번에 생성
 */
@RestController
@RequestMapping("/api/test/init")
@RequiredArgsConstructor
public class TestDataInitController {

    private final UserRepository userRepository;
    private final QuestionTemplateRepository questionTemplateRepository;
    private final TicketRepository ticketRepository;

    /**
     * 테스트용 사용자 생성
     * 
     * 리뷰 테스트를 위한 기본 사용자를 생성합니다.
     * 
     * 왜 이 메서드가 필요한가요?
     * 1. 권한 테스트: 사용자별 리뷰 접근 권한을 테스트하기 위함
     * 2. 데이터 연관성: 리뷰는 반드시 사용자와 연결되어야 하므로 필요
     * 3. 일관성: 항상 동일한 사용자로 테스트하여 결과 비교 가능
     * 4. 편의성: 매번 새로운 사용자를 만들지 않고 재사용 가능
     */
    @PostMapping("/user")
    public ResponseEntity<String> createTestUser() {
        try {
            // 테스트용 사용자 데이터 생성
            // 왜 이렇게 구성했나요?
            // 1. 간단한 ID: "testuser"로 기억하기 쉽게 설정
            // 2. 유효한 이메일: 실제 이메일 형식을 사용하여 검증 로직 테스트
            // 3. 기본 권한: "USER" 권한으로 일반 사용자 시나리오 테스트
            // 4. 현실적인 닉네임: 실제 서비스에서 사용될 수 있는 닉네임 사용
            User testUser = User.builder()
                    .id("testuser")
                    .email("test@example.com")
                    .password("password123")
                    .nickname("테스트사용자")
                    .role("USER")
                    .build();

            userRepository.save(testUser);
            return ResponseEntity.ok("테스트 사용자 생성 완료: testuser");
        } catch (Exception e) {
            // 사용자 생성 실패 시 오류 메시지 반환
            // 왜 try-catch를 사용하나요?
            // 1. 중복 사용자 오류 처리: 이미 존재하는 사용자일 경우 대응
            // 2. 데이터베이스 오류 처리: DB 연결 문제 등 예외 상황 대응
            // 3. 사용자 친화적 오류: 기술적 오류를 이해하기 쉬운 메시지로 변환
            return ResponseEntity.badRequest().body("사용자 생성 실패: " + e.getMessage());
        }
    }

    /**
     * 테스트용 질문 템플릿 생성
     * 
     * 리뷰 작성 시 사용할 질문 템플릿들을 생성합니다.
     * 
     * 왜 이 메서드가 필요한가요?
     * 1. 리뷰 질문 테스트: 리뷰에 질문을 추가하는 기능을 테스트하기 위함
     * 2. 장르별 질문: 뮤지컬, 밴드 등 장르에 맞는 다양한 질문 제공
     * 3. 카테고리 분류: 연기, 음악, 에너지 등 카테고리별 질문 구성
     * 4. 실제 사용 시나리오: 실제 서비스에서 사용될 수 있는 현실적인 질문들
     */
    @PostMapping("/question-templates")
    public ResponseEntity<String> createTestQuestionTemplates() {
        try {
            // 뮤지컬용 질문 템플릿들
            // 왜 뮤지컬 전용 질문이 필요한가요?
            // 1. 장르 특화: 뮤지컬만의 특징(연기, 음악, 무대)에 맞는 질문
            // 2. 사용자 경험: 뮤지컬 관객이 실제로 궁금해할 만한 질문들
            // 3. 데이터 분석: 뮤지컬 리뷰의 특성을 파악하기 위한 질문 구성
            QuestionTemplate musical1 = QuestionTemplate.builder()
                    .templateText("이 뮤지컬에서 가장 인상깊었던 장면은 무엇인가요?")
                    .category("PERFORMANCE")  // 공연 자체에 대한 질문
                    .genre("musical")  // 소문자로 저장 (DB와 일치시키기 위해)
                    .build();

            QuestionTemplate musical2 = QuestionTemplate.builder()
                    .templateText("배우들의 연기력은 어떠했나요?")
                    .category("ACTING")  // 연기력에 대한 질문
                    .genre("musical")  // 소문자로 저장
                    .build();

            QuestionTemplate musical3 = QuestionTemplate.builder()
                    .templateText("음악과 무대 연출은 만족스러웠나요?")
                    .category("MUSIC")  // 음악과 연출에 대한 질문
                    .genre("musical")  // 소문자로 저장
                    .build();

            // 밴드용 질문 템플릿들
            // 왜 밴드 전용 질문이 필요한가요?
            // 1. 장르 차이: 밴드 공연은 뮤지컬과 다른 특성을 가짐
            // 2. 관객 관점: 밴드 팬들이 중요하게 생각하는 요소들 반영
            // 3. 에너지 중심: 밴드 공연의 핵심인 에너지와 사운드에 집중
            QuestionTemplate band1 = QuestionTemplate.builder()
                    .templateText("밴드의 무대 에너지는 어떠했나요?")
                    .category("ENERGY")  // 에너지에 대한 질문
                    .genre("band")  // 소문자로 저장 (DB와 일치시키기 위해)
                    .build();

            QuestionTemplate band2 = QuestionTemplate.builder()
                    .templateText("음악의 질과 사운드는 만족스러웠나요?")
                    .category("SOUND")  // 사운드에 대한 질문
                    .genre("band")  // 소문자로 저장
                    .build();

            // 공통 질문 템플릿들
            // 왜 공통 질문이 필요한가요?
            // 1. 전체 평가: 장르에 관계없이 공연 전체에 대한 평가
            // 2. 비교 분석: 서로 다른 장르의 공연을 비교할 때 사용
            // 3. 기본 질문: 모든 리뷰에 포함될 수 있는 기본적인 질문
            // 뮤지컬 추가 질문들
            QuestionTemplate musical4 = QuestionTemplate.builder()
                    .templateText("이 공연을 보게 된 계기는?")
                    .category("OVERALL")
                    .genre("musical")  // 소문자로 저장
                    .build();

            QuestionTemplate musical5 = QuestionTemplate.builder()
                    .templateText("가장 인상 깊었던 순간은?")
                    .category("PERFORMANCE")
                    .genre("musical")  // 소문자로 저장
                    .build();

            QuestionTemplate musical6 = QuestionTemplate.builder()
                    .templateText("다시 본다면 어떤 점이 기대되나요?")
                    .category("OVERALL")
                    .genre("musical")  // 소문자로 저장
                    .build();

            QuestionTemplate musical7 = QuestionTemplate.builder()
                    .templateText("주인공의 성장 과정이 잘 드러났나요?")
                    .category("STORY")
                    .genre("musical")  // 소문자로 저장
                    .build();

            QuestionTemplate musical8 = QuestionTemplate.builder()
                    .templateText("무대 디자인과 조명은 어떠했나요?")
                    .category("STAGE")
                    .genre("musical")  // 소문자로 저장
                    .build();

            // 밴드 추가 질문들
            QuestionTemplate band3 = QuestionTemplate.builder()
                    .templateText("이 공연을 보게 된 계기는?")
                    .category("OVERALL")
                    .genre("band")  // 소문자로 저장
                    .build();

            QuestionTemplate band4 = QuestionTemplate.builder()
                    .templateText("가장 인상 깊었던 순간은?")
                    .category("PERFORMANCE")
                    .genre("band")  // 소문자로 저장
                    .build();

            QuestionTemplate band5 = QuestionTemplate.builder()
                    .templateText("다시 본다면 어떤 점이 기대되나요?")
                    .category("OVERALL")
                    .genre("band")  // 소문자로 저장
                    .build();

            QuestionTemplate band6 = QuestionTemplate.builder()
                    .templateText("밴드 멤버들의 호흡은 어떠했나요?")
                    .category("PERFORMANCE")
                    .genre("band")  // 소문자로 저장
                    .build();

            QuestionTemplate band7 = QuestionTemplate.builder()
                    .templateText("관객과의 소통은 어떠했나요?")
                    .category("PERFORMANCE")
                    .genre("band")  // 소문자로 저장
                    .build();

            // 공통 질문 템플릿들
            QuestionTemplate common1 = QuestionTemplate.builder()
                    .templateText("전체적으로 이 공연을 어떻게 평가하시나요?")
                    .category("OVERALL")  // 전체적인 평가에 대한 질문
                    .genre("common")  // 소문자로 저장
                    .build();

            QuestionTemplate common2 = QuestionTemplate.builder()
                    .templateText("이 공연을 보게 된 계기는?")
                    .category("OVERALL")
                    .genre("common")  // 소문자로 저장
                    .build();

            QuestionTemplate common3 = QuestionTemplate.builder()
                    .templateText("가장 인상 깊었던 순간은?")
                    .category("PERFORMANCE")
                    .genre("common")  // 소문자로 저장
                    .build();

            QuestionTemplate common4 = QuestionTemplate.builder()
                    .templateText("다시 본다면 어떤 점이 기대되나요?")
                    .category("OVERALL")
                    .genre("common")  // 소문자로 저장
                    .build();

            QuestionTemplate common5 = QuestionTemplate.builder()
                    .templateText("친구나 가족에게 이 공연을 추천하시겠어요?")
                    .category("OVERALL")
                    .genre("common")  // 소문자로 저장
                    .build();

            // 모든 질문 템플릿을 한 번에 저장
            // saveAll을 사용하는 이유:
            // 1. 효율성: 여러 개를 한 번에 저장하여 DB 접근 횟수 감소
            // 2. 트랜잭션: 모든 템플릿이 성공하거나 모두 실패하는 원자성 보장
            // 3. 성능: 개별 저장보다 빠른 처리 속도
            questionTemplateRepository.saveAll(List.of(
                    // 뮤지컬 질문 (8개)
                    musical1, musical2, musical3, musical4, musical5, musical6, musical7, musical8,
                    // 밴드 질문 (7개)
                    band1, band2, band3, band4, band5, band6, band7,
                    // 공통 질문 (5개)
                    common1, common2, common3, common4, common5
            ));

            return ResponseEntity.ok("질문 템플릿 생성 완료: 총 20개 (뮤지컬 8개, 밴드 7개, 공통 5개)");
        } catch (Exception e) {
            // 질문 템플릿 생성 실패 시 오류 메시지 반환
            return ResponseEntity.badRequest().body("질문 템플릿 생성 실패: " + e.getMessage());
        }
    }

    /**
     * 테스트용 티켓 생성
     * 
     * 리뷰 테스트를 위한 기본 티켓들을 생성합니다.
     * 
     * 왜 이 메서드가 필요한가요?
     * 1. 리뷰 연동: 리뷰는 반드시 티켓과 연결되어야 하므로 필요
     * 2. 다양한 장르: 뮤지컬, 밴드 등 다양한 장르의 티켓 제공
     * 3. 테스트 시나리오: 여러 개의 티켓으로 다양한 리뷰 테스트 가능
     * 4. 데이터 무결성: FK 제약조건을 만족시키기 위해 티켓 데이터 필요
     */
    @PostMapping("/tickets")
    public ResponseEntity<String> createTestTickets() {
        try {
            // 테스트 사용자 조회 (이미 생성되어 있어야 함)
            User testUser = userRepository.findById("testuser")
                    .orElseThrow(() -> new IllegalArgumentException("테스트 사용자가 존재하지 않습니다. 먼저 /api/test/init/user를 호출하세요."));

            // 뮤지컬 티켓 생성
            Ticket musicalTicket = Ticket.builder()
                    .user(testUser)
                    .performanceTitle("오페라의 유령")
                    .theater("세종문화회관 대극장")
                    .genre("MUSICAL")
                    .viewDate(LocalDate.of(2025, 10, 15))
                    .imageUrl("https://example.com/musical-ticket.jpg")  // 테스트용 더미 이미지 URL
                    .isPublic(true)
                    .build();

            // 밴드 티켓 생성
            Ticket bandTicket = Ticket.builder()
                    .user(testUser)
                    .performanceTitle("콜드플레이 내한공연")
                    .theater("올림픽공원 KSPO DOME")
                    .genre("BAND")
                    .viewDate(LocalDate.of(2025, 10, 20))
                    .imageUrl("https://example.com/band-ticket.jpg")  // 테스트용 더미 이미지 URL
                    .isPublic(true)
                    .build();

            // 모든 티켓을 한 번에 저장
            ticketRepository.saveAll(List.of(musicalTicket, bandTicket));

            return ResponseEntity.ok("테스트 티켓 생성 완료: 뮤지컬 1개, 밴드 1개");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("티켓 생성 실패: " + e.getMessage());
        }
    }

    /**
     * 모든 테스트 데이터 초기화
     * 
     * 리뷰 테스트에 필요한 모든 기본 데이터를 한 번에 생성합니다.
     * 
     * 왜 이 메서드가 필요한가요?
     * 1. 원스톱 초기화: 여러 단계를 거치지 않고 한 번에 모든 데이터 생성
     * 2. 테스트 준비: 리뷰 테스트를 실행하기 전에 필요한 모든 준비 완료
     * 3. 순서 보장: 사용자 → 질문 템플릿 순서로 생성하여 의존성 문제 방지
     * 4. 결과 확인: 각 단계별로 성공/실패 여부를 명확히 표시
     */
    @PostMapping("/all")
    public ResponseEntity<String> initAllTestData() {
        // 초기화 결과를 저장할 StringBuilder 사용
        // 왜 StringBuilder를 사용하나요?
        // 1. 단계별 결과 추가: 각 단계의 성공/실패를 순차적으로 기록
        // 2. 가독성: 사용자가 초기화 과정을 쉽게 파악할 수 있음
        // 3. 디버깅: 어느 단계에서 실패했는지 쉽게 확인 가능
        StringBuilder result = new StringBuilder();
        result.append("=== 테스트 데이터 초기화 시작 ===\n");

        try {
            // 1. 사용자 생성
            // 왜 사용자를 먼저 생성하나요?
            // 1. 의존성: 리뷰는 사용자와 연결되어야 하므로 사용자가 먼저 존재해야 함
            // 2. 권한 확인: 사용자별 권한 테스트를 위해 사용자 데이터가 필요
            // 3. 데이터 무결성: FK 제약조건을 만족시키기 위해 참조되는 데이터가 먼저 생성되어야 함
            result.append("1. 테스트 사용자 생성...\n");
            createTestUser();
            result.append("   ✅ 사용자 생성 완료\n");

            // 2. 질문 템플릿 생성
            // 왜 질문 템플릿을 두 번째로 생성하나요?
            // 1. 리뷰 질문 연동: 리뷰에 질문을 추가할 때 템플릿이 필요
            // 2. 데이터 참조: ReviewQuestion이 QuestionTemplate을 참조하므로 템플릿이 먼저 존재해야 함
            // 3. 테스트 완성도: 모든 기능을 테스트하기 위해 질문 템플릿이 필요
            result.append("2. 질문 템플릿 생성...\n");
            createTestQuestionTemplates();
            result.append("   ✅ 질문 템플릿 생성 완료\n");

            // 3. 테스트 티켓 생성
            // 왜 티켓을 세 번째로 생성하나요?
            // 1. 리뷰 연동: 리뷰는 반드시 티켓과 연결되어야 하므로 필요
            // 2. 사용자 의존성: 티켓은 사용자와 연결되므로 사용자 생성 후에 생성
            // 3. 테스트 시나리오: 여러 개의 티켓으로 다양한 리뷰 테스트 가능
            result.append("3. 테스트 티켓 생성...\n");
            createTestTickets();
            result.append("   ✅ 테스트 티켓 생성 완료\n");

            result.append("=== 모든 테스트 데이터 초기화 완료! ===\n");
            result.append("이제 /api/test/reviews/full-test/testuser 로 테스트를 실행할 수 있습니다.\n");

            return ResponseEntity.ok(result.toString());

        } catch (Exception e) {
            // 초기화 중 오류 발생 시
            result.append("❌ 초기화 실패: ").append(e.getMessage()).append("\n");
            return ResponseEntity.badRequest().body(result.toString());
        }
    }
}
