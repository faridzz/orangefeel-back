package digital.paisley.storage.service.service;

import digital.paisley.storage.service.dto.MetadataDTO;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

/**
 * Interface for storage.
 */
public interface IStorageService {

    /**
     * Uploads a file to the storage.
     *
     * @param fileStream File content as MultipartFile.
     * @param fileName   Name of the file to be stored.
     * @param metadata   Metadata for the storage.
     * @return File location in storage
     */
    String uploadFile(MultipartFile fileStream, String fileName, MetadataDTO metadata);

    /**
     * Downloads a file from the storage.
     *
     * @param fileName Name of the file to be downloaded.
     * @param metadata Metadata for the storage.
     * @return File content as InputStream.
     */
    InputStream downloadFile(String fileName, MetadataDTO metadata);

    /**
     * Deletes a file from the storage.
     *
     * @param fileName Name of the file to be deleted.
     * @param metadata Metadata for the storage.
     */
    void deleteFile(String fileName, MetadataDTO metadata);

    /**
     * Checks if a file exists in the storage.
     *
     * @param fileName Name of the file.
     * @param metadata Metadata for the storage.
     * @return True if the file exists, false otherwise.
     */
    boolean fileExists(String fileName, MetadataDTO metadata);
}