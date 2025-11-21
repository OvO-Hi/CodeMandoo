package com.example.record.promptcontrol_w03.dto;
/*
역할: 프롬프트/문장 유틸.

기능

정확히 2~3문장으로 강제(4문장 이상이면 3문장으로 합치기)

문장 경계 단위로 길이 클램프

{name=…, description=…} 같은 의사-JSON 캐릭터 문자열을 자연어로 정리
 */

import lombok.experimental.UtilityClass;

@UtilityClass
public class PromptTextUtils {

    /** 정확히 2~3문장으로 강제 (4문장 이상은 3문장으로 합침) */
    public String enforceTwoOrThreeSentences(String text) {
        if (text == null) return "";
        String[] parts = text.trim().replaceAll("\\s+", " ")
                .split("(?<=[.!?])\\s+");
        if (parts.length <= 3) return String.join(" ", parts);

        String s1 = parts[0];
        String s2 = parts.length > 1 ? parts[1] : "";
        StringBuilder s3 = new StringBuilder();
        for (int i = 2; i < parts.length; i++) {
            if (s3.length() > 0) s3.append(' ');
            s3.append(parts[i].replaceAll("\\s+", " "));
        }
        String third = s3.toString().trim();
        if (!third.matches(".*[.!?]$")) third += ".";
        return String.join(" ", java.util.List.of(s1, s2, third));
    }

    /** 문장 경계 기반 길이 가드 */
    public String clampBySentence(String text, int max) {
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

    /** 문자열 형태 캐릭터 설명(JSON 비슷한 포맷 포함) 정리 */
    public String cleanCharacterDescription(String description) {
        if (description == null || description.trim().isEmpty()) return "";
        String desc = description.trim();

        if (desc.startsWith("{") && desc.contains("name=")) {
            int nameStart = desc.indexOf("name=");
            if (nameStart >= 0) {
                int nameEnd = desc.indexOf(",", nameStart);
                if (nameEnd == -1) nameEnd = desc.indexOf("}", nameStart);
                if (nameEnd > nameStart) {
                    String name = desc.substring(nameStart + 5, nameEnd).trim();
                    int dStart = desc.indexOf("description=");
                    if (dStart >= 0) {
                        int dEnd = desc.indexOf(",", dStart);
                        if (dEnd == -1) dEnd = desc.indexOf("}", dStart);
                        if (dEnd > dStart) {
                            String d = desc.substring(dStart + 11, dEnd).trim();
                            return name + (!d.isEmpty() ? " (" + d + ")" : "");
                        }
                    }
                    return name;
                }
            }
        }
        return desc;
    }
}
