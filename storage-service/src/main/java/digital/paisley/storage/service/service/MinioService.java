package digital.paisley.storage.service.service;

import digital.paisley.storage.service.dto.MinioMetadataDTO;
import digital.paisley.storage.service.dto.MetadataDTO;
import digital.paisley.storage.service.dto.UploadFileResponse;
import io.minio.MinioClient;
import io.minio.StatObjectArgs;
import io.minio.PutObjectArgs;
import io.minio.GetObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.errors.ErrorResponseException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

/**
 * Implementation of the storage service using MinIO.
 */
@Service
@Slf4j
public class MinioService implements IStorageService {

    private final MinioClient minioClient;

    /**
     * Constructor for MinioService.
     *
     * @param endpoint  MinIO endpoint.
     * @param accessKey Access key.
     * @param secretKey Secret key.
     */
    public MinioService(
            @Value("${minio.endpoint}") String endpoint,
            @Value("${minio.access-key}") String accessKey,
            @Value("${minio.secret-key}") String secretKey
    ) {
        this.minioClient = MinioClient.builder()
                .endpoint(endpoint)
                .credentials(accessKey, secretKey)
                .build();
    }

    /**
     * Ensures that the specified bucket exists.
     *
     * @param bucketName Name of the bucket.
     * @throws RuntimeException if the bucket does not exist.
     */
    private void validateBucketExists(String bucketName) {
        try {
            boolean exists = minioClient.bucketExists(
                    io.minio.BucketExistsArgs.builder().bucket(bucketName).build()
            );
            if (!exists) {
                log.error("Bucket '{}' does not exist.", bucketName);
                throw new RuntimeException("Bucket '" + bucketName + "' does not exist.");
            }
            log.info("Bucket '{}' exists.", bucketName);
        } catch (Exception e) {
            log.error("Error checking bucket existence: {}", e.getMessage());
            throw new RuntimeException("Error checking bucket existence: " + e.getMessage(), e);
        }
    }

    /**
     * Creates a folder within the specified bucket.
     *
     * @param bucketName Name of the bucket.
     * @param folderName Name of the folder to create.
     */
    private void createFolder(String bucketName, String folderName) {
        String objectName = folderName.endsWith("/") ? folderName : folderName + "/";
        try {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .stream(new java.io.ByteArrayInputStream(new byte[0]), 0, -1)
                            .build()
            );
            log.info("Folder '{}' created in bucket '{}'.", folderName, bucketName);
        } catch (Exception e) {
            log.error("Error creating folder '{}': {}", folderName, e.getMessage());
            throw new RuntimeException("Error creating folder '" + folderName + "': " + e.getMessage(), e);
        }
    }

    /**
     * Extracts bucket name and validates its existence.
     * Creates the specified folder if it does not exist.
     *
     * @param metadata Metadata containing bucket and folder information.
     */
    private void prepareStorage(MinioMetadataDTO metadata) {
        String bucketName = metadata.getBucketName();
        String folderName = metadata.getFolderName();

        if (bucketName == null || bucketName.isEmpty()) {
            throw new IllegalArgumentException("Service name is required in metadata.");
        }

        validateBucketExists(bucketName);
        createFolder(bucketName, folderName);
    }

    @Override
    public UploadFileResponse uploadFile(MultipartFile fileStream, String fileName, MetadataDTO metadata) {

        if (!(metadata instanceof MinioMetadataDTO)) {
            throw new IllegalArgumentException("Invalid metadata type.");
        }
        MinioMetadataDTO minioMetadata = (MinioMetadataDTO) metadata;
        prepareStorage(minioMetadata);

        // Extract the file format
        String originalFilename = fileStream.getOriginalFilename();
        String fileExtension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            fileExtension = originalFilename.substring(originalFilename.lastIndexOf(".") + 1);
        }

        // Build a file with the correct format
        String objectName = minioMetadata.getFolderName() + "/" + fileName ;
        try {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(minioMetadata.getBucketName())
                            .object(objectName)
                            .stream(fileStream.getInputStream(), -1, 10485760) // Max part size: 10 MB
                            .build()
            );
            log.info("File '{}' uploaded to '{}/{}'.", fileName, minioMetadata.getBucketName(), minioMetadata.getFolderName());
            UploadFileResponse response = new UploadFileResponse();
            response.setFileName(fileName);
            response.setFileFormat(fileExtension);
            response.setFileUrl(minioMetadata.getBucketName() + "/" + objectName);
            response.setServiceName("Minio");
            return response;
        } catch (Exception e) {
            log.error("Error uploading file '{}': {}", fileName, e.getMessage());
            throw new RuntimeException("Error uploading file: " + e.getMessage(), e);
        }
    }

    @Override
    public InputStream downloadFile(String fileName, MetadataDTO metadata) {
        if (!(metadata instanceof MinioMetadataDTO)) {
            throw new IllegalArgumentException("Invalid metadata type.");
        }
        MinioMetadataDTO minioMetadata = (MinioMetadataDTO) metadata;
        prepareStorage(minioMetadata);

        String objectName = minioMetadata.getFolderName() + "/" + fileName;
        try {
            InputStream inputStream = minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(minioMetadata.getBucketName())
                            .object(objectName)
                            .build()
            );
            log.info("File '{}' downloaded from '{}/{}'.", fileName, minioMetadata.getBucketName(), minioMetadata.getFolderName());
            return inputStream;
        } catch (Exception e) {
            log.error("Error downloading file '{}': {}", fileName, e.getMessage());
            throw new RuntimeException("Error downloading file: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteFile(String fileName, MetadataDTO metadata) {
        if (!fileExists(fileName, metadata)) {
            throw new RuntimeException("File does not exist");
        }
        if (!(metadata instanceof MinioMetadataDTO)) {
            throw new IllegalArgumentException("Invalid metadata type.");
        }
        MinioMetadataDTO minioMetadata = (MinioMetadataDTO) metadata;
        prepareStorage(minioMetadata);

        String objectName = minioMetadata.getFolderName() + "/" + fileName;
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(minioMetadata.getBucketName())
                            .object(objectName)
                            .build()
            );
            log.info("File '{}' deleted from '{}/{}'.", fileName, minioMetadata.getBucketName(), minioMetadata.getFolderName());
        } catch (Exception e) {
            log.error("Error deleting file '{}': {}", fileName, e.getMessage());
            throw new RuntimeException("Error deleting file: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean fileExists(String fileName, MetadataDTO metadata) {
        if (!(metadata instanceof MinioMetadataDTO)) {
            throw new IllegalArgumentException("Invalid metadata type.");
        }
        MinioMetadataDTO minioMetadata = (MinioMetadataDTO) metadata;
        prepareStorage(minioMetadata);

        String objectName = minioMetadata.getFolderName() + "/" + fileName;
        try {
            minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(minioMetadata.getBucketName())
                            .object(objectName)
                            .build()
            );
            log.info("File '{}' exists in '{}/{}'.", fileName, minioMetadata.getBucketName(), minioMetadata.getFolderName());
            return true;
        } catch (Exception e) {
            if (e instanceof ErrorResponseException) {
                ErrorResponseException error = (ErrorResponseException) e;
                if ("NoSuchKey".equals(error.errorResponse().code())) {
                    log.info("File '{}' does not exist in '{}/{}'.", fileName, minioMetadata.getBucketName(), minioMetadata.getFolderName());
                    return false;
                }
            }
            log.error("Error checking existence of file '{}': {}", fileName, e.getMessage());
            throw new RuntimeException("Error checking file existence: " + e.getMessage(), e);
        }
    }
}
