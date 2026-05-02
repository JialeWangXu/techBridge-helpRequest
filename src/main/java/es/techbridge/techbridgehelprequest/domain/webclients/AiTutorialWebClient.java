package es.techbridge.techbridgehelprequest.domain.webclients;

import es.techbridge.techbridgehelprequest.configurations.FeignConfig;
import es.techbridge.techbridgehelprequest.domain.model.aitutorial.AiTutorialDto;
import es.techbridge.techbridgehelprequest.domain.model.aitutorial.CreateAiTutorialDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.UUID;

@FeignClient(name = AiTutorialWebClient.TECHBRIDGE_AI_TUTORIAL, configuration = FeignConfig.class)
public interface AiTutorialWebClient {

    String TECHBRIDGE_AI_TUTORIAL = "techbridge-aitutorial";
    String AITUTORIALS = "/aitutorial";
    String ID = "/{id}";

    @GetMapping(AITUTORIALS+ID)
    AiTutorialDto getById(@PathVariable UUID id);

    @PostMapping(AITUTORIALS)
    @PreAuthorize("hasRole('SENIOR')")
    AiTutorialDto create(@RequestBody CreateAiTutorialDto createAiTutorialDto);
}
