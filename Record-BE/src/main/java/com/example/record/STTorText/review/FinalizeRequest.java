package com.example.record.STTorText.review;

public record FinalizeRequest(
        Long transcriptionId,
        String extraNotes
) {}
