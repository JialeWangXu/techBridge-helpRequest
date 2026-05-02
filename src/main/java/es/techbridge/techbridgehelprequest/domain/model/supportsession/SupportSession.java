package es.techbridge.techbridgehelprequest.domain.model.supportsession;

import es.techbridge.techbridgehelprequest.domain.model.helprequest.HelpRequest;
import es.techbridge.techbridgehelprequest.infrastructure.postgresql.entities.HelpStatus;
import es.techbridge.techbridgehelprequest.infrastructure.postgresql.entities.SessionMethods;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SupportSession {

    private UUID id;

    private SessionMethods sessionMethod;

    private String s3RecordingUrl;

    private String meetingUrl;

    private Boolean recordingConsent;

    private String volunteerNotes;

    private HelpStatus status;

    private HelpRequest helpRequest;

    private LocalDateTime updatedAt;

    private LocalDateTime createdAt;
}
