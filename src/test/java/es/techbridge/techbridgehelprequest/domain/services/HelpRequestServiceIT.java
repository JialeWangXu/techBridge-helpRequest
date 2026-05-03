package es.techbridge.techbridgehelprequest.domain.services;

import es.techbridge.techbridgehelprequest.domain.exceptions.NotFoundException;
import es.techbridge.techbridgehelprequest.domain.model.aitutorial.AiTutorialDto;
import es.techbridge.techbridgehelprequest.domain.model.aitutorial.CreateAiTutorialDto;
import es.techbridge.techbridgehelprequest.domain.model.aitutorial.StepDto;
import es.techbridge.techbridgehelprequest.domain.model.helprequest.HelpRequest;
import es.techbridge.techbridgehelprequest.domain.model.user.UserDto;
import es.techbridge.techbridgehelprequest.domain.model.user.UserRole;
import es.techbridge.techbridgehelprequest.domain.webclients.AiTutorialWebClient;
import es.techbridge.techbridgehelprequest.domain.webclients.UserWebClient;
import es.techbridge.techbridgehelprequest.infrastructure.postgresql.entities.HelpStatus;
import es.techbridge.techbridgehelprequest.infrastructure.postgresql.entities.HelpRequestEntity;
import es.techbridge.techbridgehelprequest.infrastructure.postgresql.entities.RequestStatus;
import es.techbridge.techbridgehelprequest.infrastructure.postgresql.entities.SupportSessionEntity;
import es.techbridge.techbridgehelprequest.infrastructure.postgresql.repositories.HelpRequestRepository;
import es.techbridge.techbridgehelprequest.infrastructure.postgresql.repositories.SupportSessionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
class HelpRequestServiceIT {

    private static final UUID SENIOR_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID VOLUNTEER_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final UUID REQUEST_ID_FINDING_VOLUNTEER = UUID.fromString("11111111-2222-3333-4444-555566660001");
    private static final UUID REQUEST_ID_IN_PROGRESS = UUID.fromString("11111111-2222-3333-4444-555566660002");
    private static final UUID SUPPORT_SESSION_ID_IN_PROGRESS = UUID.fromString("22222222-bbbb-cccc-dddd-eeeeffff0003");
    private static final String SENIOR_EMAIL = "manolo@gmail.com";
    private static final String VOLUNTEER_EMAIL = "lucia@volunteer.org";

    @Autowired
    private HelpRequestService helpRequestService;

    @Autowired
    private HelpRequestRepository helpRequestRepository;

    @Autowired
    private SupportSessionRepository supportSessionRepository;

    @MockitoBean
    private UserWebClient userWebClient;

    @MockitoBean
    private AiTutorialWebClient aiTutorialWebClient;

    private UserDto senior;
    private UserDto volunteer;

    @BeforeEach
    void setUp() {
        this.senior = UserDto.builder()
                .id(SENIOR_ID)
                .firstName("Manolo")
                .lastName("Garcia")
                .email(SENIOR_EMAIL)
                .role(UserRole.SENIOR)
                .build();
        this.volunteer = UserDto.builder()
                .id(VOLUNTEER_ID)
                .firstName("Lucia")
                .lastName("Lopez")
                .email(VOLUNTEER_EMAIL)
                .role(UserRole.VOLUNTEER)
                .build();

        BDDMockito.given(this.userWebClient.readByEmail(any(String.class)))
                .willReturn(this.senior);
        BDDMockito.given(this.userWebClient.readById(any(UUID.class)))
                .willAnswer(invocation -> {
                    UUID id = invocation.getArgument(0);
                    if (VOLUNTEER_ID.equals(id)) {
                        return this.volunteer;
                    }
                    return this.senior;
                });

        AiTutorialDto aiTutorialDto = AiTutorialDto.builder()
                .id(UUID.randomUUID())
                .title("Testing1")
                .generalDescription("Testing")
                .steps(List.of(
                        new StepDto(1, "Test", "Icono Azul")
                )).build();
        BDDMockito.given(this.aiTutorialWebClient.create(any(CreateAiTutorialDto.class)))
                .willReturn(aiTutorialDto);
        BDDMockito.given(this.aiTutorialWebClient.getById(any(UUID.class)))
                .willReturn(aiTutorialDto);
    }

    @Test
    void create() {
        HelpRequest helpRequest = HelpRequest.builder()
                .title("Test1")
                .description("Testing1")
                .status(RequestStatus.OPEN)
                .build();
        this.helpRequestService.create(SENIOR_EMAIL, helpRequest);

        List<HelpRequestEntity> helpRequestEntities = this.helpRequestRepository.findBySeniorId(this.senior.getId());
        HelpRequestEntity result = helpRequestEntities.stream()
                .filter(entity -> entity.getTitle().equals("Test1"))
                .findFirst()
                .orElseThrow();

        assertThat(helpRequestEntities).hasSize(4);
        assertThat(result.getDescription()).isEqualTo("Testing1");
        assertThat(result.getStatus()).isEqualTo(RequestStatus.OPEN);
        assertThat(result.getSeniorId()).isEqualTo(SENIOR_ID);
    }

    @Test
    void getSeniorHelpRequestsByEmail() {
        List<HelpRequest> result = this.helpRequestService.getSeniorHelpRequestsByEmail(SENIOR_EMAIL);

        assertThat(result).isNotNull().hasSize(3)
                .extracting(HelpRequest::getId)
                .containsExactlyInAnyOrder(
                        REQUEST_ID_FINDING_VOLUNTEER,
                        REQUEST_ID_IN_PROGRESS,
                        UUID.fromString("11111111-2222-3333-4444-555566660003")
                );
        assertThat(result)
                .extracting(helpRequest -> helpRequest.getSenior().getEmail())
                .containsOnly(SENIOR_EMAIL);
    }

    @Test
    void getVolunteerHelpRequestsByEmail() {
        BDDMockito.given(this.userWebClient.readByEmail(any(String.class)))
                .willReturn(this.volunteer);

        List<HelpRequest> result = this.helpRequestService.getVolunteerHelpRequestsByEmail(VOLUNTEER_EMAIL);

        assertThat(result).isNotNull().hasSize(2)
                .extracting(HelpRequest::getId)
                .containsExactlyInAnyOrder(
                        REQUEST_ID_IN_PROGRESS,
                        UUID.fromString("11111111-2222-3333-4444-555566660003")
                );
        assertThat(result)
                .extracting(helpRequest -> helpRequest.getSenior().getEmail())
                .containsOnly(SENIOR_EMAIL);
    }

    @Test
    void getById() {
        HelpRequest request = this.helpRequestService.getById(REQUEST_ID_FINDING_VOLUNTEER);

        assertThat(request).isNotNull();
        assertThat(request.getId()).isEqualTo(REQUEST_ID_FINDING_VOLUNTEER);
        assertThat(request.getTitle()).contains("Tablet");
        assertThat(request.getStatus()).isEqualTo(RequestStatus.FINDING_VOLUNTEER);
        assertThat(request.getSenior().getEmail()).isEqualTo(SENIOR_EMAIL);
        assertThat(request.getVolunteer()).isNull();
    }

    @Test
    void getByIdWithVolunteer() {
        HelpRequest request = this.helpRequestService.getById(REQUEST_ID_IN_PROGRESS);

        assertThat(request).isNotNull();
        assertThat(request.getId()).isEqualTo(REQUEST_ID_IN_PROGRESS);
        assertThat(request.getStatus()).isEqualTo(RequestStatus.IN_PROGRESS);
        assertThat(request.getSenior().getEmail()).isEqualTo(SENIOR_EMAIL);
        assertThat(request.getVolunteer().getEmail()).isEqualTo(VOLUNTEER_EMAIL);
    }

    @Test
    void getByIdNotFound_InService() {
        UUID id = UUID.fromString("11111111-2222-3333-4444-787766660001");

        assertThatThrownBy(() -> this.helpRequestService.getById(id))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining(id.toString());
    }

    @Test
    void deleteById() {
        HelpRequestEntity requestToDelete = HelpRequestEntity.builder()
                .id(UUID.fromString("11111111-2222-3333-4444-555566660004"))
                .title("Delete")
                .description("Delete")
                .status(RequestStatus.CANCELLED)
                .seniorId(SENIOR_ID)
                .build();
        this.helpRequestRepository.save(requestToDelete);

        this.helpRequestService.deleteById(UUID.fromString("11111111-2222-3333-4444-555566660004"));

        assertThat(this.helpRequestRepository.existsById(UUID.fromString("11111111-2222-3333-4444-555566660004"))).isFalse();
    }

    @Test
    void deleteByIdNotFound() {
        UUID id = UUID.fromString("11111111-2222-3333-4444-777866660001");

        assertThatThrownBy(() -> this.helpRequestService.deleteById(id))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining(id.toString());
    }

    @Test
    void getAllAvailableRequests() {
        List<HelpRequest> result = this.helpRequestService.getAllAvailableHelpRequests();

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getId()).isEqualTo(REQUEST_ID_FINDING_VOLUNTEER);
        assertThat(result.getFirst().getStatus()).isEqualTo(RequestStatus.FINDING_VOLUNTEER);
        assertThat(result.getFirst().getSenior().getEmail()).isEqualTo(SENIOR_EMAIL);
    }

    @Test
    void getAllAvailableRequests_thenExcludeFindingVolunteerRequestsAlreadyAssigned() {
        this.helpRequestRepository.save(HelpRequestEntity.builder()
                .id(UUID.fromString("11111111-2222-3333-4444-555566660099"))
                .title("Peticion inconsistente")
                .description("Tiene voluntario asignado pero sigue en FINDING_VOLUNTEER")
                .status(RequestStatus.FINDING_VOLUNTEER)
                .aiTutorialId(UUID.fromString("11111111-7777-3333-4444-555566660099"))
                .seniorId(SENIOR_ID)
                .volunteerId(VOLUNTEER_ID)
                .build());

        List<HelpRequest> result = this.helpRequestService.getAllAvailableHelpRequests();

        assertThat(result).hasSize(1);
        assertThat(result)
                .extracting(HelpRequest::getId)
                .containsExactly(REQUEST_ID_FINDING_VOLUNTEER);
    }

    @Test
    void updateStatusToInProgress_thenUpdatedAndCreateASupportSession() {
        long supportSessionsBefore = this.supportSessionRepository.count();

        BDDMockito.given(this.userWebClient.readByEmail(any(String.class)))
                .willReturn(this.volunteer);
        HelpRequest helpRequest = this.helpRequestService.updateRequestStatusById(
                VOLUNTEER_EMAIL,
                REQUEST_ID_FINDING_VOLUNTEER,
                RequestStatus.IN_PROGRESS
        );
        assertThat(helpRequest.getStatus()).isEqualTo(RequestStatus.IN_PROGRESS);
        assertThat(this.helpRequestRepository.findById(REQUEST_ID_FINDING_VOLUNTEER))
                .get()
                .satisfies(request -> {
                    assertThat(request.getStatus()).isEqualTo(RequestStatus.IN_PROGRESS);
                    assertThat(request.getVolunteerId()).isEqualTo(VOLUNTEER_ID);
                });
        assertThat(this.supportSessionRepository.count()).isEqualTo(supportSessionsBefore + 1);
    }

    @Test
    void updateStatusByIdNotFound() {
        UUID id = UUID.fromString("11111111-2222-3333-4444-777866660010");

        assertThatThrownBy(() -> this.helpRequestService.updateRequestStatusById(VOLUNTEER_EMAIL,id, RequestStatus.IN_PROGRESS))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining(id.toString());
    }

    @Test
    void updateStatusToCancelled_thenUpdatedAndCancelSupportSession() {
        BDDMockito.given(this.userWebClient.readByEmail(any(String.class)))
                .willReturn(this.volunteer);
        HelpRequest helpRequest = this.helpRequestService.updateRequestStatusById(
                VOLUNTEER_EMAIL,
                REQUEST_ID_IN_PROGRESS,
                RequestStatus.CANCELLED
        );

        assertThat(helpRequest.getStatus()).isEqualTo(RequestStatus.CANCELLED);
        assertThat(this.helpRequestRepository.findById(REQUEST_ID_IN_PROGRESS))
                .get()
                .extracting(HelpRequestEntity::getStatus)
                .isEqualTo(RequestStatus.CANCELLED);
        assertThat(this.supportSessionRepository.findById(SUPPORT_SESSION_ID_IN_PROGRESS))
                .get()
                .extracting(SupportSessionEntity::getStatus)
                .isEqualTo(HelpStatus.CANCELLED);
    }

    @Test
    void updateStatusToCompleted_thenUpdatedAndFinishSupportSession() {
        BDDMockito.given(this.userWebClient.readByEmail(any(String.class)))
                .willReturn(this.volunteer);
        HelpRequest helpRequest = this.helpRequestService.updateRequestStatusById(
                VOLUNTEER_EMAIL,
                REQUEST_ID_IN_PROGRESS,
                RequestStatus.COMPLETED
        );

        assertThat(helpRequest.getStatus()).isEqualTo(RequestStatus.COMPLETED);
        assertThat(this.helpRequestRepository.findById(REQUEST_ID_IN_PROGRESS))
                .get()
                .extracting(HelpRequestEntity::getStatus)
                .isEqualTo(RequestStatus.COMPLETED);
        assertThat(this.supportSessionRepository.findById(SUPPORT_SESSION_ID_IN_PROGRESS))
                .get()
                .extracting(SupportSessionEntity::getStatus)
                .isEqualTo(HelpStatus.FINISHED);
    }

    @Test
    void saveAiTutorialId_then_update_ai_tutorial_id(){
        UUID aiTutorialId =UUID.fromString("33333333-bbbb-cccc-dddd-eeeeffff0002");
        this.helpRequestService.saveAiTutorialId(
                REQUEST_ID_FINDING_VOLUNTEER, aiTutorialId);

        assertThat(this.helpRequestRepository.findById(REQUEST_ID_FINDING_VOLUNTEER))
                .get()
                .extracting(HelpRequestEntity::getAiTutorialId)
                .isEqualTo(aiTutorialId);
    }

    @Test
    void saveAiTutorialIdNotFound() {
        UUID id = UUID.fromString("11111111-2222-3333-4444-777866660010");
        UUID tutorialId = UUID.fromString("33333333-bbbb-cccc-dddd-eeeeffff0003");
        assertThatThrownBy(() -> this.helpRequestService.saveAiTutorialId(id, tutorialId))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining(id.toString());
    }
}
