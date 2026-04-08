package es.techbridge.techbridgehelprequest.infrastructure.resources;

import es.techbridge.techbridgehelprequest.domain.model.HelpRequest;
import es.techbridge.techbridgehelprequest.domain.services.HelpRequestService;
import jakarta.validation.Valid;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Log4j2
@RequestMapping(HelpRequestResource.HELPREQUESTS)
public class HelpRequestResource {

    public static final String HELPREQUESTS = "/helprequests";
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

}
