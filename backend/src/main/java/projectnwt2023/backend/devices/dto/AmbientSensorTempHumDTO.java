package projectnwt2023.backend.devices.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AmbientSensorTempHumDTO {

    private AmbientSensorDateValueDTO temp;
    private AmbientSensorDateValueDTO hum;
    private Long length;

}
