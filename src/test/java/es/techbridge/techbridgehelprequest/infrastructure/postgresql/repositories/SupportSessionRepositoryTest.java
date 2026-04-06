package es.techbridge.techbridgehelprequest.infrastructure.postgresql.repositories;

import es.techbridge.techbridgehelprequest.infrastructure.postgresql.entities.SessionMethods;
import es.techbridge.techbridgehelprequest.infrastructure.postgresql.entities.SupportSessionEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@DataJpaTest
@ActiveProfiles("test") // Asegúrate de tener un application-test.yml para H2
@Import(SeederForDev.class)
public class SupportSessionRepositoryTest {

    @Autowired
    private SupportSessionRepository supportSessionRepository;

    @Test
    void shouldFindSessionAndLinkedRequestFromSeeder() {
        // El ID de la sesión del seeder para "App del Banco"
        UUID sessionId = UUID.fromString("22222222-bbbb-cccc-dddd-eeeeffff0001");

        Optional<SupportSessionEntity> found = supportSessionRepository.findById(sessionId);

        assertThat(found).isPresent();
        assertThat(found.get().getSessionMethod()).isEqualTo(SessionMethods.ONLINE_MEETING);

        // Verificamos que la relación 1:1 con HelpRequest funciona
        assertThat(found.get().getHelpRequest()).isNotNull();
        assertThat(found.get().getHelpRequest().getTitle()).isEqualTo("Problemas con la App del Banco");
    }

    @Test
    void shouldVerifyRecordingConsentFromSeeder() {
        UUID sessionId = UUID.fromString("22222222-bbbb-cccc-dddd-eeeeffff0001");

        SupportSessionEntity session = supportSessionRepository.findById(sessionId).orElseThrow();

        // En el seeder pusimos que esta sesión SÍ tenía consentimiento
        assertThat(session.getRecordingConsent()).isTrue();
    }
}
