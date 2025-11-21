package com.example.record.promptcontrol_w03.dto;

import java.util.List;

public class PromptRequest {

    private String title;
    private String location;
    private String date;
    private String genre;
    private List<String> cast;

    /** 이미지 생성 시 사용자 추가 요청 (선택) */
    private String imageRequest;

    /** gpt-image-1은 1장만 생성하기 때문에 의미 없음 — 필요시 남겨두되 사용 X */
    private String size;
    private int n = 1;

    /** summarize() → 영어 5줄 요약 결과 */
    private String basePrompt;

    // ===== Getter =====
    public String getTitle() { return title; }
    public String getLocation() { return location; }
    public String getDate() { return date; }
    public String getGenre() { return genre; }
    public List<String> getCast() { return cast; }
    public String getImageRequest() { return imageRequest; }
    public String getSize() { return size; }
    public int getN() { return n; }
    public String getBasePrompt() { return basePrompt; }

    // ===== Setter =====
    public void setTitle(String title) { this.title = title; }
    public void setLocation(String location) { this.location = location; }
    public void setDate(String date) { this.date = date; }
    public void setGenre(String genre) { this.genre = genre; }
    public void setCast(List<String> cast) { this.cast = cast; }
    public void setImageRequest(String imageRequest) { this.imageRequest = imageRequest; }
    public void setSize(String size) { this.size = size; }
    public void setN(int n) { this.n = n; }
    public void setBasePrompt(String basePrompt) { this.basePrompt = basePrompt; }
}
