package digital.paisley.storage.service.controller;

import digital.paisley.storage.service.dto.MetadataDTO;
import digital.paisley.storage.service.dto.MinioMetadataDTO;
import digital.paisley.storage.service.enums.MinioMetadata;
import digital.paisley.storage.service.service.IStorageService;
import digital.paisley.storage.service.util.MetadataToDTO;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/storage")
public class StorageController {

    private final IStorageService storageService;

    public StorageController(@Qualifier("minioService") IStorageService storageService) {
        this.storageService = storageService;
    }

    // Upload a file
    @PostMapping(value = "/files/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> uploadFile(
            @RequestParam("file-name") String fileName,
            @RequestParam("file-stream") MultipartFile fileStream,
            @RequestParam Map<String, Object> metadata) {

        MetadataDTO metadataDTO = MetadataToDTO.createMetadata(metadata, MinioMetadataDTO.class);
        String fileAddress = storageService.uploadFile(fileStream, fileName, metadataDTO);

        return ResponseEntity.ok("File uploaded successfully: " + fileAddress);
    }

    // Download a file
    @GetMapping(value = "/files/download", produces = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Resource> downloadFile(
            @RequestParam("file-name") String fileName,
            @RequestParam Map<String, Object> metadata) {

        MetadataDTO metadataDTO = MetadataToDTO.createMetadata(metadata, MinioMetadataDTO.class);
        InputStream fileStream = storageService.downloadFile(fileName, metadataDTO);
        Resource resource = new InputStreamResource(fileStream);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", fileName);

        return new ResponseEntity<>(resource, headers, HttpStatus.OK);
    }

    // Delete files
    @DeleteMapping(value = "/files/delete", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> deleteFiles(
            @RequestParam("file-name") String fileName,
            @RequestParam Map<String, Object> metadata) {

        MetadataDTO metadataDTO = MetadataToDTO.createMetadata(metadata, MinioMetadataDTO.class);
        storageService.deleteFile(fileName, metadataDTO);

        return ResponseEntity.ok("File deleted successfully: " + fileName);

    }

    // Check if a file exists
    @GetMapping(value = "/files/exists")
    public ResponseEntity<Boolean> fileExists(
            @RequestParam("file-name") String fileName,
            @RequestParam Map<String, Object> metadata) {

        MetadataDTO metadataDTO = MetadataToDTO.createMetadata(metadata, MinioMetadataDTO.class);
        boolean exists = storageService.fileExists(fileName, metadataDTO);

        return ResponseEntity.ok(exists);

    }
}
