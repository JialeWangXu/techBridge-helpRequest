package es.techbridge.techbridgehelprequest.application.port.in;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface SupportResourceUseCases {
    String uploadSupportSessionResource(String key, MultipartFile file) throws IOException;
    String downLoadSupportSessionResource(String key);
}
