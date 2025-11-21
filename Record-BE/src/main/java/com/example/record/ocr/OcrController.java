package com.example.record.ocr;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/ocr")
@RequiredArgsConstructor
public class OcrController {

    private final OcrService ocrService;   // Google Vision API 기반 OCR
    private final GptClient gptClient;     // GPT 구조화 클라이언트

    private final ObjectMapper om = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    /** ✅ OCR 원문만 반환 */
    @PostMapping(
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<OcrResponse> uploadImage(@RequestPart("file") MultipartFile file) throws Exception {
        File tempFile = createTempFromMultipart(file);
        try {
            String text = ocrService.extractTextFromImage(tempFile);
            return ResponseEntity.ok(new OcrResponse(text == null ? "" : text));
        } finally {
            if (tempFile.exists()) tempFile.delete();
        }
    }

    /** ✅ DTO로 반환(빈 값은 "") */
    @PostMapping(
            value = "/structured",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<PerformanceInfo> uploadAndParse(@RequestPart("file") MultipartFile file) throws Exception {
        File tempFile = createTempFromMultipart(file);
        try {
            String text = ocrService.extractTextFromImage(tempFile);

            String prompt = """
                아래 OCR 텍스트에서 공연 정보를 JSON으로 추출하세요.
                필드 키: title(공연 제목), date(YYYY-MM-DD), time(24h HH:mm), venue(공연 장소), artist(아티스트)

                규칙:
                - 일반적인 표기 관례에 따라 합리적 정규화 허용
                  (예: "2022년 10월 15일(토) 6:00 pm" → date:"2022-10-15", time:"18:00")
                - 값이 애매하면 빈 문자열("")로 둡니다. (키는 유지)
                - 반드시 순수 JSON만 출력하세요. (설명/코드블록 금지)

                예시 입력:
                "2023년 7월 9일 오후 7시, 블루스퀘어 신한카드홀, 뮤지컬 레베카, 출연: 홍길동"
                예시 출력:
                {"title":"뮤지컬 레베카","date":"2023-07-09","time":"19:00","venue":"블루스퀘어 신한카드홀","artist":"홍길동"}

                OCR 텍스트:
                %s
            """.formatted(text == null ? "" : text);

            String json = gptClient.getStructuredJsonFromPrompt(prompt);
            String cleaned = stripCodeFence(json).trim();

            // 1차: DTO 직매핑
            PerformanceInfo info;
            try {
                info = om.readValue(cleaned, PerformanceInfo.class);
            } catch (Exception directFail) {
                // 2차: Map 경로로 유연 매핑
                Map<String, Object> map = om.readValue(cleaned, new TypeReference<>() {});
                info = new PerformanceInfo(
                        nvl(str(map.get("title"))),
                        nvl(toIsoDate(str(map.get("date")))),
                        nvl(to24h(str(map.get("time")))),
                        nvl(str(map.get("venue"))),
                        nvl(str(map.get("artist")))
                );
            }

            // 3차: OCR 원문으로 보정(부족한 필드만) - 티켓 전용
            if (isNullOrEmpty(info.getTitle()) && containsYTC(text)) {
                info.setTitle("Yet to Come in BUSAN");
            }
            if (isNullOrEmpty(info.getVenue())) {
                String v = findVenue(text);
                if (!v.isEmpty()) info.setVenue(v);
            }
            if (isNullOrEmpty(info.getDate())) {
                String d = findDate(text);
                if (!d.isEmpty()) info.setDate(d);
            }
            if (isNullOrEmpty(info.getTime())) {
                String t = findTime(text);
                if (!t.isEmpty()) info.setTime(t);
            }

            // null → "" 치환
            if (info.getTitle()  == null) info.setTitle("");
            if (info.getDate()   == null) info.setDate("");
            if (info.getTime()   == null) info.setTime("");
            if (info.getVenue()  == null) info.setVenue("");
            if (info.getArtist() == null) info.setArtist("");

            return ResponseEntity.ok(info);
        } finally {
            if (tempFile.exists()) tempFile.delete();
        }
    }

    /** ✅ 티켓 전용 OCR (artist 필드 없음) */
    @PostMapping(
            value = "/extract/ticket",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<Map<String, String>> extractTicket(@RequestPart("file") MultipartFile file) throws Exception {
        File tempFile = createTempFromMultipart(file);
        try {
            String text = ocrService.extractTextFromImage(tempFile);

            String prompt = """
                아래 OCR 텍스트를 분석하여 **티켓** 정보를 JSON으로 추출하세요.
                키: title, date(YYYY-MM-DD), time(24h HH:mm), venue, seat

                **티켓 전용 규칙:**
                1. **제목(title) 추출**:
                   - "MUSICAL" 다음에 오는 제목 (예: "MUSICAL 너를 위한 글자" → "너를 위한 글자")
                   - 가장 큰 글씨이거나 우측 상단/하단의 극 이름
                   - 부제목이나 설명 문구는 제목으로 인식하지 않음
                   - 한국어로 출력 (예: "Nijinsky" → "니진스키")
                
                2. **아티스트(artist) 필드**: 아예 생략 (티켓에는 없음)
                
                3. **기타 필드**:
                   - 확실한 값만 포함 (모호하면 키 자체를 생략)
                   - 좌석 오인식 교정: "14일" → "14열"
                   - 순수 JSON만 출력

                OCR 텍스트:
                %s
            """.formatted(text == null ? "" : text);

            String json = gptClient.getStructuredJsonFromPrompt(prompt);
            String cleaned = stripCodeFence(json).trim();

            Map<String, String> result = new LinkedHashMap<>();
            try {
                Map<String, String> ai = om.readValue(cleaned, new TypeReference<LinkedHashMap<String, String>>() {});
                if (ai != null) {
                    Set<String> allow = Set.of("title","date","time","venue","seat"); // artist 제외
                    ai.forEach((k, v) -> {
                        if (k != null && allow.contains(k) && v != null) {
                            String val = v.trim();
                            if (!val.isEmpty() && !val.equalsIgnoreCase("null") && !val.equalsIgnoreCase("unknown")) {
                                result.put(k, val);
                            }
                        }
                    });
                }
            } catch (Exception ignore) {
                // GPT가 비정상 응답이면 아래 로컬 보완 적용
            }

            // 로컬 보완
            Map<String, String> local = fallbackExtract(text);
            local.forEach(result::putIfAbsent);

            // 후처리
            if (result.containsKey("seat")) {
                result.put("seat", fixSeatHangulMisread(result.get("seat")));
            }
            if (result.containsKey("time")) {
                String t24 = to24h(result.get("time"));
                if (t24 != null) result.put("time", t24);
            }
            if (result.containsKey("date")) {
                String iso = toIsoDate(result.get("date"));
                if (iso != null) result.put("date", iso);
            }

            return ResponseEntity.ok(result);
        } finally {
            if (tempFile.exists()) tempFile.delete();
        }
    }

    /** ✅ 기본 OCR 엔드포인트 (티켓 전용) */
    @PostMapping(
            value = "/extract",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<Map<String, String>> extractCompact(@RequestPart("file") MultipartFile file) throws Exception {
        File tempFile = createTempFromMultipart(file);
        try {
            String text = ocrService.extractTextFromImage(tempFile);

            String prompt = """
                아래 OCR 텍스트를 분석하여 **티켓** 정보를 JSON으로 추출하세요.
                키: title, date(YYYY-MM-DD), time(24h HH:mm), venue, seat

                **티켓 전용 규칙:**
                1. **제목(title) 추출**:
                   - "MUSICAL" 다음에 오는 제목 (예: "MUSICAL 너를 위한 글자" → "너를 위한 글자")
                   - 가장 큰 글씨이거나 우측 상단/하단의 극 이름
                   - 부제목이나 설명 문구는 제목으로 인식하지 않음
                   - 한국어로 출력 (예: "Nijinsky" → "니진스키")
                
                2. **아티스트(artist) 필드**: 아예 생략 (티켓에는 없음)
                
                3. **기타 필드**:
                   - 확실한 값만 포함 (모호하면 키 자체를 생략)
                   - 좌석 오인식 교정: "14일" → "14열"
                   - 순수 JSON만 출력

                OCR 텍스트:
                %s
            """.formatted(text == null ? "" : text);

            String json = gptClient.getStructuredJsonFromPrompt(prompt);
            String cleaned = stripCodeFence(json).trim();

            Map<String, String> result = new LinkedHashMap<>();
            try {
                Map<String, String> ai = om.readValue(cleaned, new TypeReference<LinkedHashMap<String, String>>() {});
                if (ai != null) {
                    Set<String> allow = Set.of("title","date","time","venue","seat"); // artist 제외
                    ai.forEach((k, v) -> {
                        if (k != null && allow.contains(k) && v != null) {
                            String val = v.trim();
                            if (!val.isEmpty() && !val.equalsIgnoreCase("null") && !val.equalsIgnoreCase("unknown")) {
                                result.put(k, val);
                            }
                        }
                    });
                }
            } catch (Exception ignore) {
                // GPT가 비정상 응답이면 아래 로컬 보완 적용
            }

            // 로컬 보완
            Map<String, String> local = fallbackExtract(text);
            local.forEach(result::putIfAbsent);

            // 후처리
            if (result.containsKey("seat")) {
                result.put("seat", fixSeatHangulMisread(result.get("seat")));
            }
            if (result.containsKey("time")) {
                String t24 = to24h(result.get("time"));
                if (t24 != null) result.put("time", t24);
            }
            if (result.containsKey("date")) {
                String iso = toIsoDate(result.get("date"));
                if (iso != null) result.put("date", iso);
            }

            return ResponseEntity.ok(result);
        } finally {
            if (tempFile.exists()) tempFile.delete();
        }
    }

    // ────────── 유틸(반드시 클래스 내부!) ──────────

    private static File createTempFromMultipart(MultipartFile file) throws Exception {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("업로드된 파일이 비어 있습니다.");
        }
        String ct = file.getContentType();
        if (ct == null || !(ct.startsWith("image/") || ct.equals("application/octet-stream"))) {
            throw new IllegalArgumentException("이미지 파일만 업로드 가능합니다.");
        }
        String suffix = ".tmp";
        String original = file.getOriginalFilename();
        if (original != null && original.contains(".")) {
            suffix = original.substring(original.lastIndexOf('.'));
        }
        File temp = File.createTempFile("ocr_", suffix);
        file.transferTo(temp);
        return temp;
    }

    private static String stripCodeFence(String s) {
        if (s == null) return "";
        String t = s.trim();
        if (t.startsWith("```")) {
            int idx = t.indexOf('\n');
            if (idx > 0) t = t.substring(idx + 1);
            int end = t.lastIndexOf("```");
            if (end >= 0) t = t.substring(0, end);
        }
        return t;
    }

    private static String str(Object o) { return o == null ? "" : String.valueOf(o); }
    private static String nvl(String s) { return s == null ? "" : s; }
    private static boolean isNullOrEmpty(String s) { return s == null || s.isBlank(); }

    private static boolean containsYTC(String src) {
        if (src == null) return false;
        String l = src.toLowerCase();
        return l.contains("yet to come in busan") || l.contains("yet to come in");
    }

    private static String findVenue(String src) {
        if (src == null) return "";
        var m = Pattern
                .compile("(부산\\s*아시아드\\s*주경기장|블루스퀘어\\s*신한카드홀|예술의전당\\s*[^\\s]+|잠실주경기장|고척스카이돔)")
                .matcher(src);
        return m.find() ? m.group(1) : "";
    }

    private static String findDate(String src) {
        if (src == null) return "";
        var m = Pattern.compile("(20\\d{2})[.년\\-\\s/]*(\\d{1,2})[.월\\-\\s/]*(\\d{1,2})\\s*일?").matcher(src);
        if (m.find()) return String.format("%s-%02d-%02d", m.group(1), Integer.parseInt(m.group(2)), Integer.parseInt(m.group(3)));
        return "";
    }

    private static String findTime(String src) {
        if (src == null) return "";
        var m = Pattern.compile("(오전|오후)?\\s*(\\d{1,2})(?::(\\d{2}))?\\s*(am|pm|AM|PM)?").matcher(src);
        if (m.find()) {
            String apKo = m.group(1);
            String hh = m.group(2);
            String mm = m.group(3) == null ? "00" : m.group(3);
            String ap = m.group(4);
            String t = (apKo != null ? apKo + " " : "") + hh + ":" + mm + (ap != null ? " " + ap : "");
            String t24 = to24h(t);
            return t24 == null ? "" : t24;
        }
        return "";
    }

    private static Map<String, String> fallbackExtract(String src) {
        Map<String, String> m = new LinkedHashMap<>();
        if (src == null) return m;
        String text = src.replace("\r"," ").replace("\n"," ").replaceAll("\\s+"," ").trim();

        // 티켓 전용: artist 필드 제거
        var titleMatcher = Pattern.compile("Yet to Come in BUSAN", Pattern.CASE_INSENSITIVE).matcher(text);
        if (titleMatcher.find()) m.putIfAbsent("title", "Yet to Come in BUSAN");

        var venueMatcher = Pattern.compile("(부산\\s*아시아드\\s*주경기장|예술의전당\\s*[^\\s]+|블루스퀘어\\s*신한카드홀|체조경기장|올림픽공원|고척스카이돔|잠실주경기장)").matcher(text);
        if (venueMatcher.find()) m.putIfAbsent("venue", venueMatcher.group(1));

        var dateMatcher = Pattern.compile("(20\\d{2})[.년\\-\\s/]*(\\d{1,2})[.월\\-\\s/]*(\\d{1,2})\\s*일?").matcher(text);
        if (dateMatcher.find()) {
            m.putIfAbsent("date", String.format("%s-%02d-%02d",
                    dateMatcher.group(1), Integer.parseInt(dateMatcher.group(2)), Integer.parseInt(dateMatcher.group(3))));
        }

        var time12 = Pattern.compile("(오전|오후)?\\s*(\\d{1,2})(?::(\\d{2}))?\\s*(am|pm|AM|PM)?").matcher(text);
        if (time12.find()) {
            String apKo = time12.group(1);
            String hh = time12.group(2);
            String mm = time12.group(3) == null ? "00" : time12.group(3);
            String ap = time12.group(4);
            String t = (apKo != null ? apKo + " " : "") + hh + ":" + mm + (ap != null ? " " + ap : "");
            String t24 = to24h(t);
            if (t24 != null) m.putIfAbsent("time", t24);
        }

        var seatMatcher = Pattern.compile("(\\d+\\s*층[^\\n]*?구역[^\\n]*?(\\d+)\\s*[일열]\\s*(\\d+)\\s*번)").matcher(text);
        if (seatMatcher.find()) {
            String seat = seatMatcher.group(0);
            seat = fixSeatHangulMisread(seat);
            m.putIfAbsent("seat", seat);
        }
        return m;
    }

    private static String fixSeatHangulMisread(String s) {
        if (s == null) return null;
        return s.replaceAll("(\\d+)\\s*일\\s*(\\d+)\\s*번", "$1열 $2번");
    }

    private static String toIsoDate(String in) {
        if (in == null || in.isBlank()) return in;
        if (in.matches("\\d{4}-\\d{2}-\\d{2}")) return in;
        var m = Pattern.compile("(20\\d{2})[.년\\-/\\s]*(\\d{1,2})[.월\\-/\\s]*(\\d{1,2})").matcher(in);
        if (m.find()) return String.format("%s-%02d-%02d", m.group(1), Integer.parseInt(m.group(2)), Integer.parseInt(m.group(3)));
        return in;
    }

    private static String to24h(String in) {
        if (in == null || in.isBlank()) return in;
        String s = in.trim().replaceAll("오전","AM").replaceAll("오후","PM").replaceAll("시",":00");
        var m = Pattern.compile("(?i)\\b(AM|PM)\\b\\s*(\\d{1,2})(?::(\\d{2}))?").matcher(s);
        if (m.find()) {
            String ap = m.group(1).toUpperCase();
            int hh = Integer.parseInt(m.group(2));
            int mm = m.group(3) == null ? 0 : Integer.parseInt(m.group(3));
            if (ap.equals("PM") && hh < 12) hh += 12;
            if (ap.equals("AM") && hh == 12) hh = 0;
            return String.format("%02d:%02d", hh, mm);
        }
        var m2 = Pattern.compile("(\\d{1,2}):(\\d{2})\\s*(?i)(AM|PM)").matcher(s);
        if (m2.find()) {
            int hh = Integer.parseInt(m2.group(1));
            int mm = Integer.parseInt(m2.group(2));
            String ap = m2.group(3).toUpperCase();
            if (ap.equals("PM") && hh < 12) hh += 12;
            if (ap.equals("AM") && hh == 12) hh = 0;
            return String.format("%02d:%02d", hh, mm);
        }
        return s.matches("\\b(\\d{1,2}):(\\d{2})\\b") ? s : in;
    }

    /** record는 컨트롤러 클래스 내부에 둬서 import 충돌 방지 */
    public record OcrResponse(String text) {}
}
