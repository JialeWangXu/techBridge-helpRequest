package es.techbridge.techbridgehelprequest.infrastructure.postgresql.repositories;

import es.techbridge.techbridgehelprequest.infrastructure.postgresql.entities.HelpRequestEntity;
import es.techbridge.techbridgehelprequest.infrastructure.postgresql.entities.RequestStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@Import(SeederForDev.class)
class HelpRequestRepositoryTest {

    @Autowired
    private HelpRequestRepository helpRequestRepository;

    private final UUID seniorId = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private final UUID volunteerId = UUID.fromString("22222222-2222-2222-2222-222222222222");

    @Test
    void shouldFindSeedDataById() {
        UUID tabletId = UUID.fromString("11111111-2222-3333-4444-555566660001");

        Optional<HelpRequestEntity> found = helpRequestRepository.findById(tabletId);

        assertThat(found).isPresent();
        assertThat(found.get().getTitle()).isEqualTo("Configuracion de Tablet nueva");
        assertThat(found.get().getStatus()).isEqualTo(RequestStatus.FINDING_VOLUNTEER);
    }

    @Test
    void shouldSeedEnoughRequestsForManualTesting() {
        List<HelpRequestEntity> requests = this.helpRequestRepository.findAll();

        assertThat(requests).hasSize(27);
        assertThat(requests)
                .extracting(HelpRequestEntity::getStatus)
                .contains(
                        RequestStatus.OPEN,
                        RequestStatus.FINDING_VOLUNTEER,
                        RequestStatus.IN_PROGRESS,
                        RequestStatus.COMPLETED,
                        RequestStatus.CANCELLED
                );
    }

    @Test
    void shouldFindOpenRequestsFromSeeder() {
        List<HelpRequestEntity> openRequests = helpRequestRepository.findByStatus(RequestStatus.OPEN);

        assertThat(openRequests).hasSize(5);
    }

    @Test
    void shouldFindRequestsBySeniorId() {
        List<HelpRequestEntity> seniorRequests = helpRequestRepository.findBySeniorId(seniorId);

        assertThat(seniorRequests).hasSize(27);
    }

    @Test
    void shouldFindRequestById() {
        Optional<HelpRequestEntity> result = this.helpRequestRepository
                .findById(UUID.fromString("11111111-2222-3333-4444-555566660001"));

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(UUID.fromString("11111111-2222-3333-4444-555566660001"));
    }

    @Test
    void shouldFindAvailableRequests() {
        List<HelpRequestEntity> result = this.helpRequestRepository.findAllByStatus(RequestStatus.FINDING_VOLUNTEER);

        assertThat(result).hasSize(7);
    }

    @Test
    void shouldFindAvailableRequestsWithSearchText() {
        Page<HelpRequestEntity> result = this.helpRequestRepository.findAvailableRequestsWithFilters(
                "tablet",
                PageRequest.of(0, 10)
        );

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst().getTitle()).contains("Tablet");
    }

    @Test
    void shouldFindAvailableRequestsWithSearchTextAndSeniorIds() {
        Page<HelpRequestEntity> result = this.helpRequestRepository.findAvailableRequestsWithFiltersBySeniorIds(
                "whatsapp",
                List.of(seniorId),
                PageRequest.of(0, 10)
        );

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst().getSeniorId()).isEqualTo(seniorId);
    }

    @Test
    void shouldFindRequestsByVolunteerId() {
        List<HelpRequestEntity> result = this.helpRequestRepository.findAllByVolunteerId(volunteerId);

        assertThat(result).hasSize(15);
        assertThat(result)
                .extracting(HelpRequestEntity::getId)
                .contains(
                        UUID.fromString("11111111-2222-3333-4444-555566660002"),
                        UUID.fromString("11111111-2222-3333-4444-555566660003"),
                        UUID.fromString("11111111-2222-3333-4444-555566660021"),
                        UUID.fromString("11111111-2222-3333-4444-555566660033")
                );
    }
}
