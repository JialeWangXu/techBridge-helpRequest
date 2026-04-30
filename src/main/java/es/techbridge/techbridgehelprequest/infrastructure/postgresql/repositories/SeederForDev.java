package es.techbridge.techbridgehelprequest.infrastructure.postgresql.repositories;

import es.techbridge.techbridgehelprequest.infrastructure.postgresql.entities.*;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Log4j2
@Repository
@Profile({"dev", "test"})
public class SeederForDev {

    private final SupportSessionRepository supportSessionRepository;
    private final HelpRequestRepository helpRequestRepository;
    private final UUID seniorId = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private final UUID volunteerId = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private final UUID tutorialId = UUID.fromString("33333333-bbbb-cccc-dddd-eeeeffff0001");


    public SeederForDev(SupportSessionRepository supportSessionRepository, HelpRequestRepository helpRequestRepository) {
        this.supportSessionRepository = supportSessionRepository;
        this.helpRequestRepository = helpRequestRepository;
        this.deleteAllAndInitializeAndSeedDataBase();
    }

    public void deleteAllAndInitializeAndSeedDataBase() {
        this.deleteAllAndInitialize();
        this.seedDatabase();
    }

    private void deleteAllAndInitialize() {
        this.supportSessionRepository.deleteAll();
        this.helpRequestRepository.deleteAll();
        log.warn("------- Delete All -----------");
    }

    private void seedDatabase() {
        log.warn("------- Initial Load HelpRequest & SupportSession from JAVA ----------------------------------");

        // 1. Creamos las Peticiones de Ayuda (HelpRequests)
        // Usamos UUID fijos para poder referenciarlos luego en tests si hace falta
        HelpRequestEntity[] requests = {
                HelpRequestEntity.builder()
                        .id(UUID.fromString("11111111-2222-3333-4444-555566660001"))
                        .title("Configuración de Tablet nueva")
                        .description("Necesito ayuda para instalar WhatsApp y el correo en una Samsung Tab.")
                        .aiTutorialId(tutorialId)
                        .status(RequestStatus.FINDING_VOLUNTEER)
                        .seniorId(seniorId)
                        .build(),
                HelpRequestEntity.builder()
                        .id(UUID.fromString("11111111-2222-3333-4444-555566660002"))
                        .title("Problemas con la App del Banco")
                        .description("No consigo acceder a mi cuenta, me da error de credenciales.")
                        .status(RequestStatus.IN_PROGRESS)
                        .aiTutorialId(tutorialId)
                        .seniorId(seniorId)
                        .volunteerId(volunteerId) // Ya tiene un voluntario asignado
                        .createdAt(LocalDateTime.now().minusDays(1))
                        .build(),
                HelpRequestEntity.builder()
                        .id(UUID.fromString("11111111-2222-3333-4444-555566660003"))
                        .title("Videollamada con familiares")
                        .description("Quiero aprender a usar Zoom para hablar con mis nietos en el extranjero.")
                        .aiTutorialId(tutorialId)
                        .status(RequestStatus.COMPLETED)
                        .seniorId(seniorId)
                        .volunteerId(volunteerId)
                        .createdAt(LocalDateTime.now().minusHours(5))
                        .build()
        };

        this.helpRequestRepository.saveAll(List.of(requests));
        log.warn("        ------- HelpRequests creadas -----------------------------------------------------------");

        // 2. Creamos las Sesiones de Soporte (SupportSessions)
        // Vinculamos las sesiones a las peticiones anteriores mediante la relación 1:1
        SupportSessionEntity[] sessions = {
                SupportSessionEntity.builder()
                        .id(UUID.fromString("22222222-bbbb-cccc-dddd-eeeeffff0003"))
                        .sessionMethod(SessionMethods.ONLINE_MEETING)
                        .status(HelpStatus.ACTIVE)
                        .recordingConsent(true)
                        .s3RecordingUrl("https://s3.aws.com/techbridge/session001.mp4")
                        .meetingUrl("https://meeting.test.com")
                        .volunteerNotes("El senior progresa adecuadamente con la tablet.")
                        .helpRequest(requests[1]) // Vinculada a "App del Banco"
                        .build(),
                SupportSessionEntity.builder()
                        .id(UUID.fromString("22222222-bbbb-cccc-dddd-eeeeffff0002"))
                        .sessionMethod(SessionMethods.TELEPHONE)
                        .status(HelpStatus.FINISHED)
                        .recordingConsent(false)
                        .volunteerNotes("Sesión finalizada con éxito. Zoom configurado.")
                        .helpRequest(requests[2]) // Vinculada a "Videollamada"
                        .build()
        };

        this.supportSessionRepository.saveAll(List.of(sessions));
        log.warn("        ------- SupportSessions creadas y vinculadas -------------------------------------------");
    }
}
