package projectnwt2023.backend.devices.Measurement;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class BulbOnOffMeasurement {
    private String value;
    private String timestamp;
}
