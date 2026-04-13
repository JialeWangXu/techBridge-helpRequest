package es.techbridge.techbridgehelprequest.domain.services;

import es.techbridge.techbridgehelprequest.domain.model.HelpRequest;
import es.techbridge.techbridgehelprequest.domain.model.SupportSession;
import es.techbridge.techbridgehelprequest.domain.model.UserDto;
import es.techbridge.techbridgehelprequest.domain.persistence.HelpRequestPersistence;
import es.techbridge.techbridgehelprequest.domain.webclients.UserWebClient;
import es.techbridge.techbridgehelprequest.infrastructure.postgresql.entities.HelpRequestEntity;
import es.techbridge.techbridgehelprequest.infrastructure.postgresql.entities.HelpStatus;
import es.techbridge.techbridgehelprequest.infrastructure.postgresql.entities.RequestStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.UUID;


@Service
public class HelpRequestService {

    private final HelpRequestPersistence helpRequestPersistence;
    private final UserWebClient userWebClient;
    private final SupportSessionService supportSessionService;

    @Autowired
    public HelpRequestService(HelpRequestPersistence helpRequestPersistence,
                              UserWebClient userWebClient, SupportSessionService supportSessionService) {
        this.helpRequestPersistence = helpRequestPersistence;
        this.userWebClient = userWebClient;
        this.supportSessionService = supportSessionService;
    }

    public void create(String email, HelpRequest helpRequest){
        // Buscar senior
        UserDto senior =this.userWebClient.readByEmail(email);
        helpRequest.setSenior(senior);
        helpRequest.setId(UUID.randomUUID());
        this.helpRequestPersistence.create(helpRequest);
    }

    public List<HelpRequest> getHelpRequestsByEmail(String email){

        UserDto senior = this.userWebClient.readByEmail(email);

        return this.helpRequestPersistence.getHelpRequestsBySeniorId(senior.getId())
                .stream()
                .map(HelpRequestEntity::toHelpRequest)
                .peek(r -> r.setSenior(senior))
                .toList();
    }

    public HelpRequest getById(UUID id){
        HelpRequest helpRequest = this.helpRequestPersistence.getById(id).toHelpRequest();
        if(helpRequest.getVolunteer().getId()!=null){
            UserDto volunteer = this.userWebClient.readById(helpRequest.getVolunteer().getId());
            helpRequest.setVolunteer(volunteer);
        }else{
            helpRequest.setVolunteer(null);
        }
        UserDto senior = this.userWebClient.readById(helpRequest.getSenior().getId());
        helpRequest.setSenior(senior);
        return helpRequest;
    }

    public void deleteById(UUID id){
        this.helpRequestPersistence.deleteById(id);
    }

    public List<HelpRequest> getAllAvailableHelpRequests(){
        return this.helpRequestPersistence.getAllAvailableHelpRequests()
                .stream()
                .map(HelpRequestEntity::toHelpRequest)
                .peek(helpRequest ->
                    helpRequest.setSenior(this.userWebClient.readById(helpRequest.getSenior().getId()))
                )
                .toList();

    }

    public HelpRequest updateRequestStatusById(UUID id, RequestStatus status){
        HelpRequest helpRequest = this.helpRequestPersistence.getById(id).toHelpRequest();

        // si el voluntario acceptado la peticion, se crea un sessionSupport
        if(status == RequestStatus.IN_PROGRESS){
            SupportSession supportSession = SupportSession.builder()
                    .status(HelpStatus.ACTIVE)
                    .helpRequest(helpRequest)
                    .build();
            this.supportSessionService.create(supportSession);
        }

        return this.helpRequestPersistence.updateRequestStatusById(id,status).toHelpRequest();
    }
}
