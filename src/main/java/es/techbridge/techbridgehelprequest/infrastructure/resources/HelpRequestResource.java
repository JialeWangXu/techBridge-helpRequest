package es.techbridge.techbridgehelprequest.infrastructure.resources;

import es.techbridge.techbridgehelprequest.domain.model.helprequest.HelpRequest;
import es.techbridge.techbridgehelprequest.domain.model.helprequest.RequestStatusDto;
import es.techbridge.techbridgehelprequest.domain.services.HelpRequestService;
import jakarta.validation.Valid;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@Log4j2
@RequestMapping(HelpRequestResource.HELPREQUESTS)
public class HelpRequestResource {

    public static final String HELPREQUESTS = "/helprequests";
    public static final String ID = "/{id}";
    public static final String SENIOR_MY = "/senior/my";
    public static final String VOLUNTEER_MY = "/volunteer/my";
    public static final String AVAILABLE = "/available";
    public static final String SAVE_AI_TUTORIAL_ID = "/saveAiTutorial/{id}";
    public static final String VOLUNTEER_CHECK="/inProgress/check";
    public static final String VOLUNTEER_IN_PROGRESS_COUNT="/inProgress/count";
    private final HelpRequestService helpRequestService;

    @Autowired
    public HelpRequestResource(HelpRequestService helpRequestService) {
        this.helpRequestService = helpRequestService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SENIOR')")
    public HelpRequest create(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody HelpRequest helpRequest){
        String email = jwt.getSubject();
        return this.helpRequestService.create(email,helpRequest);
    }

    @GetMapping(SENIOR_MY)
    @PreAuthorize("hasAnyRole('SENIOR')")
    public List<HelpRequest> getHelpRequestsByEmail(@AuthenticationPrincipal Jwt jwt){
        String email = jwt.getSubject();
        return this.helpRequestService.getSeniorHelpRequestsByEmail(email);
    }

    @GetMapping(ID)
    @PreAuthorize("hasAnyRole('SENIOR','VOLUNTEER')")
    public HelpRequest getById(@PathVariable UUID id){
        return this.helpRequestService.getById(id);
    }

    @DeleteMapping(ID)
    @PreAuthorize("hasAnyRole('SENIOR')")
    public void deleteById(@PathVariable UUID id){
        this.helpRequestService.deleteById(id);
    }

    @GetMapping(AVAILABLE)
    @PreAuthorize("hasAnyRole('VOLUNTEER')")
    public List<HelpRequest> getAllAvailableHelpRequests(){
        return this.helpRequestService.getAllAvailableHelpRequests();
    }

    @PutMapping(ID)
    @PreAuthorize("hasAnyRole('SENIOR','VOLUNTEER')")
    public HelpRequest updateRequestStatusById(@AuthenticationPrincipal Jwt jtw, @RequestBody RequestStatusDto status, @PathVariable UUID id){
        String email = jtw.getSubject();
        return this.helpRequestService.updateRequestStatusById(email,id,status.getStatus());
    }

    @GetMapping(VOLUNTEER_MY)
    @PreAuthorize("hasAnyRole('VOLUNTEER')")
    public List<HelpRequest> getAllVolunteersHelpRequestsByEmail(@AuthenticationPrincipal Jwt jwt){
        String email = jwt.getSubject();
        return this.helpRequestService.getVolunteerHelpRequestsByEmail(email);
    }

    @PutMapping(SAVE_AI_TUTORIAL_ID)
    @PreAuthorize("hasRole('SENIOR')")
    public void saveAiTutorialId(@PathVariable UUID id, @RequestBody UUID aiTutorialId){
        this.helpRequestService.saveAiTutorialId(id,aiTutorialId);
    }

    @GetMapping(VOLUNTEER_CHECK)
    @PreAuthorize("hasRole('VOLUNTEER')")
    public boolean checkVolunteerInProgressLimit(@AuthenticationPrincipal Jwt jwt){
        return this.helpRequestService.checkVolunteerCurrentProgress(jwt.getSubject());
    }


    @GetMapping(VOLUNTEER_IN_PROGRESS_COUNT)
    @PreAuthorize("hasRole('VOLUNTEER')")
    public Long getVolunteerInProgressCount(@AuthenticationPrincipal Jwt jwt){
        return this.helpRequestService.getVolunteerCurrentProgress(jwt.getSubject());
    }
}
