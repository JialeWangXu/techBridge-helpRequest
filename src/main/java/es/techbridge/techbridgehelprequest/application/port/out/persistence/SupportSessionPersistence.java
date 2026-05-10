package es.techbridge.techbridgehelprequest.application.port.out.persistence;

import es.techbridge.techbridgehelprequest.domain.model.supportsession.SupportSession;
import es.techbridge.techbridgehelprequest.infrastructure.postgresql.entities.HelpStatus;
import es.techbridge.techbridgehelprequest.infrastructure.postgresql.entities.SupportSessionEntity;

import java.util.Optional;
import java.util.UUID;

public interface SupportSessionPersistence {

    SupportSessionEntity create(SupportSession supportSession);

    SupportSessionEntity updateHelpStatusById(HelpStatus helpStatus, UUID uuid);

    SupportSessionEntity updateSupportSession(SupportSession session, UUID id);

    SupportSessionEntity getById(UUID id);

    void deleteById(UUID id);
}
