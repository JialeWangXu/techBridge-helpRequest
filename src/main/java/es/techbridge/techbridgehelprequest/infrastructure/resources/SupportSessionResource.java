package es.techbridge.techbridgehelprequest.infrastructure.resources;

import es.techbridge.techbridgehelprequest.domain.model.supportsession.SupportSession;
import es.techbridge.techbridgehelprequest.domain.services.SupportSessionService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@Log4j2
@RequestMapping(SupportSessionResource.SUPPORTSESSION)
public class SupportSessionResource {

    public static final String SUPPORTSESSION = "/supportsession";
    public static final String ID = "/{id}";

    private final SupportSessionService supportSessionService;

    @Autowired
    public SupportSessionResource(SupportSessionService supportSessionService) {
        this.supportSessionService = supportSessionService;
    }

    @PutMapping(ID)
    @PreAuthorize("hasRole('VOLUNTEER')")
    public SupportSession updateSupportSession(@PathVariable UUID id, @RequestBody SupportSession session){
        return this.supportSessionService.saveSessionMethod(session,id);
    }
}
