package com.example.record.promptcontrol_w03.service;

/*
역할: “이미지용 짧은 영어 프롬프트”를 만들어 주는 핵심 서비스.
핵심 기능
장르 분기: 뮤지컬/밴드 케이스로 프롬프트 템플릿 분리 (미지원 장르면 예외)
공연 DB 연계:
MusicalDbRepository에서 작품/캐릭터 조회(요약, 배경, 주요 인물수, 캐릭터 속성 활용)
BandDbRepository에서 밴드명/의미/상징/포스터 색 등 조회
리뷰 내용 분석 연계: ReviewAnalysisService.analyzeReview(review) 호출 → 감정/주제/배경/조명/행동/캐릭터 등 JSON 추출
영문화/정규화: 한국어 키워드를 영어로 치환(감정/관계/나이/성별/장소/시대 등 광범위 매핑)
2~3문장 압축: OpenAIChatService를 사용해 자연스러운 2~3문장으로 요약 + imageRequest 녹여 넣기
길이 가드: 문장 단위로 최대 글자 수를 넘지 않게 안전절단
결과: PromptResponse(prompt, meta) 생성 (meta에는 장르/요약여부/추론 키워드 등)
보조 메서드: 캐릭터 설명 정리(JSON 느낌 문자열 → 자연어), 영어 치환, 문장단위 클램프 등
 */
import com.example.record.band.BandDb;
import com.example.record.band.BandDbRepository;
import com.example.record.musical.MusicalCharacter;
import com.example.record.musical.MusicalDb;
import com.example.record.musical.MusicalDbRepository;
import com.example.record.promptcontrol_w03.dto.PromptRequest;
import com.example.record.promptcontrol_w03.dto.PromptResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PromptService {

    private final ReviewAnalysisService reviewAnalysisService;
    private final MusicalDbRepository musicalDbRepository;
    private final BandDbRepository bandDbRepository;
    private final OpenAIChatService openAIChatService;

    /** 최종 압축 프롬프트 길이 상한(문장 경계 기반) */
    @Value("${openai.limits.imagePromptMaxChars:900}")
    private int imagePromptMaxChars;

    // ─────────────────────────────────────────────────────────────────────
    // 공개 메서드: 최종 이미지 프롬프트 생성 (항상 2~3문장, 영어)
    // ─────────────────────────────────────────────────────────────────────
    public PromptResponse generatePrompt(PromptRequest input) {
        final String genre = input.getGenre();

        // 1) basePrompt 생성 (DB/후기 분석 반영)
        final String basePrompt = switch (genre) {
            case "뮤지컬" -> generateMusicalPrompt(input);
            case "밴드"   -> generateBandPrompt(input);
            default       -> throw new IllegalArgumentException("지원하지 않는 장르입니다: " + genre);
        };

        // 2) 2~3문장 압축 (imageRequest를 자연스럽게 녹임)
        final String shortForm = compressToTwoOrThreeSentences(basePrompt, safe(input.getImageRequest()));

        // 3) 문장 경계 기반 길이 가드
        final String finalPrompt = clampBySentence(shortForm, imagePromptMaxChars);

        // 4) 응답 메타 포함
        PromptResponse response = new PromptResponse();
        response.setPrompt(finalPrompt);

        Map<String, Object> meta = new HashMap<>();
        meta.put("structure", genre);
        meta.put("shortForm", true);
        meta.put("imageRequest", safe(input.getImageRequest()));
        meta.put("inferred_keywords", new String[]{"visual", "mood", "scene"});
        response.setMeta(meta);

        return response;
    }

    // ─────────────────────────────────────────────────────────────────────
    // 2~3문장 압축 (OpenAIChatService 사용)
    // ─────────────────────────────────────────────────────────────────────
    /**
     * basePrompt(멀티라인 가능) + imageRequest(색/스타일/구도 등)를
     * 영어 2~3문장으로 압축. 규칙/라벨/불릿/개행 없이 자연스러운 산문으로.
     */
    private String compressToTwoOrThreeSentences(String basePrompt, String imageRequest) {
        String userMsg = (imageRequest == null || imageRequest.isBlank())
                ? "Base prompt:\n" + basePrompt
                : "Base prompt:\n" + basePrompt + "\n\nAdditional style requests:\n" + imageRequest;

        String result = openAIChatService.complete(
                // system
                """
                You rewrite rich scene prompts for text-to-image models.
                Requirements:
                - Output MUST be in ENGLISH.
                - Output MUST be exactly 2 or 3 sentences. No bullet points, no numbered lists, no line breaks.
                - Preserve concrete visual details: subjects, setting, mood, composition, lighting, color cues.
                - If additional style requests are given, subtly weave them into the prose.
                - Include naturally that there is no visible text/logos/watermarks in the image (do not list rules).
                - Avoid meta language like "the prompt is" or quotes. Write pure descriptive prose only.
                """,
                // user
                userMsg
        );

        return result == null ? "" : result.trim();
    }

    // ─────────────────────────────────────────────────────────────────────
    // 유틸
    // ─────────────────────────────────────────────────────────────────────
    /** 공백/널 안전화 */
    private static String safe(String s) {
        return (s == null) ? null : s.trim();
    }

    /**
     * 문장 경계 기반 길이 가드:
     * - 1차: 전체가 max 이하면 그대로
     * - 2차: . ! ? 단위로 자르며 누적 길이 ≤ max 유지
     * - 3차: 그래도 초과시 하드 컷
     */
    private String clampBySentence(String text, int max) {
        if (text == null) return null;
        if (text.length() <= max) return text;

        String[] parts = text.split("(?<=[.!?])\\s+");
        StringBuilder sb = new StringBuilder();
        for (String p : parts) {
            if (sb.length() + p.length() + 1 > max) break;
            if (sb.length() > 0) sb.append(' ');
            sb.append(p);
        }
        if (sb.length() > 0) return sb.toString();

        return text.substring(0, Math.min(text.length(), max));
    }

    /**
     * 캐릭터 설명에서 JSON 형식을 제거하고 자연어로 변환
     */
    private String cleanCharacterDescription(String description) {
        if (description == null || description.trim().isEmpty()) return "";
        description = description.trim();

        if (description.startsWith("{") && description.contains("name=")) {
            int nameStart = description.indexOf("name=");
            if (nameStart >= 0) {
                int nameEnd = description.indexOf(",", nameStart);
                if (nameEnd == -1) nameEnd = description.indexOf("}", nameStart);
                if (nameEnd > nameStart) {
                    String name = description.substring(nameStart + 5, nameEnd).trim();
                    int descStart = description.indexOf("description=");
                    if (descStart >= 0) {
                        int descEnd = description.indexOf(",", descStart);
                        if (descEnd == -1) descEnd = description.indexOf("}", descStart);
                        if (descEnd > descStart) {
                            String desc = description.substring(descStart + 11, descEnd).trim();
                            return name + (!desc.isEmpty() ? " (" + desc + ")" : "");
                        }
                    }
                    return name;
                }
            }
        }
        return description;
    }

    /**
     * 후기 분석 JSON에서 내려오는 값은 String 외에도 List/Array일 수 있으므로
     * 안전하게 문자열로 변환한 뒤 translateToEnglish를 적용합니다.
     */
    private String translateValue(Object value) {
        if (value == null) return "unknown";

        if (value instanceof String str) {
            return translateToEnglish(str);
        }

        if (value instanceof Collection<?> collection) {
            String joined = collection.stream()
                    .filter(Objects::nonNull)
                    .map(Object::toString)
                    .collect(Collectors.joining(", "));
            return translateToEnglish(joined);
        }

        return translateToEnglish(value.toString());
    }

    /**
     * 한국어를 영어로 단순 매핑(프롬프트 간결화 목적)
     */
    private String translateToEnglish(String korean) {
        if (korean == null || korean.trim().isEmpty()) return "unknown";
        if (!korean.matches(".*[가-힣]+.*")) return korean.trim();

        // 감정
        korean = korean.replace("아쉬움", "regret")
                .replace("답답함", "frustration")
                .replace("분노", "anger")
                .replace("만족", "satisfaction")
                .replace("기쁨", "joy")
                .replace("슬픔", "sadness")
                .replace("사랑", "love")
                .replace("증오", "hatred")
                .replace("감동적", "emotional")
                .replace("긴장", "tension")
                .replace("갈등", "conflict")
                .replace("여운", "lingering emotion")
                .replace("놀람", "surprise")
                .replace("아리함", "confusion")
                .replace("깊은", "deep");

        // 장르/설정
        korean = korean.replace("뮤지컬", "musical")
                .replace("밴드", "band")
                .replace("콘서트", "concert")
                .replace("극장", "theater")
                .replace("무대", "stage")
                .replace("호텔", "hotel")
                .replace("방", "room")
                .replace("일제강점기", "Japanese colonial period")
                .replace("의", " of")
                .replace("은유", "metaphor")
                .replace("창작", "creation")
                .replace("추락", "fall")
                .replace("현실", "reality")
                .replace("허상", "illusion")
                .replace("예술", "art")
                .replace("본질", "essence")
                .replace("인간", "human")
                .replace("존엄", "dignity")
                .replace("납치", "abduction");

        // 나이/성별
        korean = korean.replace("20대 중반", "mid-20s")
                .replace("20대 초중반", "early to mid-20s")
                .replace("20대 초반", "early 20s")
                .replace("20대 후반", "late 20s")
                .replace("30대", "30s")
                .replace("40대", "40s")
                .replace("50대", "50s")
                .replace("남성", "male")
                .replace("여성", "female")
                .replace("남자", "male")
                .replace("여자", "female");

        // 관계
        korean = korean.replace("연인", "lovers")
                .replace("친구", "friends")
                .replace("가족", "family")
                .replace("동료", "colleagues");

        // 액션
        korean = korean.replace("노래", "singing")
                .replace("춤", "dancing")
                .replace("연기", "acting")
                .replace("연주", "playing")
                .replace("공연", "performance");

        // 직업/역할
        korean = korean.replace("시인", "poet")
                .replace("건축가", "architect")
                .replace("기생", "gisaeng")
                .replace("배우", "actor")
                .replace("가수", "singer")
                .replace("댄서", "dancer");

        // 조명
        korean = korean.replace("어둠", "darkness")
                .replace("밝음", "brightness")
                .replace("무대조명", "stage lighting")
                .replace("스포트라이트", "spotlight");

        // 남은 한글 제거 → 공백 정리
        korean = korean.replaceAll("[가-힣]", " ");
        korean = korean.replaceAll("\\s+", " ").trim();

        return korean.isEmpty() ? "unknown" : korean;
    }

    // ─────────────────────────────────────────────────────────────────────
    // 뮤지컬 프롬프트 생성
    // ─────────────────────────────────────────────────────────────────────
    /**
     * 뮤지컬 이미지 생성 프롬프트를 생성합니다.
     * 
     * 분기 처리:
     * 1. musical_db 테이블에서 title로 조회하여 데이터가 있는 경우:
     *    - DB의 summary(줄거리), background(시대적/공간적 배경), main_character_count(주요 인물 수) 우선 사용
     *    - musical_id로 musical_characters 테이블 조회하여 캐릭터 정보(gender, age, occupation, description) 활용
     *    - 후기 분석 결과는 감정(emotion), 관계(relationship), 행동(actions), 조명(lighting) 등 보조 정보로만 사용
     * 
     * 2. musical_db 테이블에 데이터가 없는 경우:
     *    - 사용자 후기 분석 결과만 사용하여 프롬프트 생성
     *    - 후기에서 추출한 theme(주제), setting(배경), character(캐릭터) 정보 활용
     */
    private String generateMusicalPrompt(PromptRequest input) {
        // 1단계: 제목 정규화 (공백 제거, 특수 문자 제거)
        String normalizedTitle = "";
        if (input.getTitle() != null) {
            normalizedTitle = input.getTitle()
                    .trim()
                    .replaceAll("\\s+", "")
                    .replaceAll("[\\u00A0\\u2000-\\u200B\\u2028\\u2029\\uFEFF]", "");
        }

        // 2단계: musical_db 테이블에서 title로 조회 시도
        // 여러 방법으로 시도: 정규화된 제목 → 원본 제목 → 부분 일치 검색
        Optional<MusicalDb> musicalOpt = musicalDbRepository.findByTitle(normalizedTitle);
        if (!musicalOpt.isPresent() && input.getTitle() != null) {
            String originalTitle = input.getTitle().trim();
            musicalOpt = musicalDbRepository.findByTitle(originalTitle);
        }
        if (!musicalOpt.isPresent()) {
            List<MusicalDb> musicals = musicalDbRepository.findByTitleContaining(normalizedTitle);
            if (!musicals.isEmpty()) {
                musicalOpt = Optional.of(musicals.get(0));
            }
        }
        if (!musicalOpt.isPresent() && input.getTitle() != null) {
            String originalTitle = input.getTitle().trim();
            List<MusicalDb> musicals = musicalDbRepository.findByTitleContaining(originalTitle);
            if (!musicals.isEmpty()) {
                musicalOpt = Optional.of(musicals.get(0));
            }
        }

        // 3단계: 후기 분석 (항상 수행 - DB 데이터가 있어도 보조 정보로 사용)
        Map<String, Object> data = reviewAnalysisService.analyzeReview(input.getBasePrompt());


        // 4단계: 분기 처리
        // ============================================================
        // 분기 1: musical_db 테이블에 데이터가 있는 경우
        // ============================================================
        if (musicalOpt.isPresent()) {
            MusicalDb musical = musicalOpt.get();
            Long musicalId = musical.getId();
            
            // musical_id로 musical_characters 테이블에서 캐릭터 정보 조회
            Optional<MusicalDb> musicalWithCharactersOpt = musicalDbRepository.findByIdWithCharacters(musicalId);
            
            // DB에서 가져온 정보 우선 사용 (summary, background, main_character_count)
            // 후기 분석 결과는 보조 정보로만 사용 (감정, 관계, 행동, 조명 등)
            String musicalSummary = musical.getSummary() != null 
                    ? musical.getSummary()  // DB의 summary 우선 사용
                    : (String) data.get("theme");  // 없으면 후기 분석 결과 사용
            
            String musicalBackground = musical.getBackground() != null 
                    ? musical.getBackground()  // DB의 background 우선 사용
                    : (String) data.get("setting");  // 없으면 후기 분석 결과 사용
            
            Integer characterCount = musical.getMainCharacterCount() != null
                    ? musical.getMainCharacterCount()  // DB의 main_character_count 우선 사용
                    : 3;  // 기본값

            // 캐릭터 정보 구성
            StringBuilder characterDetails = new StringBuilder();
            
            if (musicalWithCharactersOpt.isPresent()) {
                List<MusicalCharacter> characters = musicalWithCharactersOpt.get().getCharacters();
                
                // musical_characters 테이블에 데이터가 있는 경우: DB의 캐릭터 정보 활용
                if (characters != null && !characters.isEmpty()) {
                    // 최대 5명까지만 사용 (성능 및 프롬프트 길이 제한)
                    int maxCharacters = Math.min(characters.size(), Math.min(characterCount, 5));
                    
                    for (int i = 0; i < maxCharacters; i++) {
                        MusicalCharacter character = characters.get(i);
                        if (i > 0) characterDetails.append(", ");

                        // 캐릭터 이름
                        String charInfo = character.getName();
                        StringBuilder charAttributes = new StringBuilder();
                        
                        // DB에서 가져온 캐릭터 속성들을 영어로 번역하여 조합
                        // 나이대 (age)
                        if (character.getAge() != null && !character.getAge().trim().isEmpty()) {
                            charAttributes.append(translateToEnglish(character.getAge()));
                        }
                        // 성별 (gender)
                        if (character.getGender() != null && !character.getGender().trim().isEmpty()) {
                            if (charAttributes.length() > 0) charAttributes.append(" ");
                            charAttributes.append(translateToEnglish(character.getGender()));
                        }
                        // 직업 (occupation)
                        if (character.getOccupation() != null && !character.getOccupation().trim().isEmpty()) {
                            if (charAttributes.length() > 0) charAttributes.append(" ");
                            charAttributes.append(translateToEnglish(character.getOccupation()));
                        }
                        // 인물 설명 (description)
                        if (character.getDescription() != null && !character.getDescription().trim().isEmpty()) {
                            if (charAttributes.length() > 0) charAttributes.append(", ");
                            charAttributes.append(translateToEnglish(character.getDescription()));
                        }
                        
                        // 속성이 있으면 괄호로 묶어서 추가
                        if (charAttributes.length() > 0) {
                            charInfo += " (a " + charAttributes + ")";
                        }
                        characterDetails.append(charInfo);
                    }
                } else {
                    // musical_characters 테이블에 데이터가 없는 경우: 캐릭터 수만 사용
                    characterDetails.append(characterCount).append(" distinct characters");
                }
            } else {
                // 캐릭터 정보를 가져올 수 없는 경우: 캐릭터 수만 사용
                characterDetails.append(characterCount).append(" distinct characters");
            }

            // DB 데이터 기반 프롬프트 생성
            // DB 정보: summary, background, characterCount, characterDetails
            // 후기 분석 정보: emotion, relationship, actions, lighting
            return String.format(
                    "A %s musical theater scene about %s, set in %s and depicting %s, featuring exactly %d characters only: %s. " +
                            "The scene must include exactly %d characters—no extras or background people. With %s, under %s. " +
                            "There is no visible text, letters, words, captions, logos, or watermarks in the image.",
                    translateValue(data.get("emotion")),  // 후기 분석: 감정
                    translateToEnglish(musicalSummary),  // DB 우선: 줄거리
                    translateToEnglish(musicalBackground),  // DB 우선: 배경
                    translateValue(data.get("relationship")),  // 후기 분석: 관계
                    characterCount,  // DB 우선: 인물 수
                    translateToEnglish(characterDetails.toString()),  // DB 우선: 캐릭터 정보
                    characterCount,  // 인물 수 반복 (강조)
                    translateValue(data.get("actions")),  // 후기 분석: 행동
                    translateValue(data.get("lighting"))  // 후기 분석: 조명
            );
        }

        // ============================================================
        // 분기 2: musical_db 테이블에 데이터가 없는 경우
        // ============================================================
        // 사용자 후기 분석 결과만 사용하여 프롬프트 생성
        
        // 후기 분석에서 추출한 정보 사용
        String musicalSummary = (String) data.get("theme");  // 후기에서 추출한 주제
        String musicalBackground = (String) data.get("setting");  // 후기에서 추출한 배경

        // 후기에서 추출한 캐릭터 정보 구성
        StringBuilder characterPart = new StringBuilder();
        String cleanChar1 = cleanCharacterDescription(Objects.toString(data.get("character1"), ""));
        String cleanChar2 = cleanCharacterDescription(Objects.toString(data.get("character2"), ""));
        if (!cleanChar1.isEmpty() && !cleanChar2.isEmpty()) {
            characterPart.append(cleanChar1).append(" and ").append(cleanChar2);
        } else if (!cleanChar1.isEmpty()) {
            characterPart.append(cleanChar1);
        } else if (!cleanChar2.isEmpty()) {
            characterPart.append(cleanChar2);
        }
        for (int i = 3; i <= 5; i++) {
            String key = "character" + i;
            if (data.containsKey(key)) {
                String cleanChar = cleanCharacterDescription(Objects.toString(data.get(key), ""));
                if (!cleanChar.isEmpty()) {
                    if (characterPart.length() > 0) characterPart.append(", and ");
                    characterPart.append(cleanChar);
                }
            }
        }
        if (characterPart.length() == 0) {
            characterPart.append("the main characters");
        }

        // 후기 분석 기반 프롬프트 생성
        return String.format(
                "A %s musical theater scene about %s, set in %s and depicting %s, featuring %s. " +
                        "With %s, under %s. There is no visible text, letters, words, captions, logos, or watermarks in the image.",
                translateValue(data.get("emotion")),  // 후기 분석: 감정
                translateToEnglish(musicalSummary != null ? musicalSummary : ""),  // 후기 분석: 주제
                translateToEnglish(musicalBackground != null ? musicalBackground : ""),  // 후기 분석: 배경
                translateValue(data.get("relationship")),  // 후기 분석: 관계
                translateToEnglish(characterPart.toString()),  // 후기 분석: 캐릭터
                translateValue(data.get("actions")),  // 후기 분석: 행동
                translateValue(data.get("lighting"))  // 후기 분석: 조명
        );
    }

    private String generateBandPrompt(PromptRequest input) {
        Optional<BandDb> bandOpt = bandDbRepository.findByBandNameIgnoreCase(input.getTitle());

        String bandName = input.getTitle();
        String bandNameMeaning = bandOpt.map(BandDb::getBandNameMeaning)
                .orElse("emotional and powerful music");
        String posterColor = bandOpt.map(BandDb::getPosterColor)
                .orElse("deep blue and purple");
        String bandSymbol = bandOpt.map(BandDb::getBandSymbol)
                .orElse("stage design");

        return String.format(
                "A moody alternative rock live performance scene by %s, featuring %s, set during autumn, at %s on %s, " +
                        "with a stage design inspired by %s, including %s lighting, fog machines and backlights. " +
                        "No characters or visible text, letters, words, captions, logos, or watermarks appear in the image.",
                translateToEnglish(bandName),
                translateToEnglish(bandNameMeaning),
                translateToEnglish(input.getLocation()),
                input.getDate(),
                translateToEnglish(bandSymbol),
                translateToEnglish(posterColor)
        );
    }
}
