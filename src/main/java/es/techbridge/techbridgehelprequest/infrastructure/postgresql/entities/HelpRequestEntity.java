package es.techbridge.techbridgehelprequest.infrastructure.postgresql.entities;

import es.techbridge.techbridgehelprequest.domain.model.aitutorial.AiTutorialDto;
import es.techbridge.techbridgehelprequest.domain.model.helprequest.HelpRequest;
import es.techbridge.techbridgehelprequest.domain.model.supportsession.SupportSession;
import es.techbridge.techbridgehelprequest.domain.model.user.UserDto;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "tb_help_requests")
@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class HelpRequestEntity extends BaseAuditEntity{

    @Id
    @Column(updatable = false, nullable = false, columnDefinition = "uuid")
    private UUID id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    private RequestStatus status = RequestStatus.OPEN;

    @Column(nullable = false)
    private UUID seniorId;

    private UUID volunteerId;

    private UUID aiTutorialId;

    @OneToOne(cascade = CascadeType.ALL, mappedBy = "helpRequest", orphanRemoval = true)
    @ToString.Exclude
    private SupportSessionEntity supportSession;

    public HelpRequestEntity (HelpRequest dto){
        this.id=dto.getId();
        this.title =dto.getTitle();
        this.description=dto.getDescription();
        this.status=dto.getStatus();
        this.seniorId=dto.getSenior().getId();
    }

    public HelpRequest toHelpRequest(){

        SupportSession session =null;
        UserDto volunteer= null;
        AiTutorialDto aiTutorialDto = null;
        if(this.volunteerId !=null){
            volunteer = UserDto.builder().id(this.volunteerId).build();
        }
        if(this.supportSession!=null){
            session = this.supportSession.toSupportSession();
        }
        if(this.aiTutorialId!=null){
            aiTutorialDto = AiTutorialDto.builder().id(this.aiTutorialId).build();
        }

        return HelpRequest.builder()
                .id(this.id)
                .senior(UserDto.builder().id(this.seniorId).build())
                .volunteer(volunteer)
                .title(this.title)
                .description(this.description)
                .aiTutorial(aiTutorialDto)
                .status(this.status)
                .updatedAt(this.getUpdatedAt())
                .createdAt(this.getCreatedAt())
                .supportSession(session)
                .build();
    }

}
