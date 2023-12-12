package projectnwt2023.backend.devices.dto;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class GateEventPayloadDTO {
    private int deviceId;
    private String usedFor;
    private String timeStamp;
    private String caller;
}
