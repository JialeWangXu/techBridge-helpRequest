package es.techbridge.techbridgehelprequest.infrastructure.resource;

import es.techbridge.techbridgehelprequest.domain.model.user.UserDto;
import es.techbridge.techbridgehelprequest.domain.model.user.UserRole;
import es.techbridge.techbridgehelprequest.domain.webclients.UserWebClient;
import es.techbridge.techbridgehelprequest.infrastructure.postgresql.entities.HelpStatus;
import es.techbridge.techbridgehelprequest.infrastructure.postgresql.entities.HelpRequestEntity;
import es.techbridge.techbridgehelprequest.infrastructure.postgresql.entities.RequestStatus;
import es.techbridge.techbridgehelprequest.infrastructure.postgresql.entities.SupportSessionEntity;
import es.techbridge.techbridgehelprequest.infrastructure.postgresql.repositories.HelpRequestRepository;
import es.techbridge.techbridgehelprequest.infrastructure.postgresql.repositories.SupportSessionRepository;
import es.techbridge.techbridgehelprequest.infrastructure.resources.HelpRequestResource;
import jakarta.ws.rs.core.MediaType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
@AutoConfigureMockMvc
class HelpRequestResourceIT {

    private static final UUID SENIOR_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID VOLUNTEER_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final UUID REQUEST_ID_FINDING_VOLUNTEER = UUID.fromString("11111111-2222-3333-4444-555566660001");
    private static final UUID REQUEST_ID_IN_PROGRESS = UUID.fromString("11111111-2222-3333-4444-555566660002");
    private static final UUID SUPPORT_SESSION_ID_IN_PROGRESS = UUID.fromString("22222222-bbbb-cccc-dddd-eeeeffff0003");
    private static final String SENIOR_EMAIL = "manolo@gmail.com";
    private static final String VOLUNTEER_EMAIL = "lucia@volunteer.org";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private HelpRequestRepository helpRequestRepository;

    @Autowired
    private SupportSessionRepository supportSessionRepository;

    @MockitoBean
    private UserWebClient userWebClient;

    @BeforeEach
    void setUp() {
        UserDto senior = UserDto.builder()
                .id(SENIOR_ID)
                .firstName("Manolo")
                .lastName("Garcia")
                .email(SENIOR_EMAIL)
                .role(UserRole.SENIOR)
                .build();
        UserDto volunteer = UserDto.builder()
                .id(VOLUNTEER_ID)
                .firstName("Lucia")
                .lastName("Lopez")
                .email(VOLUNTEER_EMAIL)
                .role(UserRole.VOLUNTEER)
                .build();

        BDDMockito.given(this.userWebClient.readByEmail(any(String.class)))
                .willReturn(senior);
        BDDMockito.given(this.userWebClient.readById(any(UUID.class)))
                .willAnswer(invocation -> {
                    UUID id = invocation.getArgument(0);
                    if (VOLUNTEER_ID.equals(id)) {
                        return volunteer;
                    }
                    return senior;
                });
    }

    @Test
    void whenCreateHelpRequestAsSenior_thenReturns200() throws Exception {
        String jsonBody = """
            {
              "title": "Ayuda con WhatsApp",
              "description": "No puedo enviar fotos a mis nietos",
              "status": "OPEN"
            }
            """;

        this.mockMvc.perform(post(HelpRequestResource.HELPREQUESTS)
                        .with(jwt().jwt(jwt -> jwt.subject(SENIOR_EMAIL))
                                .authorities(() -> "ROLE_SENIOR"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andExpect(status().isOk());

        HelpRequestEntity createdRequest = this.helpRequestRepository.findBySeniorId(SENIOR_ID).stream()
                .filter(helpRequestEntity -> helpRequestEntity.getTitle().equals("Ayuda con WhatsApp"))
                .findFirst()
                .orElseThrow();

        assertThat(createdRequest.getDescription()).isEqualTo("No puedo enviar fotos a mis nietos");
        assertThat(createdRequest.getStatus()).isEqualTo(RequestStatus.OPEN);
        assertThat(createdRequest.getSeniorId()).isEqualTo(SENIOR_ID);
    }

    @Test
    void whenCreateHelpRequestWithoutSeniorRole_thenReturns403() throws Exception {
        String jsonBody = """
            {
              "title": "Request forbidden",
              "description": "Test description",
              "status": "OPEN"
            }
            """;

        this.mockMvc.perform(post(HelpRequestResource.HELPREQUESTS)
                        .with(jwt().authorities(() -> "ROLE_VOLUNTEER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andExpect(status().isForbidden());

        assertThat(this.helpRequestRepository.findBySeniorId(SENIOR_ID))
                .extracting(HelpRequestEntity::getTitle)
                .doesNotContain("Request forbidden");
    }

    @Test
    void whenGetSeniorHelpRequestsByEmail_thenReturnResult() throws Exception {
        this.mockMvc.perform(get(HelpRequestResource.HELPREQUESTS + HelpRequestResource.SENIOR_MY)
                        .with(jwt().jwt(jwt -> jwt.subject(SENIOR_EMAIL))
                                .authorities(() -> "ROLE_SENIOR"))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].senior.email").value(SENIOR_EMAIL));
    }

    @Test
    void whenGetVolunteerHelpRequestsByEmail_thenReturnResult() throws Exception {
        BDDMockito.given(this.userWebClient.readByEmail(any(String.class)))
                .willReturn(UserDto.builder()
                        .id(VOLUNTEER_ID)
                        .firstName("Lucia")
                        .lastName("Lopez")
                        .email(VOLUNTEER_EMAIL)
                        .role(UserRole.VOLUNTEER)
                        .build());

        this.mockMvc.perform(get(HelpRequestResource.HELPREQUESTS + HelpRequestResource.VOLUNTEER_MY)
                        .with(jwt().jwt(jwt -> jwt.subject(VOLUNTEER_EMAIL))
                                .authorities(() -> "ROLE_VOLUNTEER"))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].senior.email").value(SENIOR_EMAIL));
    }

    @Test
    void whenGetHelpRequestById_thenReturnResult() throws Exception {
        this.mockMvc.perform(get(HelpRequestResource.HELPREQUESTS + HelpRequestResource.ID, REQUEST_ID_FINDING_VOLUNTEER)
                        .with(jwt().jwt(jwt -> jwt.subject(SENIOR_EMAIL))
                                .authorities(() -> "ROLE_SENIOR"))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(REQUEST_ID_FINDING_VOLUNTEER.toString()))
                .andExpect(content().string(containsString("Tablet")))
                .andExpect(jsonPath("$.status").value("FINDING_VOLUNTEER"))
                .andExpect(jsonPath("$.senior.email").value(SENIOR_EMAIL));
    }

    @Test
    void whenDeleteRequestById_thenDeleteRequest() throws Exception {
        UUID requestId = UUID.fromString("11111111-2222-3333-4444-555566660004");
        HelpRequestEntity requestToDelete = HelpRequestEntity.builder()
                .id(requestId)
                .title("Delete from resource")
                .description("Delete from resource")
                .status(RequestStatus.CANCELLED)
                .seniorId(SENIOR_ID)
                .build();
        this.helpRequestRepository.save(requestToDelete);

        this.mockMvc.perform(delete(HelpRequestResource.HELPREQUESTS + HelpRequestResource.ID, requestId)
                        .with(jwt().jwt(jwt -> jwt.subject(SENIOR_EMAIL))
                                .authorities(() -> "ROLE_SENIOR"))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        assertThat(this.helpRequestRepository.existsById(requestId)).isFalse();
    }

    @Test
    void whenGetAllAvailableHelpRequest() throws Exception {
        this.mockMvc.perform(get(HelpRequestResource.HELPREQUESTS + HelpRequestResource.AVAILABLE)
                        .with(jwt().jwt(jwt -> jwt.subject(VOLUNTEER_EMAIL))
                                .authorities(() -> "ROLE_VOLUNTEER"))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(REQUEST_ID_FINDING_VOLUNTEER.toString()))
                .andExpect(jsonPath("$[0].status").value("FINDING_VOLUNTEER"))
                .andExpect(jsonPath("$[0].senior.email").value(SENIOR_EMAIL));
    }

    @Test
    void whenGetAllAvailableHelpRequest_thenExcludeAssignedRequestsStillMarkedAsFindingVolunteer() throws Exception {
        this.helpRequestRepository.save(HelpRequestEntity.builder()
                .id(UUID.fromString("11111111-2222-3333-4444-555566660099"))
                .title("Peticion inconsistente")
                .description("Tiene voluntario asignado pero sigue en FINDING_VOLUNTEER")
                .status(RequestStatus.FINDING_VOLUNTEER)
                .aiTutorialId(UUID.fromString("11111111-7777-3333-4444-555566660099"))
                .seniorId(SENIOR_ID)
                .volunteerId(VOLUNTEER_ID)
                .build());

        this.mockMvc.perform(get(HelpRequestResource.HELPREQUESTS + HelpRequestResource.AVAILABLE)
                        .with(jwt().jwt(jwt -> jwt.subject(VOLUNTEER_EMAIL))
                                .authorities(() -> "ROLE_VOLUNTEER"))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(REQUEST_ID_FINDING_VOLUNTEER.toString()));
    }

    @Test
    void whenUpdateRequestStatusById() throws Exception {
        long supportSessionsBefore = this.supportSessionRepository.count();
        String jsonRequestStatus =  """
            {
              "status": "IN_PROGRESS"
            }
            """;

        this.mockMvc.perform(put(HelpRequestResource.HELPREQUESTS + HelpRequestResource.ID, REQUEST_ID_FINDING_VOLUNTEER)
                        .with(jwt().jwt(jwt -> jwt.subject(VOLUNTEER_EMAIL))
                                .authorities(() -> "ROLE_VOLUNTEER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequestStatus))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(REQUEST_ID_FINDING_VOLUNTEER.toString()))
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));

        assertThat(this.helpRequestRepository.findById(REQUEST_ID_FINDING_VOLUNTEER))
                .get()
                .satisfies(request -> {
                    assertThat(request.getStatus()).isEqualTo(RequestStatus.IN_PROGRESS);
                });
        assertThat(this.supportSessionRepository.count()).isEqualTo(supportSessionsBefore + 1);
    }

    @Test
    void whenUpdateRequestStatusToCancelled_thenCancelSupportSession() throws Exception {
        String jsonRequestStatus = """
            {
              "status": "CANCELLED"
            }
            """;

        this.mockMvc.perform(put(HelpRequestResource.HELPREQUESTS + HelpRequestResource.ID, REQUEST_ID_IN_PROGRESS)
                        .with(jwt().jwt(jwt -> jwt.subject(VOLUNTEER_EMAIL))
                                .authorities(() -> "ROLE_VOLUNTEER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequestStatus))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(REQUEST_ID_IN_PROGRESS.toString()))
                .andExpect(jsonPath("$.status").value("CANCELLED"));

        assertThat(this.helpRequestRepository.findById(REQUEST_ID_IN_PROGRESS))
                .get()
                .extracting(HelpRequestEntity::getStatus)
                .isEqualTo(RequestStatus.CANCELLED);
        assertThat(this.supportSessionRepository.findById(SUPPORT_SESSION_ID_IN_PROGRESS))
                .get()
                .extracting(SupportSessionEntity::getStatus)
                .isEqualTo(HelpStatus.CANCELLED);
    }

    @Test
    void whenUpdateRequestStatusToCompleted_thenFinishSupportSession() throws Exception {
        String jsonRequestStatus = """
            {
              "status": "COMPLETED"
            }
            """;

        this.mockMvc.perform(put(HelpRequestResource.HELPREQUESTS + HelpRequestResource.ID, REQUEST_ID_IN_PROGRESS)
                        .with(jwt().jwt(jwt -> jwt.subject(VOLUNTEER_EMAIL))
                                .authorities(() -> "ROLE_VOLUNTEER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequestStatus))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(REQUEST_ID_IN_PROGRESS.toString()))
                .andExpect(jsonPath("$.status").value("COMPLETED"));

        assertThat(this.helpRequestRepository.findById(REQUEST_ID_IN_PROGRESS))
                .get()
                .extracting(HelpRequestEntity::getStatus)
                .isEqualTo(RequestStatus.COMPLETED);
        assertThat(this.supportSessionRepository.findById(SUPPORT_SESSION_ID_IN_PROGRESS))
                .get()
                .extracting(SupportSessionEntity::getStatus)
                .isEqualTo(HelpStatus.FINISHED);
    }

    @Test
    void whenSaveAiTutorialId_thenUpdateTutorialId() throws Exception {
        String jsonRequest= "\"33333333-bbbb-cccc-dddd-eeeeffff0002\"";
        this.mockMvc.perform(put(HelpRequestResource.HELPREQUESTS +HelpRequestResource.SAVEAITUTORIAL_ID, REQUEST_ID_IN_PROGRESS)
                        .with(jwt().jwt(jwt -> jwt.subject(SENIOR_EMAIL))
                                .authorities(() -> "ROLE_SENIOR"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isOk());

        assertThat(this.helpRequestRepository.findById(REQUEST_ID_IN_PROGRESS))
                .get()
                .extracting(HelpRequestEntity::getAiTutorialId)
                .isEqualTo(UUID.fromString("33333333-bbbb-cccc-dddd-eeeeffff0002"));
    }
}
