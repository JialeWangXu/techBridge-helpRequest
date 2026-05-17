package es.techbridge.techbridgehelprequest.infrastructure.resources;

import es.techbridge.techbridgehelprequest.domain.model.helprequest.HelpRequest;
import es.techbridge.techbridgehelprequest.domain.model.helprequest.RequestStatusDto;
import es.techbridge.techbridgehelprequest.application.services.HelpRequestService;
import es.techbridge.techbridgehelprequest.domain.model.user.ContactPreference;
import es.techbridge.techbridgehelprequest.domain.model.user.Province;
import es.techbridge.techbridgehelprequest.infrastructure.postgresql.entities.HelpStatus;
import es.techbridge.techbridgehelprequest.infrastructure.postgresql.entities.RequestStatus;
import jakarta.validation.Valid;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

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
    public Page<HelpRequest> getSeniorFilteredHelpRequests(@AuthenticationPrincipal Jwt jwt,
                                                           @RequestParam RequestStatus status,
                                                           @RequestParam String category,
                                                           @PageableDefault(page = 1, size = 3) Pageable pageable){
        String email = jwt.getSubject();
        return this.helpRequestService.getSeniorFilteredHelpRequests(email,status,category,pageable);
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
    public Page<HelpRequest> getAllAvailableHelpRequests(@PageableDefault(page = 1, size = 3)
                                                             Pageable pageable,
                                                         @RequestParam(required = false)ContactPreference contactPreference,
                                                         @RequestParam(required = false)Province province,
                                                         @RequestParam(required = false)String city,
                                                         @RequestParam(required = false) String search
                                                         ){
        return this.helpRequestService.getAllAvailableHelpRequests(pageable,contactPreference,province,city,search);
    }

    @PutMapping(ID)
    @PreAuthorize("hasAnyRole('SENIOR','VOLUNTEER')")
    public HelpRequest updateRequestStatusById(@AuthenticationPrincipal Jwt jtw, @RequestBody RequestStatusDto status, @PathVariable UUID id){
        String email = jtw.getSubject();
        return this.helpRequestService.updateRequestStatusById(email,id,status.getStatus());
    }

    @GetMapping(VOLUNTEER_MY)
    @PreAuthorize("hasAnyRole('VOLUNTEER')")
    public Page<HelpRequest> getAllVolunteersHelpRequestsByEmail(@AuthenticationPrincipal Jwt jwt,
                                                                 @RequestParam HelpStatus status,
                                                                 @PageableDefault(page = 1, size = 3)
                                                                     Pageable pageable){
        String email = jwt.getSubject();
        return this.helpRequestService.getVolunteerFilteredHelpRequestsByEmail(email,status,pageable);
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
