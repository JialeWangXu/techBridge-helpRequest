package es.techbridge.techbridgehelprequest.infrastructure.resource;

import es.techbridge.techbridgehelprequest.domain.services.SupportResourceService;
import es.techbridge.techbridgehelprequest.infrastructure.postgresql.entities.SessionMethods;
import es.techbridge.techbridgehelprequest.infrastructure.postgresql.entities.SupportSessionEntity;
import es.techbridge.techbridgehelprequest.infrastructure.postgresql.repositories.SupportSessionRepository;
import es.techbridge.techbridgehelprequest.infrastructure.resources.SupportSessionResource;
import jakarta.ws.rs.core.MediaType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
@AutoConfigureMockMvc
class SupportSessionResourceIT {

    private static final UUID SUPPORT_SESSION_ID_IN_PROGRESS = UUID.fromString("22222222-bbbb-cccc-dddd-eeeeffff0003");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SupportSessionRepository supportSessionRepository;

    @MockitoBean
    private SupportResourceService supportResourceService;

    @Test
    void whenUpdateSupportSession_thenOnlyNonNullFieldsAreUpdated() throws Exception {
        String jsonBody = """
            {
              "sessionMethod": "IN_PERSON"
            }
            """;

        this.mockMvc.perform(put(SupportSessionResource.SUPPORTSESSION + SupportSessionResource.ID, SUPPORT_SESSION_ID_IN_PROGRESS)
                        .with(jwt().authorities(() -> "ROLE_VOLUNTEER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(SUPPORT_SESSION_ID_IN_PROGRESS.toString()))
                .andExpect(jsonPath("$.sessionMethod").value("IN_PERSON"))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.recordingConsent").value(true))
                .andExpect(jsonPath("$.s3RecordingUrl").value("https://s3.aws.com/techbridge/session001.mp4"))
                .andExpect(jsonPath("$.volunteerNotes").value("El senior progresa adecuadamente con la tablet."));

        assertThat(this.supportSessionRepository.findById(SUPPORT_SESSION_ID_IN_PROGRESS))
                .get()
                .satisfies(session -> {
                    assertThat(session.getSessionMethod()).isEqualTo(SessionMethods.IN_PERSON);
                    assertThat(session.getStatus()).isEqualTo(es.techbridge.techbridgehelprequest.infrastructure.postgresql.entities.HelpStatus.ACTIVE);
                    assertThat(session.getRecordingConsent()).isTrue();
                    assertThat(session.getS3RecordingUrl()).isEqualTo("https://s3.aws.com/techbridge/session001.mp4");
                    assertThat(session.getVolunteerNotes()).isEqualTo("El senior progresa adecuadamente con la tablet.");
                });
    }

    @Test
    void whenUpdateSupportSessionWithSeveralFields_thenKeepNullFieldsUntouched() throws Exception {
        String jsonBody = """
            {
              "recordingConsent": false,
              "volunteerNotes": "Actualizado desde resource test"
            }
            """;

        this.mockMvc.perform(put(SupportSessionResource.SUPPORTSESSION + SupportSessionResource.ID, SUPPORT_SESSION_ID_IN_PROGRESS)
                        .with(jwt().authorities(() -> "ROLE_VOLUNTEER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(SUPPORT_SESSION_ID_IN_PROGRESS.toString()))
                .andExpect(jsonPath("$.recordingConsent").value(false))
                .andExpect(jsonPath("$.volunteerNotes").value("Actualizado desde resource test"))
                .andExpect(jsonPath("$.sessionMethod").value("ONLINE_MEETING"))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.s3RecordingUrl").value("https://s3.aws.com/techbridge/session001.mp4"));

        assertThat(this.supportSessionRepository.findById(SUPPORT_SESSION_ID_IN_PROGRESS))
                .get()
                .satisfies(session -> {
                    assertThat(session.getRecordingConsent()).isFalse();
                    assertThat(session.getVolunteerNotes()).isEqualTo("Actualizado desde resource test");
                    assertThat(session.getSessionMethod()).isEqualTo(SessionMethods.ONLINE_MEETING);
                    assertThat(session.getStatus()).isEqualTo(es.techbridge.techbridgehelprequest.infrastructure.postgresql.entities.HelpStatus.ACTIVE);
                    assertThat(session.getS3RecordingUrl()).isEqualTo("https://s3.aws.com/techbridge/session001.mp4");
                });
    }

    @Test
    void whenUpdateSupportSessionWithoutVolunteerRole_thenReturns403() throws Exception {
        String jsonBody = """
            {
              "sessionMethod": "ONLINE_MEETING"
            }
            """;

        this.mockMvc.perform(put(SupportSessionResource.SUPPORTSESSION + SupportSessionResource.ID, SUPPORT_SESSION_ID_IN_PROGRESS)
                        .with(jwt().authorities(() -> "ROLE_SENIOR"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andExpect(status().isForbidden());

        assertThat(this.supportSessionRepository.findById(SUPPORT_SESSION_ID_IN_PROGRESS))
                .get()
                .extracting(SupportSessionEntity::getSessionMethod)
                .isEqualTo(SessionMethods.ONLINE_MEETING);
    }

    @Test
    void whenVolunteerUploadsFile_thenReturns200() throws Exception {
        // Creamos un archivo simulado
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.pdf",
                MediaType.MEDIA_TYPE_WILDCARD,
                "contenido".getBytes()
        );

        // Simulamos el ID de la sesión
        UUID sessionId = UUID.randomUUID();

        // MockMvc para Multipart
        this.mockMvc.perform(multipart(SupportSessionResource.SUPPORTSESSION + "/" + sessionId)
                        .file(file)
                        .with(jwt().authorities(() -> "ROLE_VOLUNTEER"))) // Tiene el rol correcto
                .andExpect(status().isOk());

    }

    @Test
    void whenSeniorTriesToUploadFile_thenReturns403() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "test.pdf", "text/plain", "abc".getBytes());
        UUID sessionId = UUID.randomUUID();

        this.mockMvc.perform(multipart(SupportSessionResource.SUPPORTSESSION + "/" + sessionId)
                        .file(file)
                        .with(jwt().authorities(() -> "ROLE_SENIOR"))) // Rol incorrecto
                .andExpect(status().isForbidden());
    }
}
