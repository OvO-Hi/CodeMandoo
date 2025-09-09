package com.example.record.STT;

import com.google.cloud.speech.v1.RecognitionAudio;
import com.google.cloud.speech.v1.RecognitionConfig;
import com.google.cloud.speech.v1.RecognizeResponse;
import com.google.cloud.speech.v1.SpeechClient;
import com.google.cloud.speech.v1.SpeechRecognitionResult;
import com.google.protobuf.ByteString;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;

@Service
public class SttService {

    /** 파일 확장자에 따라 인코딩을 고른다.
     *  - wav → LINEAR16
     *  - flac → FLAC
     *  - ogg/opus/oga → OGG_OPUS
     *  - 그 외(mp3, m4a 등) → 자동 감지(ENCODING_UNSPECIFIED)
     */
    private static RecognitionConfig.AudioEncoding pickEncoding(String filePath) {
        if (filePath == null) return RecognitionConfig.AudioEncoding.ENCODING_UNSPECIFIED;
        int dot = filePath.lastIndexOf('.');
        String ext = (dot >= 0 ? filePath.substring(dot + 1) : "").toLowerCase();

        return switch (ext) {
            case "wav"  -> RecognitionConfig.AudioEncoding.LINEAR16;
            case "flac" -> RecognitionConfig.AudioEncoding.FLAC;
            case "ogg", "opus", "oga" -> RecognitionConfig.AudioEncoding.OGG_OPUS;
            default     -> RecognitionConfig.AudioEncoding.ENCODING_UNSPECIFIED; // mp3 등
        };
    }

    // 로컬 음성 파일을 읽어 Google STT API로 텍스트 변환
    public String transcribeLocalFile(String filePath) throws Exception {
        try (SpeechClient speechClient = SpeechClient.create()) {
            Path path = Path.of(filePath);
            byte[] data = Files.readAllBytes(path);
            ByteString audioBytes = ByteString.copyFrom(data);

            // ⬇⬇ 여기만 바뀜: 헬퍼로 인코딩 결정 (MP3 상수 사용 X)
            RecognitionConfig.AudioEncoding enc = pickEncoding(filePath);

            RecognitionConfig config = RecognitionConfig.newBuilder()
                    .setEncoding(enc)
                    .setLanguageCode("ko-KR")
                    .build();

            RecognitionAudio audio = RecognitionAudio.newBuilder()
                    .setContent(audioBytes)
                    .build();

            RecognizeResponse response = speechClient.recognize(config, audio);

            StringBuilder result = new StringBuilder();
            for (SpeechRecognitionResult res : response.getResultsList()) {
                if (res.getAlternativesCount() > 0) {
                    result.append(res.getAlternatives(0).getTranscript()).append(" ");
                }
            }
            return result.toString().trim();
        }
    }
}
