package es.techbridge.techbridgehelprequest.application.port.in;

import es.techbridge.techbridgehelprequest.domain.model.supportsession.SupportSession;
import es.techbridge.techbridgehelprequest.infrastructure.postgresql.entities.HelpStatus;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

public interface SupportSessionUseCases {
    SupportSession create(SupportSession supportSession);
    void updateHelpStatusById(UUID id, HelpStatus status);
    SupportSession updateSupportSession(SupportSession supportSession, UUID id);
    void deleteById(UUID id);
    void uploadResource(UUID id, MultipartFile file);
    String downloadResource(UUID id);
}
