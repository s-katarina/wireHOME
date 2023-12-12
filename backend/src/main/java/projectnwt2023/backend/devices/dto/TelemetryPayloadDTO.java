package projectnwt2023.backend.devices.dto;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class TelemetryPayloadDTO {
    private int deviceId;
    private String value;
    private String timeStamp;
}
