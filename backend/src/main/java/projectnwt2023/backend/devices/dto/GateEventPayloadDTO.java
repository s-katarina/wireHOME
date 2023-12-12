package projectnwt2023.backend.devices.dto;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class GateEventPayloadDTO extends PayloadDTO {
    private String caller;
}
