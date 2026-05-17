package es.techbridge.techbridgehelprequest.application.port.out.persistence;

import es.techbridge.techbridgehelprequest.domain.model.helprequest.HelpRequest;
import es.techbridge.techbridgehelprequest.infrastructure.postgresql.entities.HelpRequestEntity;
import es.techbridge.techbridgehelprequest.infrastructure.postgresql.entities.HelpStatus;
import es.techbridge.techbridgehelprequest.infrastructure.postgresql.entities.RequestStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface HelpRequestPersistence {

    HelpRequestEntity create(HelpRequest helpRequest);

    Page<HelpRequestEntity> getSeniorFilteredHelpRequests(UUID seniorId, RequestStatus status, String category, Pageable pageable);

    HelpRequestEntity getById(UUID id);

    void deleteById(UUID id);

    Page<HelpRequestEntity> getAllAvailableHelpRequests(Pageable pageable,
                                                        String searchText,
                                                        List<UUID> seniors);

    HelpRequestEntity updateRequestStatusById(UUID id, RequestStatus requestStatus, UUID volunteerId);
    Page<HelpRequestEntity> getVolunteerFilteredHelpRequests(UUID volunteerId, HelpStatus status, Pageable pageable);

    void saveAiTutorialId(UUID id, UUID aiTutorialId);

    Long countVolunteerInProgressRequest(UUID id);
}
