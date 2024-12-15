package digital.paisley.storage.service.enums;

import java.util.Map;

public enum MinioMetadata {
    BUCKET_NAME("bucket-name"),
    FOLDER_NAME("folder-name");
    private final String key;

    MinioMetadata(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }

    /**
     * Gets the value of the metadata key from the map.
     *
     * @param metadata Map containing metadata keys and values.
     * @return Value corresponding to the key.
     */
    public String getValue(Map<String, Object> metadata) {
        Object value = metadata.get(key);
        if (value == null || !(value instanceof String) || ((String) value).isEmpty()) {
            throw new IllegalArgumentException(key + " cannot be null, empty, or invalid.");
        }
        return (String) value;
    }
}
