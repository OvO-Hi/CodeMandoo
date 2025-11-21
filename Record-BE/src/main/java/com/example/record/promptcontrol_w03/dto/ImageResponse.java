package com.example.record.promptcontrol_w03.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)  // null 필드 자동 숨김
public class ImageResponse {

    private String prompt;
    private String imageUrl;
    private String error;

    public ImageResponse() {}

    @Builder
    public ImageResponse(String prompt, String imageUrl, String error) {
        this.prompt = prompt;
        this.imageUrl = imageUrl;
        this.error = error;
    }

    /** 성공 응답 */
    public static ImageResponse success(String prompt, String imageUrl) {
        return ImageResponse.builder()
                .prompt(prompt)
                .imageUrl(imageUrl)
                .build();
    }

    /** 에러 응답 */
    public static ImageResponse error(String message) {
        return ImageResponse.builder()
                .error(message)
                .build();
    }
}
