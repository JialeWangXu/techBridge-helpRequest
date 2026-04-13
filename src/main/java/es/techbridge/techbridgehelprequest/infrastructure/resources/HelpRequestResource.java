package es.techbridge.techbridgehelprequest.infrastructure.resources;

import es.techbridge.techbridgehelprequest.domain.model.HelpRequest;
import es.techbridge.techbridgehelprequest.domain.model.RequestStatusDto;
import es.techbridge.techbridgehelprequest.domain.services.HelpRequestService;
import es.techbridge.techbridgehelprequest.infrastructure.postgresql.entities.RequestStatus;
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
    public static final String AVAILABLE = "/available";
    private final HelpRequestService helpRequestService;

    @Autowired
    public HelpRequestResource(HelpRequestService helpRequestService) {
        this.helpRequestService = helpRequestService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SENIOR')")
    public void create(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody HelpRequest helpRequest){
        String email = jwt.getSubject();
        this.helpRequestService.create(email,helpRequest);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('SENIOR')")
    public List<HelpRequest> getHelpRequestsByEmail(@AuthenticationPrincipal Jwt jwt){
        String email = jwt.getSubject();
        return this.helpRequestService.getHelpRequestsByEmail(email);
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
    public HelpRequest updateRequestStatusById(@RequestBody RequestStatusDto status, @PathVariable UUID id){
        return this.helpRequestService.updateRequestStatusById(id,status.getStatus());
    }

}
