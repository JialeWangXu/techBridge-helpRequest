package es.techbridge.techbridgehelprequest.domain.services;

import es.techbridge.techbridgehelprequest.domain.model.SupportSession;
import es.techbridge.techbridgehelprequest.domain.persistence.SupportSessionPersistence;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SupportSessionService {

    private final SupportSessionPersistence supportSessionPersistence;

    @Autowired
    public SupportSessionService(SupportSessionPersistence supportSessionPersistence) {
        this.supportSessionPersistence = supportSessionPersistence;
    }

    public void create(SupportSession supportSession){
        this.supportSessionPersistence.create(supportSession);
    }
}
