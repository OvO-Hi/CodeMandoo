package com.example.record.review.service;

import com.example.record.promptcontrol_w03.service.OpenAIChatService;
import com.example.record.review.entity.QuestionTemplate;
import com.example.record.review.entity.Review;
import com.example.record.review.entity.Ticket;
import com.example.record.review.entity.UserCustomQuestion;
import com.example.record.review.repository.QuestionTemplateRepository;
import com.example.record.review.repository.ReviewRepository;
import com.example.record.review.repository.TicketRepository;
import com.example.record.review.repository.UserCustomQuestionRepository;
import com.example.record.user.User;
import com.example.record.user.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 후기 작성 지원 질문 서비스
 * 
 * 역할:
 * 1. 사용자의 티켓 개수에 따라 적절한 질문 제공
 * 2. 사용자의 과거 후기를 분석하여 맞춤 질문 생성
 * 3. 사용자의 선호 키워드 추출 및 저장
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewQuestionService {

    private final QuestionTemplateRepository questionTemplateRepository;
    private final UserCustomQuestionRepository userCustomQuestionRepository;
    private final TicketRepository ticketRepository;
    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final OpenAIChatService openAIChatService;
    private final ObjectMapper objectMapper;

    /**
     * 사용자에게 표시할 질문들을 가져옵니다.
     * 
     * 로직:
     * 1. 티켓 개수가 3개 이하: 기존 DB 질문 중에서 장르별 랜덤 질문 제공
     * 2. 티켓 개수가 3개 이상: 사용자 맞춤 질문 풀에서 랜덤 질문 제공
     * 
     * @param userId 사용자 ID
     * @param genre 장르 (예: "MUSICAL", "BAND")
     * @return 질문 텍스트 목록 (최대 3개)
     */
    public List<String> getQuestionsForUser(String userId, String genre) {
        // 장르 매핑 (프론트엔드 → 백엔드)
        String mappedGenre = mapGenre(genre);
        
        // 사용자의 티켓 개수 확인
        long ticketCount = ticketRepository.countByUser_Id(userId);
        
        List<String> questions = new ArrayList<>();
        
        if (ticketCount <= 3) {
            // 티켓 개수가 3개 이하: 기존 DB 질문 중에서 장르별 랜덤 질문 제공
            log.info("=== 질문 조회 시작 ===");
            log.info("사용자 ID: {}", userId);
            log.info("요청 장르: {} (매핑된 장르: {})", genre, mappedGenre);
            log.info("티켓 개수: {}개 (3개 이하)", ticketCount);
            
            // 전체 질문 개수 확인 (디버깅용)
            long totalTemplates = questionTemplateRepository.count();
            log.info("DB의 전체 질문 템플릿 개수: {}개", totalTemplates);
            
            // 모든 장르의 질문 개수 확인 (디버깅용)
            List<QuestionTemplate> allMusical = questionTemplateRepository.findByGenre("musical");
            List<QuestionTemplate> allBand = questionTemplateRepository.findByGenre("band");
            List<QuestionTemplate> allCommon = questionTemplateRepository.findByGenre("common");
            log.info("장르별 질문 개수 - musical: {}개, band: {}개, common: {}개", 
                    allMusical.size(), allBand.size(), allCommon.size());
            
            // 대소문자 구분 없이 조회 시도 (혹시 대문자로 저장되어 있을 경우)
            List<QuestionTemplate> allMusicalUpper = questionTemplateRepository.findByGenre("MUSICAL");
            List<QuestionTemplate> allBandUpper = questionTemplateRepository.findByGenre("BAND");
            log.info("대문자 장르 조회 - MUSICAL: {}개, BAND: {}개", 
                    allMusicalUpper.size(), allBandUpper.size());
            
            // 해당 장르의 질문 개수 확인
            List<QuestionTemplate> allTemplates = questionTemplateRepository.findByGenre(mappedGenre);
            log.info("장르 '{}'의 질문 개수: {}개", mappedGenre, allTemplates.size());
            
            List<QuestionTemplate> templates = questionTemplateRepository.findRandomByGenre(mappedGenre, 3);
            log.info("랜덤 조회 결과: {}개", templates.size());
            
            questions = templates.stream()
                    .map(QuestionTemplate::getTemplateText)
                    .collect(Collectors.toList());
            
            // 해당 장르의 질문이 없으면 common 장르 질문 사용
            if (questions.isEmpty() && !mappedGenre.equals("common")) {
                log.warn("사용자 {}의 장르 {}에 대한 질문이 없어 common 장르 질문 사용", userId, mappedGenre);
                List<QuestionTemplate> commonTemplates = questionTemplateRepository.findRandomByGenre("common", 3);
                questions = commonTemplates.stream()
                        .map(QuestionTemplate::getTemplateText)
                        .collect(Collectors.toList());
            }
            
            log.info("최종 반환 질문 개수: {}개", questions.size());
            log.info("반환 질문 내용: {}", questions);
            log.info("=== 질문 조회 완료 ===");
            
            log.info("사용자 {}의 티켓 개수: {}개 (3개 이하) - 기존 DB 질문 사용, 장르: {}, 질문 개수: {}", 
                    userId, ticketCount, mappedGenre, questions.size());
        } else {
            // 티켓 개수가 3개 이상: 사용자 맞춤 질문 풀에서 랜덤 질문 제공
            List<UserCustomQuestion> customQuestions = userCustomQuestionRepository.findRandomByUserAndGenre(userId, mappedGenre, 3);
            
            if (customQuestions.isEmpty()) {
                // 맞춤 질문이 없으면 기존 DB 질문 사용
                List<QuestionTemplate> templates = questionTemplateRepository.findRandomByGenre(mappedGenre, 3);
                questions = templates.stream()
                        .map(QuestionTemplate::getTemplateText)
                        .collect(Collectors.toList());
                
                // 해당 장르의 질문이 없으면 common 장르 질문 사용
                if (questions.isEmpty() && !mappedGenre.equals("common")) {
                    log.warn("사용자 {}의 장르 {}에 대한 질문이 없어 common 장르 질문 사용", userId, mappedGenre);
                    List<QuestionTemplate> commonTemplates = questionTemplateRepository.findRandomByGenre("common", 3);
                    questions = commonTemplates.stream()
                            .map(QuestionTemplate::getTemplateText)
                            .collect(Collectors.toList());
                }
                
                log.info("사용자 {}의 맞춤 질문이 없음 - 기존 DB 질문 사용, 장르: {}, 질문 개수: {}", 
                        userId, mappedGenre, questions.size());
            } else {
                questions = customQuestions.stream()
                        .map(UserCustomQuestion::getTemplateText)
                        .collect(Collectors.toList());
                log.info("사용자 {}의 맞춤 질문 {}개 사용", userId, questions.size());
            }
        }
        
        // 질문이 하나도 없으면 빈 리스트 반환 (프론트엔드에서 기본 질문 사용)
        if (questions.isEmpty()) {
            log.warn("사용자 {}에게 제공할 질문이 없음 - 빈 리스트 반환", userId);
        }
        
        return questions;
    }

    /**
     * 사용자의 새로 추가된 후기들을 분석하여 맞춤 질문을 생성합니다.
     * 
     * 호출 시점: 사용자의 후기가 3개, 6개, 9개... 이런 식으로 3개씩 늘어날 때마다
     * 
     * @param userId 사용자 ID
     */
    @Transactional
    public void analyzeAndGenerateCustomQuestions(String userId) {
        // 사용자의 모든 리뷰를 생성 시간 순으로 조회
        List<Review> allReviews = reviewRepository.findByTicket_User_IdOrderByCreatedAtAsc(userId);
        
        if (allReviews.size() < 3) {
            log.info("사용자 {}의 리뷰가 3개 미만이므로 분석하지 않음", userId);
            return;
        }
        
        // 3개씩 그룹화하여 마지막 그룹(새로 추가된 3개)만 분석
        int reviewCount = allReviews.size();
        int groupIndex = (reviewCount - 1) / 3; // 0-based 그룹 인덱스
        int startIndex = groupIndex * 3;
        int endIndex = Math.min(startIndex + 3, reviewCount);
        
        // 새로 추가된 3개 후기만 추출
        List<Review> newReviews = allReviews.subList(startIndex, endIndex);
        
        if (newReviews.isEmpty()) {
            log.info("사용자 {}의 새로 추가된 후기가 없음", userId);
            return;
        }
        
        log.info("사용자 {}의 새로 추가된 후기 {}개 분석 시작", userId, newReviews.size());
        
        // 후기 텍스트 추출 (summary 필드 사용)
        List<String> reviewTexts = newReviews.stream()
                .map(Review::getSummary)
                .filter(Objects::nonNull)
                .filter(text -> !text.trim().isEmpty())
                .collect(Collectors.toList());
        
        if (reviewTexts.isEmpty()) {
            log.warn("사용자 {}의 새로 추가된 후기에 텍스트가 없음", userId);
            return;
        }
        
        // OpenAI를 사용하여 후기 분석 및 키워드 추출
        String favoriteKeywords = analyzeReviewsAndExtractKeywords(reviewTexts);
        
        // 사용자의 favorite 필드 업데이트
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다: " + userId));
        user.setFavorite(favoriteKeywords);
        userRepository.save(user);
        
        log.info("사용자 {}의 선호 키워드 업데이트: {}", userId, favoriteKeywords);
        
        // 후기들의 장르 추출 (티켓에서 가져옴)
        // DB에 소문자로 저장되어 있으므로 소문자로 변환
        Set<String> genres = newReviews.stream()
                .map(review -> review.getTicket().getGenre())
                .filter(Objects::nonNull)
                .map(genre -> {
                    // 티켓의 genre가 대문자일 수 있으므로 소문자로 변환
                    String lowerGenre = genre.toLowerCase();
                    // "MUSICAL" -> "musical", "BAND" -> "band"
                    if (lowerGenre.equals("musical")) {
                        return "musical";
                    } else if (lowerGenre.equals("band")) {
                        return "band";
                    } else {
                        return "common";
                    }
                })
                .collect(Collectors.toSet());
        
        // 각 장르별로 맞춤 질문 생성
        for (String genre : genres) {
            List<String> customQuestions = generateCustomQuestions(reviewTexts, genre, favoriteKeywords);
            
            // 생성된 질문들을 DB에 저장
            for (String questionText : customQuestions) {
                // 카테고리 추출 (간단한 휴리스틱)
                String category = extractCategory(questionText);
                
                UserCustomQuestion customQuestion = UserCustomQuestion.builder()
                        .user(user)
                        .category(category)
                        .genre(genre)
                        .templateText(questionText)
                        .build();
                
                userCustomQuestionRepository.save(customQuestion);
            }
            
            log.info("사용자 {}의 장르 {}에 대한 맞춤 질문 {}개 생성 완료", userId, genre, customQuestions.size());
        }
    }

    /**
     * 후기들을 분석하여 주요 키워드를 추출합니다.
     * 
     * @param reviewTexts 후기 텍스트 목록
     * @return 쉼표로 구분된 키워드 문자열 (예: "연기,음악,무대연출,스토리")
     */
    private String analyzeReviewsAndExtractKeywords(List<String> reviewTexts) {
        String combinedReviews = String.join("\n\n", reviewTexts);
        
        String systemPrompt = """
            당신은 공연 후기를 분석하는 전문가입니다.
            사용자가 작성한 후기들을 분석하여 주요 키워드를 추출해주세요.
            키워드는 3~5개 정도로 추출하고, 쉼표로 구분하여 반환해주세요.
            예시: "연기,음악,무대연출,스토리,감동"
            """;
        
        String userPrompt = String.format("""
            다음은 사용자가 작성한 공연 후기들입니다.
            이 후기들에서 가장 자주 언급되거나 중요한 주제를 키워드로 추출해주세요.
            
            후기들:
            %s
            
            키워드만 쉼표로 구분하여 반환해주세요.
            """, combinedReviews);
        
        try {
            String response = openAIChatService.complete(systemPrompt, userPrompt);
            // 응답에서 키워드만 추출 (불필요한 설명 제거)
            String keywords = response.trim()
                    .replaceAll("^[^가-힣]*", "") // 앞의 불필요한 텍스트 제거
                    .replaceAll("[^가-힣,]+$", "") // 뒤의 불필요한 텍스트 제거
                    .replaceAll("\\s+", ""); // 공백 제거
            
            log.info("추출된 키워드: {}", keywords);
            return keywords;
        } catch (Exception e) {
            log.error("키워드 추출 실패: {}", e.getMessage(), e);
            return "일반";
        }
    }

    /**
     * 후기들을 분석하여 맞춤 질문을 생성합니다.
     * 
     * @param reviewTexts 후기 텍스트 목록
     * @param genre 장르
     * @param favoriteKeywords 사용자의 선호 키워드
     * @return 생성된 질문 목록 (최대 3개)
     */
    private List<String> generateCustomQuestions(List<String> reviewTexts, String genre, String favoriteKeywords) {
        String combinedReviews = String.join("\n\n", reviewTexts);
        
        String systemPrompt = """
            당신은 공연 후기 작성 지원 질문을 생성하는 전문가입니다.
            사용자가 작성한 후기들을 분석하여, 사용자가 자주 언급하는 주제와 관련된 질문을 생성해주세요.
            질문은 자연스럽고 구체적이어야 하며, 사용자가 더 자세한 후기를 작성할 수 있도록 도와주는 질문이어야 합니다.
            """;
        
        String userPrompt = String.format("""
            다음은 사용자가 작성한 공연 후기들입니다.
            장르: %s
            사용자가 자주 언급하는 주제: %s
            
            후기들:
            %s
            
            이 후기들을 바탕으로 사용자에게 도움이 될 만한 질문 3개를 생성해주세요.
            각 질문은 한 줄로 작성하고, 번호 없이 질문만 반환해주세요.
            질문들은 쉼표로 구분해주세요.
            
            예시 형식:
            이 공연에서 가장 인상깊었던 장면은 무엇인가요?,배우들의 연기력은 어떠했나요?,음악과 무대 연출은 만족스러웠나요?
            """, genre, favoriteKeywords, combinedReviews);
        
        try {
            String response = openAIChatService.complete(systemPrompt, userPrompt);
            // 응답에서 질문만 추출
            String[] questions = response.trim()
                    .replaceAll("^[^가-힣?]*", "") // 앞의 불필요한 텍스트 제거
                    .replaceAll("[^가-힣?,.]+$", "") // 뒤의 불필요한 텍스트 제거
                    .split("[,，]"); // 쉼표로 분리
            
            List<String> result = Arrays.stream(questions)
                    .map(String::trim)
                    .filter(q -> !q.isEmpty() && q.contains("?"))
                    .limit(3)
                    .collect(Collectors.toList());
            
            log.info("생성된 맞춤 질문 {}개: {}", result.size(), result);
            return result;
        } catch (Exception e) {
            log.error("맞춤 질문 생성 실패: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    /**
     * 질문 텍스트에서 카테고리를 추출합니다.
     * 
     * @param questionText 질문 텍스트
     * @return 카테고리 (예: "PERFORMANCE", "MUSIC", "STAGE")
     */
    private String extractCategory(String questionText) {
        if (questionText.contains("연기") || questionText.contains("배우")) {
            return "ACTING";
        } else if (questionText.contains("음악") || questionText.contains("사운드")) {
            return "MUSIC";
        } else if (questionText.contains("무대") || questionText.contains("연출")) {
            return "STAGE";
        } else if (questionText.contains("스토리") || questionText.contains("내용")) {
            return "STORY";
        } else {
            return "OVERALL";
        }
    }

    /**
     * 프론트엔드 장르를 백엔드 장르로 매핑합니다.
     * 
     * @param frontendGenre 프론트엔드 장르 (예: "밴드", "연극/뮤지컬")
     * @return 백엔드 장르 (예: "band", "musical") - DB에 저장된 소문자 형식으로 반환
     * 
     * 주의: RDS DB의 questions_templates 테이블에 genre가 소문자("musical", "band")로 저장되어 있으므로
     * 소문자로 매핑해야 합니다.
     */
    private String mapGenre(String frontendGenre) {
        if (frontendGenre == null) {
            return "common";
        }
        
        String genre = frontendGenre.trim();
        if (genre.contains("밴드") || genre.equalsIgnoreCase("BAND")) {
            return "band";  // DB에 소문자로 저장되어 있음
        } else if (genre.contains("뮤지컬") || genre.contains("연극") || genre.equalsIgnoreCase("MUSICAL")) {
            return "musical";  // DB에 소문자로 저장되어 있음
        } else {
            return "common";  // DB에 소문자로 저장되어 있음
        }
    }
}

