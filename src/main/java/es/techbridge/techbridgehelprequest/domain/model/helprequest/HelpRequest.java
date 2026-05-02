package es.techbridge.techbridgehelprequest.domain.model.helprequest;

import es.techbridge.techbridgehelprequest.domain.model.supportsession.SupportSession;
import es.techbridge.techbridgehelprequest.domain.model.aitutorial.AiTutorialDto;
import es.techbridge.techbridgehelprequest.domain.model.user.UserDto;
import es.techbridge.techbridgehelprequest.infrastructure.postgresql.entities.RequestStatus;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class HelpRequest {

    private UUID id;

    private String title;

    private String description;

    private RequestStatus status;

    private LocalDateTime updatedAt;

    private LocalDateTime createdAt;

    private UserDto senior;

    private UserDto volunteer;

    private AiTutorialDto aiTutorial;

    private SupportSession supportSession;
}
