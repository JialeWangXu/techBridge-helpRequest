package es.techbridge.techbridgehelprequest.application.services;

import es.techbridge.techbridgehelprequest.application.port.in.HelpRequestUseCases;
import es.techbridge.techbridgehelprequest.domain.exceptions.FailedCreateAiTutorialException;
import es.techbridge.techbridgehelprequest.domain.model.aitutorial.AiTutorialDto;
import es.techbridge.techbridgehelprequest.domain.model.aitutorial.CreateAiTutorialDto;
import es.techbridge.techbridgehelprequest.domain.model.helprequest.HelpRequest;
import es.techbridge.techbridgehelprequest.domain.model.supportsession.SupportSession;
import es.techbridge.techbridgehelprequest.domain.model.user.ContactPreference;
import es.techbridge.techbridgehelprequest.domain.model.user.Province;
import es.techbridge.techbridgehelprequest.domain.model.user.UserDto;
import es.techbridge.techbridgehelprequest.application.port.out.persistence.HelpRequestPersistence;
import es.techbridge.techbridgehelprequest.application.port.out.webclients.AiTutorialWebClient;
import es.techbridge.techbridgehelprequest.application.port.out.webclients.UserWebClient;
import es.techbridge.techbridgehelprequest.domain.model.user.UserFiltersDto;
import es.techbridge.techbridgehelprequest.infrastructure.postgresql.entities.HelpRequestEntity;
import es.techbridge.techbridgehelprequest.infrastructure.postgresql.entities.HelpStatus;
import es.techbridge.techbridgehelprequest.infrastructure.postgresql.entities.RequestStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    public Page<HelpRequest> getSeniorFilteredHelpRequests(String email, RequestStatus status, String category, Pageable pageable){

        UserDto senior = this.userWebClient.readByEmail(email);
        return this.helpRequestPersistence
                .getSeniorFilteredHelpRequests(senior.getId(), status, category, pageable)
                .map(entity -> {
                    HelpRequest request = entity.toHelpRequest();
                    request.setSenior(senior);
                    return request;
                });
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
    public Page<HelpRequest> getAllAvailableHelpRequests(Pageable pageable,
                                                         ContactPreference contactPreference,
                                                         Province province,
                                                         String city,
                                                         String search){

        List<UUID> seniorIds = null;
        if(contactPreference!=null || province!=null || (city!=null && !city.isEmpty())){
            seniorIds = this.userWebClient
                    .getFilteredUserIds(new UserFiltersDto(contactPreference,province,city));
            if (seniorIds==null || seniorIds.isEmpty())return null;
        }

        if(search==null || search.isEmpty()) search=null;

        return this.helpRequestPersistence.getAllAvailableHelpRequests(pageable,search,seniorIds)
                .map(helpRequestEntity -> {
                    UserDto senior = this.userWebClient.readById(helpRequestEntity.getSeniorId());
                    HelpRequest helpRequest = helpRequestEntity.toHelpRequest();
                    helpRequest.setSenior(senior);
                    return helpRequest;
                });
    }

    @Override
    public HelpRequest updateRequestStatusById(String email, UUID id, RequestStatus status){
        HelpRequest helpRequest = this.helpRequestPersistence.getById(id).toHelpRequest();
        UUID volunteerId = null;

        // si el voluntario acceptado la peticion, se crea un sessionSupport
        if(status == RequestStatus.IN_PROGRESS){
            UserDto volunteer = this.userWebClient.readByEmail(email);
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
        if(updatedHelpRequest.getVolunteer()!=null){
            UserDto volunteer = this.userWebClient.readById(updatedHelpRequest.getVolunteer().getId());
            updatedHelpRequest.setVolunteer(volunteer);
        }
        if(updatedHelpRequest.getAiTutorial()!=null){
            AiTutorialDto aiTutorialDto = this.aiTutorialWebClient.getById(updatedHelpRequest.getAiTutorial().getId());
            updatedHelpRequest.setAiTutorial(aiTutorialDto);
        }

        return updatedHelpRequest;
    }

    @Override
    public Page<HelpRequest> getVolunteerFilteredHelpRequestsByEmail(String email,HelpStatus status, Pageable pageable){
        UserDto volunteer = this.userWebClient.readByEmail(email);
        return this.helpRequestPersistence.getVolunteerFilteredHelpRequests(volunteer.getId(),status,pageable)
                .map(helpRequestEntity -> {
                    UserDto senior = this.userWebClient.readById(helpRequestEntity.getSeniorId());
                    HelpRequest helpRequest = helpRequestEntity.toHelpRequest();
                    helpRequest.setSenior(senior);
                    return helpRequest;
                });
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
