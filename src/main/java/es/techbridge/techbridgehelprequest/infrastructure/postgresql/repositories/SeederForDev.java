package es.techbridge.techbridgehelprequest.infrastructure.postgresql.repositories;

import es.techbridge.techbridgehelprequest.infrastructure.postgresql.entities.HelpRequestEntity;
import es.techbridge.techbridgehelprequest.infrastructure.postgresql.entities.HelpStatus;
import es.techbridge.techbridgehelprequest.infrastructure.postgresql.entities.RequestStatus;
import es.techbridge.techbridgehelprequest.infrastructure.postgresql.entities.SessionMethods;
import es.techbridge.techbridgehelprequest.infrastructure.postgresql.entities.SupportSessionEntity;
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

        HelpRequestEntity[] requests = {
                HelpRequestEntity.builder()
                        .id(requestId(1))
                        .title("Configuracion de Tablet nueva")
                        .description("Necesito ayuda para instalar WhatsApp y el correo en una Samsung Tab.")
                        .aiTutorialId(tutorialId)
                        .status(RequestStatus.FINDING_VOLUNTEER)
                        .seniorId(seniorId)
                        .build(),
                HelpRequestEntity.builder()
                        .id(requestId(2))
                        .title("Problemas con la App del Banco")
                        .description("No consigo acceder a mi cuenta, me da error de credenciales.")
                        .status(RequestStatus.IN_PROGRESS)
                        .aiTutorialId(tutorialId)
                        .seniorId(seniorId)
                        .volunteerId(volunteerId)
                        .createdAt(LocalDateTime.now().minusDays(1))
                        .build(),
                HelpRequestEntity.builder()
                        .id(requestId(3))
                        .title("Videollamada con familiares")
                        .description("Quiero aprender a usar Zoom para hablar con mis nietos en el extranjero.")
                        .aiTutorialId(tutorialId)
                        .status(RequestStatus.COMPLETED)
                        .seniorId(seniorId)
                        .volunteerId(volunteerId)
                        .createdAt(LocalDateTime.now().minusHours(5))
                        .build(),
                helpRequest(10, "Configurar el lector de pantalla", "Necesito activar TalkBack y aprender los gestos basicos.", RequestStatus.FINDING_VOLUNTEER, true, false, 2),
                helpRequest(11, "Recuperar fotos borradas del movil", "Quiero revisar la papelera de Google Fotos antes de perder las imagenes.", RequestStatus.FINDING_VOLUNTEER, true, false, 3),
                helpRequest(12, "Instalar certificado digital", "Me han pedido el certificado para una gestion con la administracion.", RequestStatus.FINDING_VOLUNTEER, true, false, 4),
                helpRequest(13, "Configurar alarma de medicacion", "Necesito recordatorios diarios para varias pastillas.", RequestStatus.FINDING_VOLUNTEER, true, false, 5),
                helpRequest(14, "Aprender a usar Bizum", "Quiero enviar pequenas cantidades de dinero de forma segura.", RequestStatus.FINDING_VOLUNTEER, true, false, 6),
                helpRequest(15, "Actualizar el navegador", "Algunas paginas no cargan porque Chrome esta desactualizado.", RequestStatus.FINDING_VOLUNTEER, true, false, 7),
                helpRequest(16, "Dudas antes de comprar un movil", "Necesito comparar opciones sencillas y con buena bateria.", RequestStatus.OPEN, false, false, 8),
                helpRequest(17, "Preparar una impresora nueva", "Aun no he abierto la caja y quiero saber si podre imprimir desde el movil.", RequestStatus.OPEN, false, false, 9),
                helpRequest(18, "Organizar contactos duplicados", "Tengo contactos repetidos y varios sin nombre claro.", RequestStatus.OPEN, false, false, 10),
                helpRequest(19, "Crear una cuenta de correo", "Necesito una cuenta nueva para tramites y familia.", RequestStatus.OPEN, false, false, 11),
                helpRequest(20, "Configurar copia de seguridad", "Quiero asegurar fotos y documentos importantes antes de cambiar de telefono.", RequestStatus.OPEN, false, false, 12),
                helpRequest(21, "Ayuda con una compra online", "No encuentro el seguimiento del pedido ni el correo de confirmacion.", RequestStatus.IN_PROGRESS, true, true, 13),
                helpRequest(22, "Revisar privacidad de Facebook", "Quiero limitar quien puede ver mis publicaciones y fotos.", RequestStatus.IN_PROGRESS, true, true, 14),
                helpRequest(23, "Pasar contactos al movil nuevo", "Tengo los contactos en la SIM y necesito copiarlos al telefono.", RequestStatus.IN_PROGRESS, true, true, 15),
                helpRequest(24, "Configurar videollamada en el televisor", "Quiero usar una camara externa para llamadas familiares.", RequestStatus.IN_PROGRESS, true, true, 16),
                helpRequest(25, "Sincronizar calendario familiar", "Necesito compartir citas medicas con mi hija.", RequestStatus.IN_PROGRESS, true, true, 17),
                helpRequest(26, "Eliminar ventanas emergentes", "Aparecen avisos molestos en el navegador cada pocos minutos.", RequestStatus.COMPLETED, true, true, 18),
                helpRequest(27, "Aprender a escanear documentos", "Quiero enviar documentos en PDF desde el movil.", RequestStatus.COMPLETED, true, true, 19),
                helpRequest(28, "Configurar la app de salud", "La pulsera no sincronizaba pasos ni pulsaciones.", RequestStatus.COMPLETED, true, true, 20),
                helpRequest(29, "Cambiar contrasena del correo", "Habia dudas de seguridad y se cambio la contrasena.", RequestStatus.COMPLETED, true, true, 21),
                helpRequest(30, "Consultar factura de luz", "Se reviso el area cliente y la descarga de facturas.", RequestStatus.COMPLETED, true, true, 22),
                helpRequest(31, "Instalar una app no necesaria", "Se cancelo porque la aplicacion no era oficial.", RequestStatus.CANCELLED, true, true, 23),
                helpRequest(32, "Reparar ordenador antiguo", "Se cancelo porque el equipo no arrancaba y requiere servicio tecnico.", RequestStatus.CANCELLED, true, true, 24),
                helpRequest(33, "Configurar router de casa", "Se cancelo porque la operadora resolvio la incidencia.", RequestStatus.CANCELLED, true, true, 25)
        };

        this.helpRequestRepository.saveAll(List.of(requests));
        log.warn("        ------- HelpRequests creadas -----------------------------------------------------------");

        SupportSessionEntity[] sessions = {
                SupportSessionEntity.builder()
                        .id(sessionId(3))
                        .sessionMethod(SessionMethods.ONLINE_MEETING)
                        .status(HelpStatus.ACTIVE)
                        .recordingConsent(true)
                        .s3RecordingUrl("https://s3.aws.com/techbridge/session001.mp4")
                        .meetingUrl("https://meeting.test.com")
                        .volunteerNotes("El senior progresa adecuadamente con la tablet.")
                        .helpRequest(requests[1])
                        .build(),
                SupportSessionEntity.builder()
                        .id(sessionId(2))
                        .sessionMethod(SessionMethods.TELEPHONE)
                        .status(HelpStatus.FINISHED)
                        .recordingConsent(false)
                        .volunteerNotes("Sesion finalizada con exito. Zoom configurado.")
                        .helpRequest(requests[2])
                        .build(),
                supportSession(21, SessionMethods.TELEPHONE, HelpStatus.ACTIVE, false, null, "Dudas de seguimiento del pedido.", requests[14]),
                supportSession(22, SessionMethods.ONLINE_MEETING, HelpStatus.ACTIVE, true, "https://meeting.test.com/privacy", "Pendiente revisar etiquetas de fotos.", requests[15]),
                supportSession(23, SessionMethods.IN_PERSON, HelpStatus.ACTIVE, false, null, "Se comprobo la SIM y queda importar favoritos.", requests[16]),
                supportSession(24, SessionMethods.ONLINE_MEETING, HelpStatus.ACTIVE, true, "https://meeting.test.com/tv", "La camara ya funciona, falta guardar acceso directo.", requests[17]),
                supportSession(25, SessionMethods.TELEPHONE, HelpStatus.ACTIVE, false, null, "Calendario compartido con permisos basicos.", requests[18]),
                supportSession(26, SessionMethods.ONLINE_MEETING, HelpStatus.FINISHED, true, "https://meeting.test.com/popups", "Se eliminaron permisos de notificaciones.", requests[19]),
                supportSession(27, SessionMethods.TELEPHONE, HelpStatus.FINISHED, false, null, "Aprendio a escanear y compartir PDF.", requests[20]),
                supportSession(28, SessionMethods.IN_PERSON, HelpStatus.FINISHED, false, null, "Pulsera sincronizada correctamente.", requests[21]),
                supportSession(29, SessionMethods.ONLINE_MEETING, HelpStatus.FINISHED, true, "https://meeting.test.com/password", "Contrasena cambiada y verificacion activada.", requests[22]),
                supportSession(30, SessionMethods.TELEPHONE, HelpStatus.FINISHED, false, null, "Factura descargada y enviada por correo.", requests[23]),
                supportSession(31, SessionMethods.TELEPHONE, HelpStatus.CANCELLED, false, null, "Se detecto app no oficial.", requests[24]),
                supportSession(32, SessionMethods.IN_PERSON, HelpStatus.CANCELLED, false, null, "Equipo derivado a servicio tecnico.", requests[25]),
                supportSession(33, SessionMethods.TELEPHONE, HelpStatus.CANCELLED, false, null, "La operadora resolvio la incidencia.", requests[26])
        };

        this.supportSessionRepository.saveAll(List.of(sessions));
        log.warn("        ------- SupportSessions creadas y vinculadas -------------------------------------------");
    }

    private HelpRequestEntity helpRequest(int number,
                                          String title,
                                          String description,
                                          RequestStatus status,
                                          boolean withTutorial,
                                          boolean withVolunteer,
                                          int daysAgo) {
        return HelpRequestEntity.builder()
                .id(requestId(number))
                .title(title)
                .description(description)
                .status(status)
                .seniorId(seniorId)
                .volunteerId(withVolunteer ? volunteerId : null)
                .aiTutorialId(withTutorial ? tutorialId(number) : null)
                .createdAt(LocalDateTime.now().minusDays(daysAgo))
                .build();
    }

    private SupportSessionEntity supportSession(int number,
                                                SessionMethods sessionMethod,
                                                HelpStatus status,
                                                boolean recordingConsent,
                                                String meetingUrl,
                                                String volunteerNotes,
                                                HelpRequestEntity helpRequest) {
        return SupportSessionEntity.builder()
                .id(sessionId(number))
                .sessionMethod(sessionMethod)
                .status(status)
                .recordingConsent(recordingConsent)
                .s3RecordingUrl(recordingConsent ? "https://s3.aws.com/techbridge/session" + number + ".mp4" : null)
                .meetingUrl(meetingUrl)
                .volunteerNotes(volunteerNotes)
                .helpRequest(helpRequest)
                .build();
    }

    private UUID requestId(int number) {
        return UUID.fromString(String.format("11111111-2222-3333-4444-55556666%04d", number));
    }

    private UUID tutorialId(int number) {
        return UUID.fromString(String.format("33333333-bbbb-cccc-dddd-eeeeffff%04d", number));
    }

    private UUID sessionId(int number) {
        return UUID.fromString(String.format("22222222-bbbb-cccc-dddd-eeeeffff%04d", number));
    }
}
