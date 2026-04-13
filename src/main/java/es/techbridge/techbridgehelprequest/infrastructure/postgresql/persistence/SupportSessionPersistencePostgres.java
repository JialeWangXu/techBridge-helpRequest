package es.techbridge.techbridgehelprequest.infrastructure.postgresql.persistence;

import es.techbridge.techbridgehelprequest.domain.model.SupportSession;
import es.techbridge.techbridgehelprequest.domain.persistence.SupportSessionPersistence;
import es.techbridge.techbridgehelprequest.infrastructure.postgresql.entities.SupportSessionEntity;
import es.techbridge.techbridgehelprequest.infrastructure.postgresql.repositories.SupportSessionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.UUID;


@Repository
public class SupportSessionPersistencePostgres implements SupportSessionPersistence {

    private final SupportSessionRepository supportSessionRepository;

    @Autowired
    public SupportSessionPersistencePostgres(SupportSessionRepository supportSessionRepository) {
        this.supportSessionRepository = supportSessionRepository;
    }

    @Override
    public void create(SupportSession supportSession) {
        SupportSessionEntity supportSessionEntity = new SupportSessionEntity(supportSession);
        supportSessionEntity.setId(UUID.randomUUID());
        this.supportSessionRepository.save(supportSessionEntity);
    }

}
