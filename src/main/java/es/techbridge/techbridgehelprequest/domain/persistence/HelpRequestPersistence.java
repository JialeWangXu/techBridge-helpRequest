package es.techbridge.techbridgehelprequest.domain.persistence;

import es.techbridge.techbridgehelprequest.domain.model.HelpRequest;
import es.techbridge.techbridgehelprequest.infrastructure.postgresql.entities.HelpRequestEntity;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface HelpRequestPersistence {

    void create(HelpRequest helpRequest);

    List<HelpRequestEntity> getHelpRequestsBySeniorId(UUID seniorId);

}
