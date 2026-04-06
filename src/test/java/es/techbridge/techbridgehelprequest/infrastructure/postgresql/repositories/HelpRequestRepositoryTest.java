package es.techbridge.techbridgehelprequest.infrastructure.postgresql.repositories;


import es.techbridge.techbridgehelprequest.infrastructure.postgresql.entities.HelpRequestEntity;
import es.techbridge.techbridgehelprequest.infrastructure.postgresql.entities.RequestStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test") // Asegúrate de tener un application-test.yml para H2
@Import(SeederForDev.class)
class HelpRequestRepositoryTest {

    @Autowired
    private HelpRequestRepository helpRequestRepository;
    private final UUID seniorId = UUID.fromString("11111111-1111-1111-1111-111111111111");

    @Test
    void shouldFindSeedDataById() {
        UUID tabletId = UUID.fromString("11111111-2222-3333-4444-555566660001");

        Optional<HelpRequestEntity> found = helpRequestRepository.findById(tabletId);

        assertThat(found).isPresent();
        assertThat(found.get().getTitle()).isEqualTo("Configuración de Tablet nueva");
        assertThat(found.get().getStatus()).isEqualTo(RequestStatus.OPEN);
    }

    @Test
    void shouldFindOpenRequestsFromSeeder() {
        List<HelpRequestEntity> openRequests = helpRequestRepository.findByStatus(RequestStatus.OPEN);

        assertThat(openRequests).isNotEmpty();
        assertThat(openRequests).extracting(HelpRequestEntity::getTitle)
                .contains("Configuración de Tablet nueva");
    }

    @Test
    void shouldFindRequestsBySeniorId() {
        List<HelpRequestEntity> seniorRequests = helpRequestRepository.findBySeniorId(seniorId);
        assertThat(seniorRequests).hasSize(3);
    }
}
