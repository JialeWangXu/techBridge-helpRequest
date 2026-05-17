package es.techbridge.techbridgehelprequest.application.services;

import es.techbridge.techbridgehelprequest.application.port.in.SupportResourceUseCases;
import es.techbridge.techbridgehelprequest.application.port.out.resourceFacade.ResourceFacade;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
public class SupportResourceService implements SupportResourceUseCases {

    private final ResourceFacade resourceFacade;

    public SupportResourceService(ResourceFacade resourceFacade) {
        this.resourceFacade = resourceFacade;
    }

    @Override
    public String uploadSupportSessionResource(String key, MultipartFile file) throws IOException {
        this.resourceFacade.uploadResource(key,file);
        return key;
    }

    @Override
    public String downLoadSupportSessionResource(String key) {
        return this.resourceFacade.downLoadResource(key);
    }
}
