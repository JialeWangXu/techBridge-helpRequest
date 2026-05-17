package es.techbridge.techbridgehelprequest.infrastructure.postgresql.repositories;

import es.techbridge.techbridgehelprequest.infrastructure.postgresql.entities.HelpRequestEntity;
import es.techbridge.techbridgehelprequest.infrastructure.postgresql.entities.HelpStatus;
import es.techbridge.techbridgehelprequest.infrastructure.postgresql.entities.RequestStatus;
import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface HelpRequestRepository extends JpaRepository<HelpRequestEntity, UUID> {

    Optional<HelpRequestEntity> findById(@NonNull UUID uuid);
    Page<HelpRequestEntity> findAllBySeniorIdAndStatusAndVolunteerIdIsNull(UUID seniorId, RequestStatus status, Pageable pageable);
    Page<HelpRequestEntity> findAllBySeniorIdAndStatusAndVolunteerIdIsNotNull(UUID seniorId, RequestStatus status, Pageable pageable);
    Page<HelpRequestEntity> findAllBySeniorIdAndStatus(UUID seniorId, RequestStatus status, Pageable pageable);
    List<HelpRequestEntity> findBySeniorId(UUID seniorId);
    List<HelpRequestEntity> findByStatus(RequestStatus status);
    List<HelpRequestEntity> findAllByStatus(RequestStatus status);
    List<HelpRequestEntity> findAllByVolunteerId(UUID volunteerId);
    void deleteById(@NonNull UUID uuid);
    Page<HelpRequestEntity> findAllByStatusAndVolunteerIdIsNullAndAiTutorialIdIsNotNullAndSeniorIdIsIn(RequestStatus status, Collection<UUID> seniorIds, Pageable pageable);
    Page<HelpRequestEntity> findAllByStatusAndVolunteerIdIsNullAndAiTutorialIdIsNotNull(RequestStatus status, Pageable pageable);
    Page<HelpRequestEntity> findAllByVolunteerIdAndSupportSession_Status(UUID volunteerId, HelpStatus status, Pageable pageable);
    Long countByVolunteerIdAndStatus(UUID volunteerId,RequestStatus status);
    @Query("SELECT r FROM HelpRequestEntity r WHERE " +
            "(LOWER(r.title) LIKE CONCAT('%', LOWER(:searchText), '%') " +
            "OR LOWER(r.description) LIKE CONCAT('%', LOWER(:searchText), '%')) " +
            "AND r.status = es.techbridge.techbridgehelprequest.infrastructure.postgresql.entities.RequestStatus.FINDING_VOLUNTEER " +
            "AND r.aiTutorialId IS NOT NULL " +
            "AND r.volunteerId IS NULL"
    )
    Page<HelpRequestEntity> findAvailableRequestsWithFilters(
            @Param("searchText") String searchText,
            Pageable pageable
    );

    @Query("SELECT r FROM HelpRequestEntity r WHERE " +
            "(LOWER(r.title) LIKE CONCAT('%', LOWER(:searchText), '%') " +
            "OR LOWER(r.description) LIKE CONCAT('%', LOWER(:searchText), '%')) " +
            "AND r.seniorId IN :seniorIds " +
            "AND r.status = es.techbridge.techbridgehelprequest.infrastructure.postgresql.entities.RequestStatus.FINDING_VOLUNTEER " +
            "AND r.aiTutorialId IS NOT NULL " +
            "AND r.volunteerId IS NULL"
    )
    Page<HelpRequestEntity> findAvailableRequestsWithFiltersBySeniorIds(
            @Param("searchText") String searchText,
            @Param("seniorIds") List<UUID> seniorIds,
            Pageable pageable
    );
}
