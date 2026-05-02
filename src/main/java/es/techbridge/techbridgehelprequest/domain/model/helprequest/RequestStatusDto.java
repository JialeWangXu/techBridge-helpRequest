package es.techbridge.techbridgehelprequest.domain.model.helprequest;

import es.techbridge.techbridgehelprequest.infrastructure.postgresql.entities.RequestStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
@AllArgsConstructor
public class RequestStatusDto {
    private RequestStatus status;
}
