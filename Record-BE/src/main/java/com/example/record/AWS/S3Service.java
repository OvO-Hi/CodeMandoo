// S3Service: AWS S3에 파일을 업로드하고, 업로드된 파일의 공개 URL을 반환하는 서비스 클래스입니다.


package com.example.record.AWS;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class S3Service {

    // application.yml에서 S3 관련 설정 주입
    @Value("${cloud.aws.credentials.access-key}")
    private String accessKey;

    @Value("${cloud.aws.credentials.secret-key}")
    private String secretKey;

    @Value("${cloud.aws.region.static}")
    private String region;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    // MultipartFile을 받아 AWS S3에 업로드하고 업로드된 파일의 URL을 반환
    public String uploadFile(MultipartFile file) throws IOException {
        // S3 클라이언트 생성 (액세스 키, 시크릿 키, 리전 기반)
        S3Client s3Client = S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(accessKey, secretKey)))
                .build();

        // 고유한 파일 이름 생성 (UUID + 원래 파일명)
        String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();

        // S3에 업로드할 요청 객체 생성
        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucket)
                .key(fileName)
                .acl("public-read") // 업로드된 파일을 외부에서 접근 가능하도록 설정
                .contentType(file.getContentType())
                .build();

        // 실제 파일 업로드 수행
        s3Client.putObject(request, RequestBody.fromBytes(file.getBytes()));

        // 업로드된 파일의 공개 URL 반환
        return "https://" + bucket + ".s3." + region + ".amazonaws.com/" + fileName;
    }
}

