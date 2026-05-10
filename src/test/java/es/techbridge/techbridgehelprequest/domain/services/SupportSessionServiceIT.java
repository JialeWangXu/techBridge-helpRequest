package es.techbridge.techbridgehelprequest.domain.services;

import es.techbridge.techbridgehelprequest.domain.exceptions.NotFoundException;
import es.techbridge.techbridgehelprequest.domain.model.helprequest.HelpRequest;
import es.techbridge.techbridgehelprequest.domain.model.supportsession.SupportSession;
import es.techbridge.techbridgehelprequest.infrastructure.postgresql.entities.*;
import es.techbridge.techbridgehelprequest.infrastructure.postgresql.repositories.HelpRequestRepository;
import es.techbridge.techbridgehelprequest.infrastructure.postgresql.repositories.SupportSessionRepository;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
class SupportSessionServiceIT {

    private static final UUID REQUEST_ID_FINDING_VOLUNTEER = UUID.fromString("11111111-2222-3333-4444-555566660001");
    private static final UUID SUPPORT_SESSION_ID_IN_PROGRESS = UUID.fromString("22222222-bbbb-cccc-dddd-eeeeffff0003");

    @Autowired
    private SupportSessionService supportSessionService;

    @Autowired
    private SupportSessionRepository supportSessionRepository;

    @Autowired
    private HelpRequestRepository helpRequestRepository;

    @MockitoBean
    private SupportResourceService supportResourceService;

    @Test
    void create() {
        long supportSessionsBefore = this.supportSessionRepository.count();
        HelpRequest helpRequest = this.helpRequestRepository.findById(REQUEST_ID_FINDING_VOLUNTEER)
                .orElseThrow()
                .toHelpRequest();

        SupportSession supportSession = SupportSession.builder()
                .sessionMethod(SessionMethods.TELEPHONE)
                .status(HelpStatus.ACTIVE)
                .recordingConsent(false)
                .volunteerNotes("Nueva sesion de soporte para pruebas")
                .helpRequest(helpRequest)
                .build();

        this.supportSessionService.create(supportSession);

        assertThat(this.supportSessionRepository.count()).isEqualTo(supportSessionsBefore + 1);
        SupportSessionEntity createdSupportSession = this.supportSessionRepository.findAll().stream()
                .filter(session -> session.getHelpRequest() != null)
                .filter(session -> REQUEST_ID_FINDING_VOLUNTEER.equals(session.getHelpRequest().getId()))
                .findFirst()
                .orElseThrow();
        assertThat(createdSupportSession.getSessionMethod()).isEqualTo(SessionMethods.TELEPHONE);
        assertThat(createdSupportSession.getStatus()).isEqualTo(HelpStatus.ACTIVE);
        assertThat(createdSupportSession.getVolunteerNotes()).isEqualTo("Nueva sesion de soporte para pruebas");
    }

    @Test
    void updateHelpStatusById() {
        this.supportSessionService.updateHelpStatusById(SUPPORT_SESSION_ID_IN_PROGRESS, HelpStatus.CANCELLED);

        assertThat(this.supportSessionRepository.findById(SUPPORT_SESSION_ID_IN_PROGRESS))
                .get()
                .extracting(SupportSessionEntity::getStatus)
                .isEqualTo(HelpStatus.CANCELLED);
    }

    @Test
    void updateHelpStatusByIdNotFound() {
        UUID id = UUID.fromString("22222222-bbbb-cccc-dddd-eeeeffff0999");

        assertThatThrownBy(() -> this.supportSessionService.updateHelpStatusById(id, HelpStatus.CANCELLED))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining(id.toString());
    }

    @Test
    void updateSupportSession_thenOnlyNonNullFieldsAreUpdated() {
        SupportSession partialUpdate = SupportSession.builder()
                .sessionMethod(SessionMethods.IN_PERSON)
                .build();

        SupportSession supportSession = this.supportSessionService.updateSupportSession(
                partialUpdate,
                SUPPORT_SESSION_ID_IN_PROGRESS
        );

        assertThat(supportSession.getId()).isEqualTo(SUPPORT_SESSION_ID_IN_PROGRESS);
        assertThat(supportSession.getSessionMethod()).isEqualTo(SessionMethods.IN_PERSON);
        assertThat(this.supportSessionRepository.findById(SUPPORT_SESSION_ID_IN_PROGRESS))
                .get()
                .satisfies(session -> {
                    assertThat(session.getSessionMethod()).isEqualTo(SessionMethods.IN_PERSON);
                    assertThat(session.getStatus()).isEqualTo(HelpStatus.ACTIVE);
                    assertThat(session.getRecordingConsent()).isTrue();
                    assertThat(session.getS3RecordingUrl()).isEqualTo("https://s3.aws.com/techbridge/session001.mp4");
                    assertThat(session.getVolunteerNotes()).isEqualTo("El senior progresa adecuadamente con la tablet.");
                });
    }

    @Test
    void updateSupportSession_thenUpdateSeveralFieldsAndKeepRemainingOnes() {
        SupportSession partialUpdate = SupportSession.builder()
                .recordingConsent(false)
                .volunteerNotes("Sesion actualizada desde test")
                .build();

        SupportSession supportSession = this.supportSessionService.updateSupportSession(
                partialUpdate,
                SUPPORT_SESSION_ID_IN_PROGRESS
        );

        assertThat(supportSession.getId()).isEqualTo(SUPPORT_SESSION_ID_IN_PROGRESS);
        assertThat(this.supportSessionRepository.findById(SUPPORT_SESSION_ID_IN_PROGRESS))
                .get()
                .satisfies(session -> {
                    assertThat(session.getRecordingConsent()).isFalse();
                    assertThat(session.getVolunteerNotes()).isEqualTo("Sesion actualizada desde test");
                    assertThat(session.getSessionMethod()).isEqualTo(SessionMethods.ONLINE_MEETING);
                    assertThat(session.getStatus()).isEqualTo(HelpStatus.ACTIVE);
                    assertThat(session.getS3RecordingUrl()).isEqualTo("https://s3.aws.com/techbridge/session001.mp4");
                });
    }

    @Test
    void updateSupportSession_thenUpdateMeetingUrl() {
        SupportSession partialUpdate = SupportSession.builder()
                .meetingUrl("https://meet.techbridge.dev/session-123")
                .build();

        SupportSession supportSession = this.supportSessionService.updateSupportSession(
                partialUpdate,
                SUPPORT_SESSION_ID_IN_PROGRESS
        );

        assertThat(supportSession.getId()).isEqualTo(SUPPORT_SESSION_ID_IN_PROGRESS);
        assertThat(supportSession.getMeetingUrl()).isEqualTo("https://meet.techbridge.dev/session-123");
        assertThat(this.supportSessionRepository.findById(SUPPORT_SESSION_ID_IN_PROGRESS))
                .get()
                .satisfies(session -> {
                    assertThat(session.getMeetingUrl()).isEqualTo("https://meet.techbridge.dev/session-123");
                    assertThat(session.getSessionMethod()).isEqualTo(SessionMethods.ONLINE_MEETING);
                    assertThat(session.getStatus()).isEqualTo(HelpStatus.ACTIVE);
                });
    }

    @Test
    void updateSupportSessionNotFound() {
        UUID id = UUID.fromString("22222222-bbbb-cccc-dddd-eeeeffff0888");
        SupportSession partialUpdate = SupportSession.builder()
                .sessionMethod(SessionMethods.ONLINE_MEETING)
                .build();

        assertThatThrownBy(() -> this.supportSessionService.updateSupportSession(partialUpdate, id))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining(id.toString());
    }

    @Test
    void deleteById() {
        HelpRequestEntity help = this.helpRequestRepository.findById(UUID.fromString("11111111-2222-3333-4444-555566660001")).get();
        SupportSessionEntity sessionToDelete = SupportSessionEntity.builder()
                .id(UUID.fromString("11111111-2222-3333-4444-555566661004"))
                .status(HelpStatus.FINISHED)
                .helpRequest(help)
                .build();
        this.supportSessionRepository.save(sessionToDelete);

        this.supportSessionService.deleteById(UUID.fromString("11111111-2222-3333-4444-555566661004"));

        assertThat(this.helpRequestRepository.existsById(UUID.fromString("11111111-2222-3333-4444-555566661004"))).isFalse();
    }

    @Test
    void uploadResource() throws IOException {
        MockMultipartFile file = new MockMultipartFile(
                "file", "original.pdf", "application/pdf", "contenido-binario".getBytes()
        );

        BDDMockito.given(supportResourceService.uploadSupportSessionResource(any(String.class),any(MultipartFile.class))).willReturn(SUPPORT_SESSION_ID_IN_PROGRESS.toString());
        this.supportSessionService.uploadResource(SUPPORT_SESSION_ID_IN_PROGRESS,file);

        assertThat(this.supportSessionRepository.findById(SUPPORT_SESSION_ID_IN_PROGRESS).get().getS3RecordingUrl())
                .isEqualTo(SUPPORT_SESSION_ID_IN_PROGRESS.toString());
    }


    @Test
    void downloadResource(){
        BDDMockito.given(supportResourceService.downLoadSupportSessionResource(any(String.class))).willReturn(SUPPORT_SESSION_ID_IN_PROGRESS.toString());
        String resul = this.supportSessionService.downloadResource(SUPPORT_SESSION_ID_IN_PROGRESS);
        assertThat(resul).isNotEmpty()
                .isEqualTo(SUPPORT_SESSION_ID_IN_PROGRESS.toString());

    }



}
