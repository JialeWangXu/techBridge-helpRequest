package es.techbridge.techbridgehelprequest.domain.model;

import es.techbridge.techbridgehelprequest.infrastructure.postgresql.entities.SessionMethods;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
@AllArgsConstructor
public class SessionMethodDto {

    private SessionMethods sessionMethod;
}
