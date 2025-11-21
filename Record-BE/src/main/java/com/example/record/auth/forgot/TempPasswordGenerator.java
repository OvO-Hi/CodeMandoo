package com.example.record.auth.forgot;

import java.security.SecureRandom;

public final class TempPasswordGenerator {
    private static final String UPPER = "ABCDEFGHJKLMNPQRSTUVWXYZ";
    private static final String LOWER = "abcdefghijkmnpqrstuvwxyz";
    private static final String DIGIT = "23456789";
    private static final String ALL = UPPER + LOWER + DIGIT;
    private static final SecureRandom RND = new SecureRandom();

    private TempPasswordGenerator() {}

    public static String generate(int len) {
        if (len < 8) len = 8;

        // 최소 요건: 숫자 1+ 영문 1+ 보장
        StringBuilder sb = new StringBuilder(len);
        sb.append(UPPER.charAt(RND.nextInt(UPPER.length())));
        sb.append(LOWER.charAt(RND.nextInt(LOWER.length())));
        sb.append(DIGIT.charAt(RND.nextInt(DIGIT.length())));

        for (int i = sb.length(); i < len; i++) {
            sb.append(ALL.charAt(RND.nextInt(ALL.length())));
        }

        // 간단 셔플
        for (int i = sb.length() - 1; i > 0; i--) {
            int j = RND.nextInt(i + 1);
            char c = sb.charAt(i);
            sb.setCharAt(i, sb.charAt(j));
            sb.setCharAt(j, c);
        }
        return sb.toString();
    }
}
