package ro.ase.ism.dissertation.service;

import io.minio.BucketExistsArgs;
import io.minio.GetObjectArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.errors.ErrorResponseException;
import io.minio.errors.InsufficientDataException;
import io.minio.errors.InternalException;
import io.minio.errors.InvalidResponseException;
import io.minio.errors.ServerException;
import io.minio.errors.XmlParserException;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileStorageService {

    private final MinioClient minioClient;

    @Value("${minio.bucketName}")
    private String bucketName;

    @PostConstruct
    public void initBucket() {
        try {
            boolean found = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
            if (!found) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
                log.info("Created new bucket '{}'", bucketName);
            }
        } catch (ServerException | InsufficientDataException | IOException | NoSuchAlgorithmException |
                 InvalidKeyException | XmlParserException | InternalException | ErrorResponseException |
                 InvalidResponseException e) {
            throw new RuntimeException("Could not initialize MinIO bucket", e);
        }
    }

    public String uploadFile(String prefix, MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        String extension = FilenameUtils.getExtension(originalFilename);
        String objectKey = prefix + "/" + UUID.randomUUID() + "." + extension;

        try (InputStream stream = file.getInputStream()) {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectKey)
                            .stream(stream, file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build()
            );
            log.info("Uploaded file to MinIO: {}", objectKey);
            return objectKey;
        } catch (ServerException | InsufficientDataException | IOException | NoSuchAlgorithmException |
                 InvalidKeyException | XmlParserException | InternalException | ErrorResponseException |
                 InvalidResponseException e) {
            throw new RuntimeException("Failed to upload file", e);
        }
    }

    public InputStream downloadFile(String objectKey) {
        try {
            return minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectKey)
                            .build()
            );
        } catch (ServerException | InsufficientDataException | IOException | NoSuchAlgorithmException |
                 InvalidKeyException | XmlParserException | InternalException | ErrorResponseException |
                 InvalidResponseException e) {
            throw new RuntimeException("Failed to download file", e);
        }
    }

    public void deleteFile(String objectKey) {
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectKey)
                            .build()
            );
            log.info("Deleted file from MinIO: {}", objectKey);
        } catch (ServerException | InsufficientDataException | IOException | NoSuchAlgorithmException |
                 InvalidKeyException | XmlParserException | InternalException | ErrorResponseException |
                 InvalidResponseException e) {
            throw new RuntimeException("Failed to delete file", e);
        }
    }
}
