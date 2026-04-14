package es.techbridge.techbridgehelprequest.infrastructure.postgresql.entities;

import es.techbridge.techbridgehelprequest.domain.model.SupportSession;
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
    private HelpStatus status = HelpStatus.ACTIVE;

    @OneToOne
    @JoinColumn(name = "help_request_id")
    private HelpRequestEntity helpRequest;

    public SupportSessionEntity(SupportSession dto){
        this.sessionMethod = dto.getSessionMethod();
        this.s3RecordingUrl = dto.getS3RecordingUrl();
        this.recordingConsent = dto.getRecordingConsent();
        this.volunteerNotes = dto.getVolunteerNotes();
        this.helpRequest = new HelpRequestEntity(dto.getHelpRequest());
        this.status = dto.getStatus();
    }

    public SupportSession toSupportSession(){
        // para evitar el bucle, si SupportSession necesita informacion de helprequest,
        // necesita buscar manualmente.
        return SupportSession.builder()
                .id(this.id)
                .sessionMethod(this.sessionMethod)
                .s3RecordingUrl(this.s3RecordingUrl)
                .recordingConsent(this.recordingConsent)
                .volunteerNotes(this.volunteerNotes)
                .status(this.status)
                .build();
    }
}
