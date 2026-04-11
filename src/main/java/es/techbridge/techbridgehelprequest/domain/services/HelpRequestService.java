package es.techbridge.techbridgehelprequest.domain.services;

import es.techbridge.techbridgehelprequest.domain.model.HelpRequest;
import es.techbridge.techbridgehelprequest.domain.model.UserDto;
import es.techbridge.techbridgehelprequest.domain.persistence.HelpRequestPersistence;
import es.techbridge.techbridgehelprequest.domain.webclients.UserWebClient;
import es.techbridge.techbridgehelprequest.infrastructure.postgresql.entities.HelpRequestEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class HelpRequestService {

    private final HelpRequestPersistence helpRequestPersistence;
    private final UserWebClient userWebClient;

    @Autowired
    public HelpRequestService(HelpRequestPersistence helpRequestPersistence,
                              UserWebClient userWebClient) {
        this.helpRequestPersistence = helpRequestPersistence;
        this.userWebClient = userWebClient;
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
}
