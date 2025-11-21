package com.example.record.STTorText.review;

public record ReviewRequest(
        Long transcriptionId,
        String text
) {}
