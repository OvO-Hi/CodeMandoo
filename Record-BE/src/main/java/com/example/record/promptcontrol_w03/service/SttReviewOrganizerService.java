package com.example.record.promptcontrol_w03.service;
//길이 제한 없음, 구조화 & 정리문 반환
/*
역할: STT/후기 내용을 길이 제한 없이 구조적으로 정리 + 내러티브 문단 생성.
핵심 기능
DB 컨텍스트 로딩:
뮤지컬: 제목 정규화 → findByTitle()/findByTitleContaining() 등으로 유연 매칭, summary/background/characters 로드
밴드: 의미/색상/상징/배경 등 단서(cues) 조합
StructuredMeta 생성: 장르/제목/날짜/장소/감정/주제/관계/배경/조명/행동/캐릭터/하이라이트 등 필드 구성
서식화 출력: 섹션(기본정보/연출/인물/핵심포인트/—정리후기—)로 보기 좋은 텍스트 조립
결과: StructuredMeta, narrative(문단), dbSummary, rawAnalysis 등을 묶어 반환
 */

import com.example.record.band.BandDb;
import com.example.record.band.BandDbRepository;
import com.example.record.musical.MusicalCharacter;
import com.example.record.musical.MusicalDb;
import com.example.record.musical.MusicalDbRepository;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class SttReviewOrganizerService {

    private final ReviewAnalysisService reviewAnalysisService;
    private final MusicalDbRepository musicalDbRepository;
    private final BandDbRepository bandDbRepository;

    public OrganizedReview organize(OrganizeRequest req) {
        String genre = safe(req.getGenre());
        String title = safe(req.getTitle());
        String review = safe(req.getReview());
        String date = safe(req.getDate());
        String location = safe(req.getLocation());

        Map<String, Object> a = reviewAnalysisService.analyzeReview(review);
        DbContext db = "뮤지컬".equals(genre)
                ? loadMusicalContext(title, a)
                : "밴드".equals(genre)
                ? loadBandContext(title, a)
                : DbContext.empty();

        StructuredMeta meta = buildStructuredMeta(genre, title, date, location, a, db);
        String narrative = buildNarrativeKorean(meta);

        return OrganizedReview.builder()
                .genre(genre)
                .title(title)
                .date(date)
                .location(location)
                .structured(meta)
                .narrative(narrative)
                .rawAnalysis(a)
                .dbSummary(db.summary)
                .build();
    }

    private DbContext loadMusicalContext(String title, Map<String, Object> a) {
        if (isBlank(title)) return DbContext.empty();

        String normalized = title.trim()
                .replaceAll("\\s+", "")
                .replaceAll("[\\u00A0\\u2000-\\u200B\\u2028\\u2029\\uFEFF]", "");

        Optional<MusicalDb> opt = musicalDbRepository.findByTitle(normalized);
        if (!opt.isPresent()) opt = musicalDbRepository.findByTitle(title.trim());
        if (!opt.isPresent()) {
            List<MusicalDb> list = musicalDbRepository.findByTitleContaining(normalized);
            if (!list.isEmpty()) opt = Optional.of(list.get(0));
        }
        if (!opt.isPresent()) {
            List<MusicalDb> list = musicalDbRepository.findByTitleContaining(title.trim());
            if (!list.isEmpty()) opt = Optional.of(list.get(0));
        }

        if (!opt.isPresent()) {
            return DbContext.builder()
                    .type("musical")
                    .summary(null)
                    .background(null)
                    .characters(Collections.emptyList())
                    .build();
        }

        MusicalDb base = opt.get();
        Optional<MusicalDb> withChars = musicalDbRepository.findByIdWithCharacters(base.getId());
        MusicalDb m = withChars.orElse(base);

        String summary = notBlank(m.getSummary()) ? m.getSummary() : objToStr(a.get("theme"));
        String bg = notBlank(m.getBackground()) ? m.getBackground() : objToStr(a.get("setting"));

        List<String> characters = new ArrayList<>();
        if (m.getCharacters() != null && !m.getCharacters().isEmpty()) {
            for (MusicalCharacter c : m.getCharacters()) {
                String info = cleanCharacterDescription(c);
                if (notBlank(info)) characters.add(info);
                if (characters.size() >= 5) break;
            }
        }

        return DbContext.builder()
                .type("musical")
                .summary(summary)
                .background(bg)
                .characters(characters)
                .build();
    }

    private DbContext loadBandContext(String title, Map<String, Object> a) {
        if (isBlank(title)) return DbContext.empty();

        Optional<BandDb> opt = bandDbRepository.findByBandNameIgnoreCase(title);
        if (!opt.isPresent()) {
            return DbContext.builder().type("band").build();
        }

        BandDb band = opt.get();
        String meaning = band.getBandNameMeaning();
        String color = band.getPosterColor();
        String symbol = band.getBandSymbol();

        List<String> cues = new ArrayList<>();
        if (notBlank(meaning)) cues.add(meaning);
        if (notBlank(color)) cues.add(color);
        if (notBlank(symbol)) cues.add(symbol);

        return DbContext.builder()
                .type("band")
                .summary(joinNonEmpty(", ", cues))
                .background(objToStr(a.get("setting")))
                .characters(Collections.emptyList())
                .build();
    }

    private StructuredMeta buildStructuredMeta(
            String genre, String title, String date, String location,
            Map<String, Object> a, DbContext db
    ) {
        String emotion = objToStr(a.get("emotion"));
        String theme = objToStr(a.get("theme"));
        String relationship = objToStr(a.get("relationship"));
        String setting = firstNonBlank(db.background, objToStr(a.get("setting")));
        String actions = objToStr(a.get("actions"));
        String lighting = objToStr(a.get("lighting"));

        List<String> characters = new ArrayList<>();
        if (db.characters != null && !db.characters.isEmpty()) {
            characters.addAll(db.characters);
        } else {
            for (int i = 1; i <= 5; i++) {
                String key = "character" + i;
                if (a.containsKey(key)) {
                    String v = com.example.record.promptcontrol_w03.dto.PromptTextUtils.cleanCharacterDescription(
                            Objects.toString(a.get(key), "")
                    );
                    if (notBlank(v)) characters.add(v);
                }
            }
        }

        List<String> highlights = new ArrayList<>();
        if (notBlank(theme)) highlights.add("주제: " + theme);
        if (notBlank(emotion)) highlights.add("주요 감정: " + emotion);
        if (notBlank(relationship)) highlights.add("관계: " + relationship);
        if (notBlank(actions)) highlights.add("무대/행동: " + actions);
        if (notBlank(lighting)) highlights.add("조명/분위기: " + lighting);

        return StructuredMeta.builder()
                .genre(genre)
                .title(title)
                .date(date)
                .location(location)
                .theme(theme)
                .emotion(emotion)
                .relationship(relationship)
                .setting(setting)
                .lighting(lighting)
                .actions(actions)
                .characters(characters)
                .dbSummary(db.summary)
                .highlights(highlights)
                .build();
    }

    private String buildNarrativeKorean(StructuredMeta m) {
        StringBuilder sb = new StringBuilder();

        if (notBlank(m.getTitle()) || notBlank(m.getGenre())) {
            sb.append("【").append(orEmpty(m.getTitle()));
            if (notBlank(m.getGenre())) sb.append(" / ").append(m.getGenre());
            sb.append("】").append("\n");
        }
        if (notBlank(m.getDate()) || notBlank(m.getLocation())) {
            sb.append(orEmpty(m.getDate()));
            if (notBlank(m.getDate()) && notBlank(m.getLocation())) sb.append(" · ");
            sb.append(orEmpty(m.getLocation())).append("\n\n");
        }

        if (notBlank(m.getTheme()) || notBlank(m.getSetting()) || notBlank(m.getDbSummary())) {
            sb.append("■ 작품/공연 요지").append("\n");
            if (notBlank(m.getTheme()))    sb.append("- 주제: ").append(m.getTheme()).append("\n");
            if (notBlank(m.getSetting()))  sb.append("- 배경: ").append(m.getSetting()).append("\n");
            if (notBlank(m.getDbSummary())) sb.append("- 참고 메타: ").append(m.getDbSummary()).append("\n");
            sb.append("\n");
        }

        if (notBlank(m.getEmotion()) || notBlank(m.getLighting()) || notBlank(m.getActions())) {
            sb.append("■ 무드 & 연출").append("\n");
            if (notBlank(m.getEmotion()))  sb.append("- 감정: ").append(m.getEmotion()).append("\n");
            if (notBlank(m.getLighting())) sb.append("- 조명: ").append(m.getLighting()).append("\n");
            if (notBlank(m.getActions()))  sb.append("- 무대/행동: ").append(m.getActions()).append("\n");
            sb.append("\n");
        }

        if (m.getCharacters() != null && !m.getCharacters().isEmpty()) {
            sb.append("■ 인물/등장 캐릭터").append("\n");
            for (String c : m.getCharacters()) sb.append("- ").append(c).append("\n");
            sb.append("\n");
        }

        if (m.getHighlights() != null && !m.getHighlights().isEmpty()) {
            sb.append("■ 핵심 포인트").append("\n");
            for (String h : m.getHighlights()) sb.append("- ").append(h).append("\n");
            sb.append("\n");
        }

        sb.append("— 정리 후기 —").append("\n");
        java.util.List<String> lines = new java.util.ArrayList<>();
        if (notBlank(m.getTheme()) || notBlank(m.getSetting())) {
            lines.add(String.format("%s 속에서 펼쳐진 이야기와 무대는 %s의 결을 따라 전개되었고, 장면 전환마다 %s이(가) 자연스럽게 스며들었습니다.",
                    orDefault(m.getTitle(), "이번 공연"),
                    orDefault(m.getTheme(), "주요 주제"),
                    orDefault(m.getSetting(), "공간적 배경")));
        }
        if (notBlank(m.getEmotion()) || notBlank(m.getLighting()) || notBlank(m.getActions())) {
            lines.add(String.format("무대는 %s 분위기 아래 %s이(가) 돋보였으며, 연출 측면에서 %s이(가) 전체 감정선을 견인했습니다.",
                    orDefault(m.getEmotion(), "감정적인"),
                    orDefault(m.getLighting(), "조명 설계"),
                    orDefault(m.getActions(), "배우들의 동선과 장면 구성")));
        }
        if (m.getCharacters() != null && !m.getCharacters().isEmpty()) {
            lines.add("등장인물은 " + String.join(", ", m.getCharacters()) + " 등이 주축이 되어 장면의 밀도를 높였습니다.");
        }
        if (notBlank(m.getDbSummary())) {
            lines.add("작품의 기본 맥락은 DB 메타에서 드러난 '" + m.getDbSummary() + "' 특성이 후기에 자연스럽게 이어졌습니다.");
        }
        if (lines.isEmpty()) {
            lines.add("공연의 인상과 감정선이 무대 구성과 연출에 조화롭게 반영되어 깊은 여운을 남겼습니다.");
        }
        sb.append(String.join("\n", lines));
        return sb.toString().trim();
    }

    // ===== 유틸 =====
    private static String orEmpty(String s) { return s == null ? "" : s; }
    private static boolean isBlank(String s) { return s == null || s.trim().isEmpty(); }
    private static boolean notBlank(String s) { return !isBlank(s); }
    private static String safe(String s) { return s == null ? null : s.trim(); }
    private static String objToStr(Object o) { return o == null ? null : String.valueOf(o).trim(); }
    private static String firstNonBlank(String a, String b) { return notBlank(a) ? a : (notBlank(b) ? b : null); }
    private static String joinNonEmpty(String sep, java.util.List<String> arr) {
        java.util.List<String> clean = new java.util.ArrayList<>();
        for (String s : arr) if (notBlank(s)) clean.add(s.trim());
        return String.join(sep, clean);
    }
    private static String orDefault(String value, String fallback) {
        return notBlank(value) ? value : fallback;
    }
    private static String cleanCharacterDescription(MusicalCharacter c) {
        if (c == null) return null;
        String name = orEmpty(c.getName());
        java.util.List<String> attrs = new java.util.ArrayList<>();
        if (notBlank(c.getAge())) attrs.add(c.getAge().trim());
        if (notBlank(c.getGender())) attrs.add(c.getGender().trim());
        if (notBlank(c.getOccupation())) attrs.add(c.getOccupation().trim());
        if (notBlank(c.getDescription())) attrs.add(c.getDescription().trim());
        String attr = String.join(", ", attrs);
        return notBlank(attr) ? name + " (" + attr + ")" : name;
    }

    // ===== DTOs =====
    @Data @Builder @AllArgsConstructor
    public static class OrganizeRequest {
        private String genre;
        private String title;
        private String review;
        private String date;
        private String location;
    }

    @Data @Builder
    public static class OrganizedReview {
        private String genre;
        private String title;
        private String date;
        private String location;
        private StructuredMeta structured;
        private String narrative;
        private Map<String, Object> rawAnalysis;
        private String dbSummary;
    }

    @Data @Builder
    public static class StructuredMeta {
        private String genre;
        private String title;
        private String date;
        private String location;
        private String theme;
        private String emotion;
        private String relationship;
        private String setting;
        private String lighting;
        private String actions;
        private java.util.List<String> characters;
        private String dbSummary;
        private java.util.List<String> highlights;
    }

    @Data @Builder
    private static class DbContext {
        private String type;
        private String summary;
        private String background;
        private java.util.List<String> characters;

        static DbContext empty() {
            return DbContext.builder()
                    .type(null)
                    .summary(null)
                    .background(null)
                    .characters(java.util.Collections.emptyList())
                    .build();
        }
    }
}
