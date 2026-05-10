package es.techbridge.techbridgehelprequest.domain.services;

import es.techbridge.techbridgehelprequest.application.port.in.SupportSessionUseCases;
import es.techbridge.techbridgehelprequest.domain.exceptions.FailUploadResourceException;
import es.techbridge.techbridgehelprequest.domain.exceptions.NotFoundException;
import es.techbridge.techbridgehelprequest.domain.model.supportsession.SupportSession;
import es.techbridge.techbridgehelprequest.application.port.out.persistence.SupportSessionPersistence;
import es.techbridge.techbridgehelprequest.infrastructure.postgresql.entities.HelpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Service
public class SupportSessionService implements SupportSessionUseCases {

    private final SupportSessionPersistence supportSessionPersistence;
    private final SupportResourceService supportResourceService;

    @Autowired
    public SupportSessionService(SupportSessionPersistence supportSessionPersistence, SupportResourceService supportResourceService) {
        this.supportSessionPersistence = supportSessionPersistence;
        this.supportResourceService = supportResourceService;
    }

    public SupportSession create(SupportSession supportSession){
        return this.supportSessionPersistence.create(supportSession).toSupportSession();
    }

    public void updateHelpStatusById(UUID id, HelpStatus status){
        this.supportSessionPersistence.updateHelpStatusById(status,id).toSupportSession();
    }

    public SupportSession updateSupportSession(SupportSession supportSession, UUID id){
        return this.supportSessionPersistence.updateSupportSession(supportSession,id).toSupportSession();
    }

    public void deleteById(UUID id){
        this.supportSessionPersistence.deleteById(id);
    }

    public void uploadResource(UUID id, MultipartFile file){
        try{
            String resource = this.supportResourceService.uploadSupportSessionResource(id.toString(),file);
            if(resource!=null && !resource.isEmpty()){
                SupportSession supportSession = SupportSession.builder()
                        .s3RecordingUrl(resource)
                        .build();
                this.supportSessionPersistence.updateSupportSession(supportSession,id);
            }
        }catch (IOException ex){
            throw new FailUploadResourceException(id.toString());
        }
    }

    public String downloadResource(UUID id){
        SupportSession supportSession = this.supportSessionPersistence.getById(id).toSupportSession();
        if(supportSession.getS3RecordingUrl()!=null){
            return this.supportResourceService.downLoadSupportSessionResource(supportSession.getS3RecordingUrl());
        }else{
            throw new NotFoundException("No resource found for the support session with ID: "+id);
        }
    }

}
