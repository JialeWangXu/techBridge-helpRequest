package es.techbridge.techbridgehelprequest.application.port.out.resourceFacade;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface ResourceFacade {
    String uploadResource(String key, MultipartFile file) throws IOException;
    String downLoadResource(String key);
}
