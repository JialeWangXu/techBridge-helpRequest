package es.techbridge.techbridgehelprequest.infrastructure.postgresql.persistence;

import es.techbridge.techbridgehelprequest.domain.exceptions.NotFoundException;
import es.techbridge.techbridgehelprequest.domain.model.SupportSession;
import es.techbridge.techbridgehelprequest.domain.persistence.SupportSessionPersistence;
import es.techbridge.techbridgehelprequest.infrastructure.postgresql.entities.HelpStatus;
import es.techbridge.techbridgehelprequest.infrastructure.postgresql.entities.SupportSessionEntity;
import es.techbridge.techbridgehelprequest.infrastructure.postgresql.repositories.SupportSessionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;


@Repository
public class SupportSessionPersistencePostgres implements SupportSessionPersistence {

    private final SupportSessionRepository supportSessionRepository;

    @Autowired
    public SupportSessionPersistencePostgres(SupportSessionRepository supportSessionRepository) {
        this.supportSessionRepository = supportSessionRepository;
    }

    @Override
    public SupportSessionEntity create(SupportSession supportSession) {
        SupportSessionEntity supportSessionEntity = new SupportSessionEntity(supportSession);
        supportSessionEntity.setId(UUID.randomUUID());
        this.supportSessionRepository.save(supportSessionEntity);
        return supportSessionEntity;
    }

    @Override
    public SupportSessionEntity updateHelpStatusById(HelpStatus helpStatus, UUID id) {

        Optional<SupportSessionEntity> supportSession = this.supportSessionRepository.findById(id);
        if(supportSession.isPresent()){
            supportSession.get().setStatus(helpStatus);
            this.supportSessionRepository.save(supportSession.get());
            return supportSession.get();
        }else{
            throw new NotFoundException("No support session found with ID: "+id);
        }
    }

}
