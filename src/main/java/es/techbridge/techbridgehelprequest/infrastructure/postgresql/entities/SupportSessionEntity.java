package es.techbridge.techbridgehelprequest.infrastructure.postgresql.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "tb_support_sessions")
@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class SupportSessionEntity extends BaseAuditEntity{
    @Id
    @Column(updatable = false, nullable = false, columnDefinition = "uuid")
    private UUID id;

    @Enumerated(EnumType.STRING)
    private SessionMethods sessionMethod;

    private String s3RecordingUrl;

    private Boolean recordingConsent = false;

    @Column(columnDefinition = "TEXT")
    private String volunteerNotes;

    @Enumerated(EnumType.STRING)
    private HelpStatus status = HelpStatus.PENDING;

    @OneToOne
    @JoinColumn(name = "help_request_id")
    private HelpRequestEntity helpRequest;
}
