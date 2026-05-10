package es.techbridge.techbridgehelprequest.domain.services;

import es.techbridge.techbridgehelprequest.application.port.in.SupportResourceUseCases;
import es.techbridge.techbridgehelprequest.application.port.out.resourceFacade.ResourceFacade;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.io.IOException;
import java.time.Duration;

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
