package es.techbridge.techbridgehelprequest.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AiTutorialDto {

    private UUID id;
    private String stepContent;
    private String s3ImageUrls;
    private LocalDateTime generateAt;
}
