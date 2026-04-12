package es.techbridge.techbridgehelprequest.infrastructure.postgresql.entities;

import es.techbridge.techbridgehelprequest.domain.model.HelpRequest;
import es.techbridge.techbridgehelprequest.domain.model.SupportSession;
import es.techbridge.techbridgehelprequest.domain.model.UserDto;
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

    @OneToOne(cascade = CascadeType.ALL, mappedBy = "helpRequest")
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
        UUID volunteer = null;
        if(this.volunteerId !=null){
            volunteer = this.volunteerId;
        }
        if(this.supportSession!=null){
            session = this.supportSession.toSupportSession();
        }

        return HelpRequest.builder()
                .id(this.id)
                .senior(UserDto.builder().id(this.seniorId).build())
                .volunteer(UserDto.builder().id(volunteer).build())
                .title(this.title)
                .description(this.description)
                .status(this.status)
                .updatedAt(this.getUpdatedAt())
                .createdAt(this.getCreatedAt())
                .supportSession(session)
                .build();
    }

}
