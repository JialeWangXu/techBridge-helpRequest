package es.techbridge.techbridgehelprequest.infrastructure.postgresql.persistence;

import es.techbridge.techbridgehelprequest.domain.exceptions.NotFoundException;
import es.techbridge.techbridgehelprequest.domain.model.HelpRequest;
import es.techbridge.techbridgehelprequest.domain.persistence.HelpRequestPersistence;
import es.techbridge.techbridgehelprequest.infrastructure.postgresql.entities.HelpRequestEntity;
import es.techbridge.techbridgehelprequest.infrastructure.postgresql.entities.RequestStatus;
import es.techbridge.techbridgehelprequest.infrastructure.postgresql.repositories.HelpRequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public class HelpRequestPersistencePostgres implements HelpRequestPersistence {

    private final HelpRequestRepository helpRequestRepository;

    @Autowired
    public HelpRequestPersistencePostgres(HelpRequestRepository helpRequestRepository) {
        this.helpRequestRepository = helpRequestRepository;
    }

    @Override
    public void create(HelpRequest helpRequest) {
        HelpRequestEntity helpRequestEntity = new HelpRequestEntity(helpRequest);
        this.helpRequestRepository.save(helpRequestEntity);
    }

    @Override
    public List<HelpRequestEntity> getHelpRequestsBySeniorId(UUID seniorId) {
        return this.helpRequestRepository.findBySeniorId(seniorId);
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
    public List<HelpRequestEntity> getAllAvailableHelpRequests() {
        // Requests generado con tutorial y solicitada ayuda de voluntario
        List<HelpRequestEntity> availableHelpRequests =
                this.helpRequestRepository.findAllByStatus(RequestStatus.FINDING_VOLUNTEER);
        return availableHelpRequests.stream()
                .filter(helpRequestEntity -> helpRequestEntity.getAiTutorialId()!=null)
                .toList();
    }

}
