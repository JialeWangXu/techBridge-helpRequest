package es.techbridge.techbridgehelprequest.domain.services;

import es.techbridge.techbridgehelprequest.domain.model.SupportSession;
import es.techbridge.techbridgehelprequest.domain.persistence.SupportSessionPersistence;
import es.techbridge.techbridgehelprequest.infrastructure.postgresql.entities.HelpStatus;
import es.techbridge.techbridgehelprequest.infrastructure.postgresql.entities.SessionMethods;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class SupportSessionService {

    private final SupportSessionPersistence supportSessionPersistence;

    @Autowired
    public SupportSessionService(SupportSessionPersistence supportSessionPersistence) {
        this.supportSessionPersistence = supportSessionPersistence;
    }

    public SupportSession create(SupportSession supportSession){
        return this.supportSessionPersistence.create(supportSession).toSupportSession();
    }

    public void updateHelpStatusById(UUID id, HelpStatus status){
        this.supportSessionPersistence.updateHelpStatusById(status,id).toSupportSession();
    }

    public SupportSession saveSessionMethod(SupportSession supportSession, UUID id){
        return this.supportSessionPersistence.saveSessionMethod(supportSession,id).toSupportSession();
    }

    public void deleteById(UUID id){
        this.supportSessionPersistence.deleteById(id);
    }

}
