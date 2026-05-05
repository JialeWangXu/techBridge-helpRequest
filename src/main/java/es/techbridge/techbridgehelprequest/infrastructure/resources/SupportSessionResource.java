package es.techbridge.techbridgehelprequest.infrastructure.resources;

import es.techbridge.techbridgehelprequest.domain.model.supportsession.SupportSession;
import es.techbridge.techbridgehelprequest.domain.services.SupportSessionService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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
        return this.supportSessionService.updateSupportSession(session,id);
    }

    @PostMapping(ID)
    @PreAuthorize("hasRole('VOLUNTEER')")
    public void uploadResource(@PathVariable UUID id, @RequestParam("file") MultipartFile file){
        this.supportSessionService.uploadResource(id,file);
    }

    @GetMapping(ID)
    @PreAuthorize("hasAnyRole('VOLUNTEER','SENIOR')")
    public ResponseEntity<String> downloadResource(@PathVariable UUID id){
        try{
            String data = this.supportSessionService.downloadResource(id);
            return ResponseEntity.ok()
                    .body(data);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }
}
