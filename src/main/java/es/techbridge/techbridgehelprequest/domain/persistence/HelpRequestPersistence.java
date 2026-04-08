package es.techbridge.techbridgehelprequest.domain.persistence;

import es.techbridge.techbridgehelprequest.domain.model.HelpRequest;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface HelpRequestPersistence {

    void create(HelpRequest helpRequest);

}
