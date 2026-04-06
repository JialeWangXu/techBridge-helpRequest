package es.techbridge.techbridgehelprequest.infrastructure.postgresql.entities;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "tb_help_requests")
@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class HelpRequestEntity extends BaseAuditEntity{

    @Id
    @Column(updatable = false, nullable = false, columnDefinition = "uuid")
    private UUID id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    private RequestStatus status = RequestStatus.OPEN;

    @Column(nullable = false)
    private UUID seniorId;

    private UUID volunteerId;

    private UUID aiTutorialId;

    @OneToOne(cascade = CascadeType.ALL, mappedBy = "helpRequest")
    @ToString.Exclude
    private SupportSessionEntity supportSession;
}
