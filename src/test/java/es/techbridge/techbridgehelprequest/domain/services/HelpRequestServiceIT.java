package es.techbridge.techbridgehelprequest.domain.services;

import es.techbridge.techbridgehelprequest.domain.model.HelpRequest;
import es.techbridge.techbridgehelprequest.domain.model.UserDto;
import es.techbridge.techbridgehelprequest.domain.model.UserRole;
import es.techbridge.techbridgehelprequest.domain.webclients.UserWebClient;
import es.techbridge.techbridgehelprequest.infrastructure.postgresql.entities.HelpRequestEntity;
import es.techbridge.techbridgehelprequest.infrastructure.postgresql.entities.RequestStatus;
import es.techbridge.techbridgehelprequest.infrastructure.postgresql.repositories.HelpRequestRepository;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.LIST;
import static org.mockito.ArgumentMatchers.any;

@SpringBootTest
@ActiveProfiles("test") // Asegúrate de tener un application-test.yml para H2
class HelpRequestServiceIT {

    @Autowired
    private HelpRequestService helpRequestService;

    @Autowired
    private HelpRequestRepository helpRequestRepository;

    @MockitoBean
    private UserWebClient userWebClient;

    private final String seniorEmail = "manolo@gmail.com";
    
    private final UserDto senior = UserDto.builder().id(UUID.fromString("11111111-1111-1111-1111-111111111111"))
            .firstName("Manolo")
            .lastName("García").email(seniorEmail)
            .role(UserRole.SENIOR)
            .build();

    @Test
     void create() {
        HelpRequest helpRequest = HelpRequest.builder()
                .title("Test1")
                .description("Testing1")
                .status(RequestStatus.OPEN)
                .build();
        BDDMockito.given(this.userWebClient.readByEmail(any(String.class)))
                .willAnswer(invocation -> senior);
        this.helpRequestService.create(seniorEmail,helpRequest);
        List<HelpRequestEntity> helpRequestEntities = this.helpRequestRepository.findBySeniorId(senior.getId());
        HelpRequestEntity result = helpRequestEntities.stream()
                .filter(x -> x.getTitle().equals("Test1")).findFirst().get();
        assertThat(result.getTitle()).isEqualTo("Test1");
    }

    @Test
    void getHelpRequestsByEmail(){
        BDDMockito.given(this.userWebClient.readByEmail(any(String.class)))
                .willAnswer(invocation -> senior);
        List<HelpRequest> result = this.helpRequestService.getHelpRequestsByEmail(seniorEmail);
        assertThat(result).isNotNull();
        assertThat(result.size()).isEqualTo(3);
    }
}
