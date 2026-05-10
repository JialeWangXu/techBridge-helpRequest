package es.techbridge.techbridgehelprequest.application.port.in;

import es.techbridge.techbridgehelprequest.domain.model.helprequest.HelpRequest;
import es.techbridge.techbridgehelprequest.infrastructure.postgresql.entities.RequestStatus;

import java.util.List;
import java.util.UUID;

public interface HelpRequestUseCases {
    HelpRequest create(String email, HelpRequest helpRequest);
    List<HelpRequest> getSeniorHelpRequestsByEmail(String email);
    HelpRequest getById(UUID id);
    void deleteById(UUID id);
    HelpRequest updateRequestStatusById(String volunteerEmail, UUID id, RequestStatus status);
    List<HelpRequest> getVolunteerHelpRequestsByEmail(String email);
    void saveAiTutorialId(UUID id, UUID aiTutorialId);
    List<HelpRequest> getAllAvailableHelpRequests();
    boolean checkVolunteerCurrentProgress(String email);
    long getVolunteerCurrentProgress(String email);
}
