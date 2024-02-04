package projectnwt2023.backend.devices.dto;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class GateEventDTO {
    private String caller;
    private String eventType;

    private String timestamp;

    private String callerUsername;

    public GateEventDTO(String caller, String eventType, String timestamp) {
        this.caller = caller;
        this.eventType = eventType;
        this.timestamp = timestamp;
        this.callerUsername = "";
    }
}
