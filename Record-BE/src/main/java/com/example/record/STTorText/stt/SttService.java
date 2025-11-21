
package com.example.record.STTorText.stt;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
public class SttService {

    @Value("${stt.ffmpeg.path:ffmpeg}")
    private String ffmpegPath;

    /** 25MB 초과 파일은 m4a(16kHz/mono)로 재인코딩 */
    public byte[] maybeReencodeToM4a(byte[] inputBytes, String originalSuffix) throws Exception {
        if (inputBytes.length <= 24 * 1024 * 1024) return inputBytes;

        Path src = null, out = null;
        try {
            src = Files.createTempFile("whisper_src_", originalSuffix != null ? originalSuffix : ".tmp");
            Files.write(src, inputBytes);

            out = Files.createTempFile("whisper_enc_", ".m4a");
            ProcessBuilder pb = new ProcessBuilder(
                    ffmpegPath, "-y",
                    "-i", src.toString(),
                    "-ac", "1",
                    "-ar", "16000",
                    "-c:a", "aac",
                    "-b:a", "64k",
                    out.toString()
            ).redirectErrorStream(true);

            Process p = pb.start();
            try (BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream(), StandardCharsets.UTF_8))) {
                while (br.readLine() != null) { /* consume */ }
            }
            int exit = p.waitFor();
            if (exit != 0) throw new RuntimeException("ffmpeg 재인코딩 실패(exit=" + exit + ")");

            return Files.readAllBytes(out);
        } finally {
            try { if (src != null) Files.deleteIfExists(src); } catch (Exception ignore) {}
            try { if (out != null) Files.deleteIfExists(out); } catch (Exception ignore) {}
        }
    }
}
