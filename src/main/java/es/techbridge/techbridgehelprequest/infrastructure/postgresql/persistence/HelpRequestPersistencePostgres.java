package es.techbridge.techbridgehelprequest.infrastructure.postgresql.persistence;

import es.techbridge.techbridgehelprequest.domain.exceptions.NotFoundException;
import es.techbridge.techbridgehelprequest.domain.model.helprequest.HelpRequest;
import es.techbridge.techbridgehelprequest.application.port.out.persistence.HelpRequestPersistence;
import es.techbridge.techbridgehelprequest.infrastructure.postgresql.entities.HelpRequestEntity;
import es.techbridge.techbridgehelprequest.infrastructure.postgresql.entities.HelpStatus;
import es.techbridge.techbridgehelprequest.infrastructure.postgresql.entities.RequestStatus;
import es.techbridge.techbridgehelprequest.infrastructure.postgresql.repositories.HelpRequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class HelpRequestPersistencePostgres implements HelpRequestPersistence {

    private final HelpRequestRepository helpRequestRepository;
    private static final String SENIOR = "senior";

    @Autowired
    public HelpRequestPersistencePostgres(HelpRequestRepository helpRequestRepository) {
        this.helpRequestRepository = helpRequestRepository;
    }

    @Override
    public HelpRequestEntity create(HelpRequest helpRequest) {
        HelpRequestEntity helpRequestEntity = new HelpRequestEntity(helpRequest);
        this.helpRequestRepository.save(helpRequestEntity);
        return helpRequestEntity;
    }

    @Override
    public Page<HelpRequestEntity> getSeniorFilteredHelpRequests(UUID seniorId, RequestStatus status, String category, Pageable pageable) {
        if(category.equals("VOLUNTEER")){
            return this.helpRequestRepository.findAllBySeniorIdAndStatusAndVolunteerIdIsNotNull(seniorId,status,pageable);
        }else if (category.equals("AI_ONLY")){
            return this.helpRequestRepository.findAllBySeniorIdAndStatusAndVolunteerIdIsNull(seniorId, status, pageable);
        }else{
            return this.helpRequestRepository.findAllBySeniorIdAndStatus(seniorId, status, pageable);
        }
    }

    @Override
    public HelpRequestEntity getById(UUID id) {
        return this.helpRequestRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("No Help request found with the ID: "+id));
    }

    @Override
    public void deleteById(UUID id) {

        if(!helpRequestRepository.existsById(id)){
            throw  new NotFoundException("No Help request found with the ID: "+id);
        }
        this.helpRequestRepository.deleteById(id);
    }

    @Override
    public Page<HelpRequestEntity> getAllAvailableHelpRequests(Pageable pageable,
                                                               String searchText,
                                                               List<UUID> seniors) {
        String search = (searchText != null && !searchText.trim().isEmpty()) ? searchText.trim().toLowerCase(): null;
        List<UUID> seniorIds = (seniors != null && !seniors.isEmpty()) ? seniors : null;
        if (search == null && seniorIds == null) {
            return this.helpRequestRepository.findAllByStatusAndVolunteerIdIsNullAndAiTutorialIdIsNotNull(
                    RequestStatus.FINDING_VOLUNTEER,
                    pageable
            );
        }
        if (search == null) {
            return this.helpRequestRepository.findAllByStatusAndVolunteerIdIsNullAndAiTutorialIdIsNotNullAndSeniorIdIsIn(
                    RequestStatus.FINDING_VOLUNTEER,
                    seniorIds,
                    pageable
            );
        }
        if (seniorIds == null) {
            return this.helpRequestRepository.findAvailableRequestsWithFilters(search, pageable);
        }
        return this.helpRequestRepository.findAvailableRequestsWithFiltersBySeniorIds(search,seniorIds,pageable);
    }

    @Override
    public HelpRequestEntity updateRequestStatusById(UUID id, RequestStatus requestStatus, UUID volunteerId) {
        Optional<HelpRequestEntity> request = this.helpRequestRepository.findById(id);
        if(request.isEmpty()){
            throw new NotFoundException("No Help request found with the ID: "+id);
        }
        if (volunteerId!=null){
            request.get().setVolunteerId(volunteerId);
        }
        request.get().setStatus(requestStatus);
        if (requestStatus == RequestStatus.FINDING_VOLUNTEER){
            request.get().setVolunteerId(null);
            if(request.get().getSupportSession()!=null){
                request.get().setSupportSession(null);
            }

        }
        this.helpRequestRepository.save(request.get());
        return request.get();
    }

    @Override
    public Page<HelpRequestEntity> getVolunteerFilteredHelpRequests(UUID volunteerId, HelpStatus status, Pageable pageable) {
        return this.helpRequestRepository
                .findAllByVolunteerIdAndSupportSession_Status(volunteerId,status,pageable);
    }

    @Override
    public void saveAiTutorialId(UUID id, UUID aiTutorialId) {
        HelpRequestEntity helpRequestEntity = this.helpRequestRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("No Help request found with the ID: "+id));
        helpRequestEntity.setAiTutorialId(aiTutorialId);
        this.helpRequestRepository.save(helpRequestEntity);
    }

    @Override
    public Long countVolunteerInProgressRequest(UUID id) {
        return this.helpRequestRepository.countByVolunteerIdAndStatus(id,RequestStatus.IN_PROGRESS);
    }

}
