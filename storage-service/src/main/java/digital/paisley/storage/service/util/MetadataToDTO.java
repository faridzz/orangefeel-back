package digital.paisley.storage.service.util;

import digital.paisley.storage.service.dto.MetadataDTO;
import digital.paisley.storage.service.dto.MinioMetadataDTO;
import digital.paisley.storage.service.enums.MinioMetadata;

import java.util.Map;

public class MetadataToDTO {

    private MetadataToDTO() {
    }

    public static <T extends MetadataDTO> T createMetadata(Map<String, Object> metadata, Class<T> metadataClass) {
        validateMetadata(metadata);

        try {
            if (metadataClass == MinioMetadataDTO.class) {
                return metadataClass.getDeclaredConstructor(MinioMetadataDTO.class)
                        .newInstance(createMinioMetadata(metadata));
            }

            throw new IllegalArgumentException("Unsupported metadata type: " + metadataClass.getName());
        } catch (Exception e) {
            throw new RuntimeException("Error creating metadata instance for class: " + metadataClass.getName(), e);
        }
    }

    private static MinioMetadataDTO createMinioMetadata(Map<String, Object> metadata) {
        return new MinioMetadataDTO(
                MinioMetadata.BUCKET_NAME.getValue(metadata),
                MinioMetadata.FOLDER_NAME.getValue(metadata)
        );
    }

    private static void validateMetadata(Map<String, Object> metadata) {
        if (metadata == null || metadata.isEmpty()) {
            throw new IllegalArgumentException("Metadata map cannot be null or empty.");
        }
    }
}
