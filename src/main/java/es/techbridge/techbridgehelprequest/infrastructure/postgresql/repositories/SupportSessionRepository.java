package es.techbridge.techbridgehelprequest.infrastructure.postgresql.repositories;

import es.techbridge.techbridgehelprequest.infrastructure.postgresql.entities.SupportSessionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface SupportSessionRepository extends JpaRepository<SupportSessionEntity, UUID> {

    Optional<SupportSessionEntity> findById(UUID uuid);

}
