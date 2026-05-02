package es.techbridge.techbridgehelprequest.domain.services;

import es.techbridge.techbridgehelprequest.domain.exceptions.NotFoundException;
import es.techbridge.techbridgehelprequest.domain.model.helprequest.HelpRequest;
import es.techbridge.techbridgehelprequest.domain.model.supportsession.SupportSession;
import es.techbridge.techbridgehelprequest.infrastructure.postgresql.entities.*;
import es.techbridge.techbridgehelprequest.infrastructure.postgresql.repositories.HelpRequestRepository;
import es.techbridge.techbridgehelprequest.infrastructure.postgresql.repositories.SupportSessionRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
public class SupportSessionServiceIT {

    private static final UUID REQUEST_ID_FINDING_VOLUNTEER = UUID.fromString("11111111-2222-3333-4444-555566660001");
    private static final UUID SUPPORT_SESSION_ID_IN_PROGRESS = UUID.fromString("22222222-bbbb-cccc-dddd-eeeeffff0003");

    @Autowired
    private SupportSessionService supportSessionService;

    @Autowired
    private SupportSessionRepository supportSessionRepository;

    @Autowired
    private HelpRequestRepository helpRequestRepository;

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
    void saveSessionMethod_thenOnlyNonNullFieldsAreUpdated() {
        SupportSession partialUpdate = SupportSession.builder()
                .sessionMethod(SessionMethods.IN_PERSON)
                .build();

        SupportSession supportSession = this.supportSessionService.saveSessionMethod(
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
    void saveSessionMethod_thenUpdateSeveralFieldsAndKeepRemainingOnes() {
        SupportSession partialUpdate = SupportSession.builder()
                .recordingConsent(false)
                .volunteerNotes("Sesion actualizada desde test")
                .build();

        SupportSession supportSession = this.supportSessionService.saveSessionMethod(
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
    void saveSessionMethod_thenUpdateMeetingUrl() {
        SupportSession partialUpdate = SupportSession.builder()
                .meetingUrl("https://meet.techbridge.dev/session-123")
                .build();

        SupportSession supportSession = this.supportSessionService.saveSessionMethod(
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
    void saveSessionMethodNotFound() {
        UUID id = UUID.fromString("22222222-bbbb-cccc-dddd-eeeeffff0888");
        SupportSession partialUpdate = SupportSession.builder()
                .sessionMethod(SessionMethods.ONLINE_MEETING)
                .build();

        assertThatThrownBy(() -> this.supportSessionService.saveSessionMethod(partialUpdate, id))
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
}
