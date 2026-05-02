package es.techbridge.techbridgehelprequest.domain.model.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDto {

    private UUID id;

    private String firstName;

    private String lastName;

    private String email;

    private String password;

    private UserRole role;

    private String telephone;

    private String address;

    private String city;

    private Province province;

    private Integer postalCode;

    private Boolean active;

    //Senior
    private ContactPreference contactPreference;

    //Volunteer
    private String specialties;
    private Boolean isAvailable = true;
}
