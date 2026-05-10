package es.techbridge.techbridgehelprequest.application.port.out.webclients;

import es.techbridge.techbridgehelprequest.configurations.FeignConfig;
import es.techbridge.techbridgehelprequest.domain.model.user.UserDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(name = UserWebClient.TECHBRIDGE_USER, configuration = FeignConfig.class)
public interface UserWebClient {
    String TECHBRIDGE_USER = "techbridge-user";
    String USERS = "/users";
    String ID_ID = "/id/{id}";
    String EMAIL = "/email/{email}";

    @GetMapping(USERS+EMAIL)
    UserDto readByEmail(@PathVariable String email);

    @GetMapping(USERS+ID_ID)
    UserDto readById(@PathVariable UUID id);

}
