package projectnwt2023.backend.devices.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AirConditionerActionRequest {

    private String action;
    private String userEmail;

}
