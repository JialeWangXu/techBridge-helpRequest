package es.techbridge.techbridgehelprequest.application.port.in;

import es.techbridge.techbridgehelprequest.domain.model.helprequest.HelpRequest;
import es.techbridge.techbridgehelprequest.domain.model.user.ContactPreference;
import es.techbridge.techbridgehelprequest.domain.model.user.Province;
import es.techbridge.techbridgehelprequest.infrastructure.postgresql.entities.HelpStatus;
import es.techbridge.techbridgehelprequest.infrastructure.postgresql.entities.RequestStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface HelpRequestUseCases {
    HelpRequest create(String email, HelpRequest helpRequest);
    Page<HelpRequest> getSeniorFilteredHelpRequests(String email, RequestStatus status, String category, Pageable pageable);
    HelpRequest getById(UUID id);
    void deleteById(UUID id);
    HelpRequest updateRequestStatusById(String volunteerEmail, UUID id, RequestStatus status);
    Page<HelpRequest> getVolunteerFilteredHelpRequestsByEmail(String email, HelpStatus status, Pageable pageable);
    void saveAiTutorialId(UUID id, UUID aiTutorialId);
    Page<HelpRequest> getAllAvailableHelpRequests(Pageable pageable,
                                                  ContactPreference contactPreference,
                                                  Province province,
                                                  String city,
                                                  String search);
    boolean checkVolunteerCurrentProgress(String email);
    long getVolunteerCurrentProgress(String email);
}
