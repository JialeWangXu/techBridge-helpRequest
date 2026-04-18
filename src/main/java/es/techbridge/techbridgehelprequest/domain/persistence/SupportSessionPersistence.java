package es.techbridge.techbridgehelprequest.domain.persistence;

import es.techbridge.techbridgehelprequest.domain.model.SupportSession;
import es.techbridge.techbridgehelprequest.infrastructure.postgresql.entities.HelpStatus;
import es.techbridge.techbridgehelprequest.infrastructure.postgresql.entities.SupportSessionEntity;

import java.util.UUID;

public interface SupportSessionPersistence {

    SupportSessionEntity create(SupportSession supportSession);

    SupportSessionEntity updateHelpStatusById(HelpStatus helpStatus, UUID uuid);

    SupportSessionEntity saveSessionMethod(SupportSession session, UUID id);

    void deleteById(UUID id);
}
