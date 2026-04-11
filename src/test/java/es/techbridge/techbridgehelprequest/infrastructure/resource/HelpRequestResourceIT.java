package es.techbridge.techbridgehelprequest.infrastructure.resource;

import es.techbridge.techbridgehelprequest.domain.model.HelpRequest;
import es.techbridge.techbridgehelprequest.domain.model.UserDto;
import es.techbridge.techbridgehelprequest.domain.services.HelpRequestService;
import es.techbridge.techbridgehelprequest.domain.webclients.UserWebClient;
import es.techbridge.techbridgehelprequest.infrastructure.postgresql.entities.RequestStatus;
import es.techbridge.techbridgehelprequest.infrastructure.resources.HelpRequestResource;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.MediaType;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;



@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
class HelpRequestResourceIT {

    @MockitoBean
    HelpRequestService helpRequestService;

    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private UserWebClient userWebClient;

    private final String seniorEmail = "manolo@gmail.com";

    @Test
    void whenCreateHelpRequestAsSenior_thenReturns200() throws Exception {
        // Datos de prueba basados en tu esquema Swagger
        String jsonBody = """
            {
              "title": "Ayuda con WhatsApp",
              "description": "No puedo enviar fotos a mis nietos",
              "status":"OPEN"
            }
            """;

        mockMvc.perform(post(HelpRequestResource.HELPREQUESTS)
                        .with(jwt().jwt(j -> j.subject(seniorEmail))
                                .authorities(() -> "ROLE_SENIOR")) // Simula @PreAuthorize y @AuthenticationPrincipal
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andExpect(status().isOk());

        // Verificamos que se llama al servicio con el email extraído del JWT
        verify(helpRequestService).create(eq(seniorEmail), any(HelpRequest.class));
    }

    @Test
    void whenCreateHelpRequestWithoutSeniorRole_thenReturns403() throws Exception {
        String jsonBody = "{\"title\": \"Test\", \"description\": \"Test description\"}";

        mockMvc.perform(post(HelpRequestResource.HELPREQUESTS)
                        .with(jwt().authorities(() -> "ROLE_VOLUNTEER")) // Rol no autorizado
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andExpect(status().isForbidden());
    }

    @Test
    void  whenGetHelpRequestByEmail_thenReturnResult() throws Exception {
        mockMvc.perform(get(HelpRequestResource.HELPREQUESTS)
                        .with(jwt().jwt(j -> j.subject(seniorEmail))
                                .authorities(()-> "ROLE_SENIOR"))
                        .contentType(MediaType.APPLICATION_JSON)
                        )
                .andExpect(status().isOk());
        verify(helpRequestService).getHelpRequestsByEmail(seniorEmail);
    }

}
