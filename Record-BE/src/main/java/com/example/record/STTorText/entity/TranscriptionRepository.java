package com.example.record.STTorText.entity;

import com.example.record.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TranscriptionRepository extends JpaRepository<Transcription, Long> {
    List<Transcription> findByUser(User user);
}
