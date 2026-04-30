package es.techbridge.techbridgehelprequest.domain.model;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class StepDto {

    private int number;
    private String instruction;
    private String advice;
}
