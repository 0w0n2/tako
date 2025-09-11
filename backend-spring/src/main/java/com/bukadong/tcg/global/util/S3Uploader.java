package com.bukadong.tcg.global.util;

import com.bukadong.tcg.global.common.base.BaseResponseStatus;
import com.bukadong.tcg.global.common.exception.BaseException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class S3Uploader {

    private static final Logger log = LoggerFactory.getLogger(S3Uploader.class);

    private final S3Client s3Client;

    // application.yaml에 설정한 버킷 이름을 주입받기
    @Value("${spring.cloud.aws.s3.bucket}")
    private String bucket;

    /**
     * S3에 파일을 업로드하고, 저장된 파일의 경로를 반환
     *
     * @param multipartFile 업로드할 파일
     * @param dirName       파일을 저장할 디렉토리 이름 (ex: "thumbnails", "profiles")
     * @return S3에 저장된 파일의 키 (ex: "thumbnails/a1b2c3d4.jpg")
     */
    public String upload(MultipartFile multipartFile, String dirName) {
        if (multipartFile == null || multipartFile.isEmpty()) {
            return null;
        }

        // 원본 파일에서 확장자 추출
        String originalFileName = multipartFile.getOriginalFilename();
        String extension = "";
        if (originalFileName != null && originalFileName.contains(".")) {
            extension = originalFileName.substring(originalFileName.lastIndexOf("."));
        }

        // UUID를 사용한 고유한 파일명 생성 (디렉토리/UUID.확장자)
        String uniqueFileKey = dirName + "/" + UUID.randomUUID().toString() + extension;

        // S3에 업로드할 요청 객체 생성
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucket)
                .key(uniqueFileKey)
                .contentType(multipartFile.getContentType())
                .contentLength(multipartFile.getSize())
                .build();

        try {
            // S3에 파일 업로드
            s3Client.putObject(putObjectRequest, RequestBody.fromBytes(multipartFile.getBytes()));
        } catch (IOException e) {
            throw new BaseException(BaseResponseStatus.S3_FILE_UPLOAD_FAILED);
        }

        // DB에 저장할 파일의 키(경로)를 반환
        return uniqueFileKey;
    }

    /**
     * S3에서 파일을 삭제
     *
     * @param fileKey = 삭제할 파일의 DB에 저장된 값
     */
    public void delete(String fileKey) {
        if (!StringUtils.hasText(fileKey)) {
            return;
        }

        try {
            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(bucket)
                    .key(fileKey)
                    .build();
            s3Client.deleteObject(deleteObjectRequest);
        } catch (Exception e) {
            // 삭제 실패 로그, 별도의 방법으로 처리하기 (보통 주기적인 batch 작업으로 정리)
            log.error("S3 파일 삭제에 실패했습니다. fileKey: {}", fileKey, e);
        }
    }

    /**
     * S3 파일의 전체 URL을 가져오기
     *
     * @param fileKey = S3 파일 키
     * @return 파일의 전체 URL
     */
    public String getFileUrl(String fileKey) {
        if (StringUtils.hasText(fileKey)) {
            return null;
        }
        return s3Client.utilities().getUrl(builder -> builder.bucket(bucket).key(fileKey)).toExternalForm();
    }
}