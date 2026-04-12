package es.techbridge.techbridgehelprequest.infrastructure.postgresql.repositories;

import es.techbridge.techbridgehelprequest.infrastructure.postgresql.entities.HelpRequestEntity;
import es.techbridge.techbridgehelprequest.infrastructure.postgresql.entities.RequestStatus;
import org.jspecify.annotations.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface HelpRequestRepository extends JpaRepository<HelpRequestEntity, UUID> {

    Optional<HelpRequestEntity> findById(@NonNull UUID uuid);

    List<HelpRequestEntity> findBySeniorId(UUID seniorId);

    List<HelpRequestEntity> findByStatus(RequestStatus status);

    void deleteById(@NonNull UUID uuid);

    List<HelpRequestEntity> findAllByStatus(RequestStatus status);
}
