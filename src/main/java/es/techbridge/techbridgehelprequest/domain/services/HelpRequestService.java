package es.techbridge.techbridgehelprequest.domain.services;

import es.techbridge.techbridgehelprequest.application.port.in.HelpRequestUseCases;
import es.techbridge.techbridgehelprequest.domain.exceptions.FailedCreateAiTutorialException;
import es.techbridge.techbridgehelprequest.domain.model.aitutorial.AiTutorialDto;
import es.techbridge.techbridgehelprequest.domain.model.aitutorial.CreateAiTutorialDto;
import es.techbridge.techbridgehelprequest.domain.model.helprequest.HelpRequest;
import es.techbridge.techbridgehelprequest.domain.model.supportsession.SupportSession;
import es.techbridge.techbridgehelprequest.domain.model.user.UserDto;
import es.techbridge.techbridgehelprequest.application.port.out.persistence.HelpRequestPersistence;
import es.techbridge.techbridgehelprequest.application.port.out.webclients.AiTutorialWebClient;
import es.techbridge.techbridgehelprequest.application.port.out.webclients.UserWebClient;
import es.techbridge.techbridgehelprequest.infrastructure.postgresql.entities.HelpRequestEntity;
import es.techbridge.techbridgehelprequest.infrastructure.postgresql.entities.HelpStatus;
import es.techbridge.techbridgehelprequest.infrastructure.postgresql.entities.RequestStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.UUID;


@Service
public class HelpRequestService implements HelpRequestUseCases {

    private final HelpRequestPersistence helpRequestPersistence;
    private final UserWebClient userWebClient;
    private final SupportSessionService supportSessionService;
    private final AiTutorialWebClient aiTutorialWebClient;

    @Value("${app.request-limit.volunteer-in-progress-limit}")
    private Long volunteerInProgressRequestLimit;

    @Autowired
    public HelpRequestService(HelpRequestPersistence helpRequestPersistence,
                              UserWebClient userWebClient, SupportSessionService supportSessionService, AiTutorialWebClient aiTutorialWebClient) {
        this.helpRequestPersistence = helpRequestPersistence;
        this.userWebClient = userWebClient;
        this.supportSessionService = supportSessionService;
        this.aiTutorialWebClient = aiTutorialWebClient;
    }

    @Override
    public HelpRequest create(String email, HelpRequest helpRequest){
        // Buscar senior
        UserDto senior =this.userWebClient.readByEmail(email);
        helpRequest.setSenior(senior);
        helpRequest.setId(UUID.randomUUID());
        HelpRequestEntity helpRequestEntity= this.helpRequestPersistence.create(helpRequest);
        AiTutorialDto aiTutorial;
        try{
            aiTutorial = this.aiTutorialWebClient.create(
                    CreateAiTutorialDto.builder()
                            .title(helpRequest.getTitle())
                            .description(helpRequest.getDescription())
                            .helpRequestId(helpRequestEntity.getId())
                            .build()
            );
        } catch (Exception e) {
            this.helpRequestPersistence.deleteById(helpRequestEntity.getId());
            throw new FailedCreateAiTutorialException("Please revise request content");
        }
        this.helpRequestPersistence.saveAiTutorialId(helpRequestEntity.getId(),aiTutorial.getId());
        helpRequestEntity.setAiTutorialId(aiTutorial.getId());
        return helpRequestEntity.toHelpRequest();
    }

    @Override
    public List<HelpRequest> getSeniorHelpRequestsByEmail(String email){

        UserDto senior = this.userWebClient.readByEmail(email);

        return this.helpRequestPersistence.getHelpRequestsBySeniorId(senior.getId())
                .stream()
                .map(HelpRequestEntity::toHelpRequest)
                .peek(r -> r.setSenior(senior))
                .toList();
    }

    @Override
    public HelpRequest getById(UUID id){
        HelpRequest helpRequest = this.helpRequestPersistence.getById(id).toHelpRequest();
        if(helpRequest.getVolunteer()!=null&&helpRequest.getVolunteer().getId()!=null){
            UserDto volunteer = this.userWebClient.readById(helpRequest.getVolunteer().getId());
            helpRequest.setVolunteer(volunteer);
        }else{
            helpRequest.setVolunteer(null);
        }
        if(helpRequest.getAiTutorial()!=null && helpRequest.getAiTutorial().getId()!=null){
            AiTutorialDto aiTutorialDto = this.aiTutorialWebClient.getById(helpRequest.getAiTutorial().getId());
            helpRequest.setAiTutorial(aiTutorialDto);
        }
        UserDto senior = this.userWebClient.readById(helpRequest.getSenior().getId());
        helpRequest.setSenior(senior);
        return helpRequest;
    }
    @Override
    public void deleteById(UUID id){
        this.helpRequestPersistence.deleteById(id);
    }

    @Override
    public List<HelpRequest> getAllAvailableHelpRequests(){
        return this.helpRequestPersistence.getAllAvailableHelpRequests()
                .stream()
                .map(HelpRequestEntity::toHelpRequest)
                .peek(helpRequest ->
                    helpRequest.setSenior(this.userWebClient.readById(helpRequest.getSenior().getId()))
                )
                .toList();

    }

    @Override
    public HelpRequest updateRequestStatusById(String volunteerEmail, UUID id, RequestStatus status){
        HelpRequest helpRequest = this.helpRequestPersistence.getById(id).toHelpRequest();
        UUID volunteerId = null;

        // si el voluntario acceptado la peticion, se crea un sessionSupport
        if(status == RequestStatus.IN_PROGRESS){
            UserDto volunteer = this.userWebClient.readByEmail(volunteerEmail);
            volunteerId = volunteer.getId();
            helpRequest.setVolunteer(volunteer);
            SupportSession supportSession = SupportSession.builder()
                    .status(HelpStatus.ACTIVE)
                    .helpRequest(helpRequest)
                    .build();
            helpRequest.setSupportSession(this.supportSessionService.create(supportSession));
        }else if (status == RequestStatus.CANCELLED && helpRequest.getSupportSession()!=null){
            this.supportSessionService.updateHelpStatusById(helpRequest.getSupportSession().getId(),HelpStatus.CANCELLED);
        }else if (status == RequestStatus.COMPLETED && helpRequest.getSupportSession()!=null){
            this.supportSessionService.updateHelpStatusById(helpRequest.getSupportSession().getId(),HelpStatus.FINISHED);
        }

        HelpRequest updatedHelpRequest = this.helpRequestPersistence.updateRequestStatusById(id, status, volunteerId).toHelpRequest();
        updatedHelpRequest.setSenior(this.userWebClient.readById(updatedHelpRequest.getSenior().getId()));
        if (status == RequestStatus.IN_PROGRESS && updatedHelpRequest.getSupportSession() == null) {
            updatedHelpRequest.setSupportSession(helpRequest.getSupportSession());
        }
        if(updatedHelpRequest.getVolunteer().getId()!=null){
            UserDto volunteer = this.userWebClient.readById(updatedHelpRequest.getVolunteer().getId());
            updatedHelpRequest.setVolunteer(volunteer);
        }
        if(updatedHelpRequest.getAiTutorial().getId()!=null){
            AiTutorialDto aiTutorialDto = this.aiTutorialWebClient.getById(updatedHelpRequest.getAiTutorial().getId());
            updatedHelpRequest.setAiTutorial(aiTutorialDto);
        }

        return updatedHelpRequest;
    }

    @Override
    public List<HelpRequest> getVolunteerHelpRequestsByEmail(String email){
        UserDto volunteer = this.userWebClient.readByEmail(email);
        return this.helpRequestPersistence.getHelpRequestsByVolunteerId(volunteer.getId())
                .stream()
                .map(HelpRequestEntity::toHelpRequest)
                .peek(helpRequest -> {
                    UserDto senior = this.userWebClient.readById(helpRequest.getSenior().getId());
                    helpRequest.setSenior(senior);
                })
                .toList();
    }

    @Override
    public void saveAiTutorialId(UUID id, UUID aiTutorialId){
        this.helpRequestPersistence.saveAiTutorialId(id,aiTutorialId);
    }

    @Override
    public boolean checkVolunteerCurrentProgress(String email) {
        return getVolunteerCurrentProgress(email) >= volunteerInProgressRequestLimit;
    }

    @Override
    public long getVolunteerCurrentProgress(String email) {
        UserDto volunteer = this.userWebClient.readByEmail(email);
        return this.helpRequestPersistence.countVolunteerInProgressRequest(volunteer.getId());
    }
}
