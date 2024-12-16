package digital.paisley.storage.service.dto;

import lombok.Data;

@Data
public class UploadFileResponse {
    private String fileName;
    private String fileFormat;
    private String serviceName;
    private String fileUrl;
}
