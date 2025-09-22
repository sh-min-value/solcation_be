package org.solcation.solcation_be.util.s3;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class S3Utils {
    private final S3Client s3Client;

    @Value("${cloud.s3.bucket.name}")
    private String BUCKET_NAME;

    /* S3 객체 업로드 */
    public String uploadObject(MultipartFile file, String filename, String path) {
        try {
            String newFilename = getUniqueFileName(filename);

            PutObjectRequest objectRequest = PutObjectRequest.builder()
                    .bucket(BUCKET_NAME)
                    .key(path + newFilename)
                    .contentType(file.getContentType())
                    .contentLength(file.getSize())
                    .build();
            s3Client.putObject(objectRequest, RequestBody.fromBytes(file.getBytes()));

            return newFilename;
        } catch(IOException e) {
            log.info("Failed to upload object to s3", e);
        }
        return null;
    }

    /* S3 객체 삭제 */
    public void deleteObject(String filename, String path) {
        try {
            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(BUCKET_NAME)
                    .key(path + filename)
                    .build();

            s3Client.deleteObject(deleteObjectRequest);

        } catch (AwsServiceException | SdkClientException e) {
            e.printStackTrace();
        }
    }

    /* 고유 파일 이름 생성 */
    public String getUniqueFileName(String filename) {
        StringBuilder sb = new StringBuilder();
        sb.append(UUID.randomUUID().toString())
                .append("_")
                .append(filename);

        return sb.toString();
    }

    /* 파일 url */
    public String getPublicUrl(String filename, String path) {
        return String.format("https://%s.s3.%s.amazonaws.com/%s", BUCKET_NAME, s3Client.serviceClientConfiguration().region(), path + filename);
    }

    /* 확장자 확인 확인 및 제한 */
    public boolean checkExtension(String filename) {
        if(filename.isEmpty()) {
            return false;
        }
        List<String> possibleExt = Arrays.asList(".jpg", ".jpeg", ".png");
        String extension = filename.substring(filename.lastIndexOf("."));

        return possibleExt.contains(extension);
    }

    /* 삭제 실패 시 복구를 위한 임시 백업 */
    public void copyObject(String sourceKey, String sourcePath, String targetKey, String targetPath) {
        try {
            s3Client.copyObject(builder -> builder
                    .copySource(BUCKET_NAME + "/" + sourcePath + sourceKey)
                    .bucket(BUCKET_NAME)
                    .key(targetPath + targetKey)
            );
        } catch (AwsServiceException | SdkClientException e) {
            log.error("S3 객체 복사 실패: {} -> {}", sourceKey, targetKey, e);
        }
    }

}
