package digital.paisley.storage.service.dto;

import lombok.*;

import java.util.Map;

/**
 * Metadata DTO specific to MinIO operations.
 */
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class MinioMetadataDTO implements MetadataDTO {
    private String bucketName;
    private String folderName;

    public MinioMetadataDTO(Map<String, Object> metadata) {
        this.bucketName = (String) metadata.get("bucket-name");
        this.folderName = (String) metadata.get("folder-name");
    }

    public MinioMetadataDTO(MinioMetadataDTO minioMetadataDTO) {
        this.bucketName = minioMetadataDTO.getBucketName();
        this.folderName = minioMetadataDTO.getFolderName();
    }

}
