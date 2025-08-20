package com.fivlo.fivlo_backend.domain.growth.service;

import com.fivlo.fivlo_backend.domain.growth.dto.PresignedUrlRequest;
import com.fivlo.fivlo_backend.domain.growth.dto.PresignedUrlResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

/**
 * S3 파일 업로드 서비스
 * S3 PreSigned URL 생성 및 파일 업로드 관리
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class S3FileUploadService {

    private final S3Presigner s3Presigner;

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    @Value("${aws.region}")
    private String region;

    // PreSigned URL 유효 시간 (15분)
    private static final Duration PRESIGNED_URL_DURATION = Duration.ofMinutes(15);

    /**
     * 성장앨범 이미지 업로드용 PreSigned URL 생성
     */
    public PresignedUrlResponse generatePresignedUrl(PresignedUrlRequest request) {
        log.info("PreSigned URL 생성 시작 - fileName: {}, contentType: {}", 
                request.getFileName(), request.getContentType());

        try {
            // S3 객체 키 생성 (UUID + 원본 파일 확장자)
            String s3ObjectKey = generateS3ObjectKey(request.getFileName());
            
            // S3 Put 요청 생성
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s3ObjectKey)
                    .contentType(request.getContentType())
                    .build();

            // PreSigned URL 생성
            PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                    .signatureDuration(PRESIGNED_URL_DURATION)
                    .putObjectRequest(putObjectRequest)
                    .build();

            PresignedPutObjectRequest presignedRequest = s3Presigner.presignPutObject(presignRequest);
            
            // 응답 데이터 생성
            String presignedUrl = presignedRequest.url().toString();
            String s3ObjectUrl = generateS3ObjectUrl(s3ObjectKey);
            LocalDateTime expirationTime = LocalDateTime.now().plus(PRESIGNED_URL_DURATION);

            log.info("PreSigned URL 생성 완료 - s3ObjectKey: {}, expirationTime: {}", 
                    s3ObjectKey, expirationTime);

            return PresignedUrlResponse.builder()
                    .presignedUrl(presignedUrl)
                    .s3ObjectUrl(s3ObjectUrl)
                    .s3ObjectKey(s3ObjectKey)
                    .expirationTime(expirationTime)
                    .build();

        } catch (Exception e) {
            log.error("PreSigned URL 생성 실패 - fileName: {}", request.getFileName(), e);
            throw new RuntimeException("PreSigned URL 생성에 실패했습니다.", e);
        }
    }

    /**
     * S3 객체 키 생성 (growth-albums/{UUID}.{extension})
     */
    private String generateS3ObjectKey(String originalFileName) {
        String fileExtension = extractFileExtension(originalFileName);
        String uuid = UUID.randomUUID().toString();
        return String.format("growth-albums/%s.%s", uuid, fileExtension);
    }

    /**
     * 파일 확장자 추출
     */
    private String extractFileExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return "jpg"; // 기본 확장자
        }
        return fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
    }

    /**
     * S3 객체 URL 생성
     */
    private String generateS3ObjectUrl(String s3ObjectKey) {
        return String.format("https://%s.s3.%s.amazonaws.com/%s", 
                bucketName, region, s3ObjectKey);
    }
}
