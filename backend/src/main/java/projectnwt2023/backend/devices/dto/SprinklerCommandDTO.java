package projectnwt2023.backend.devices.dto;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class SprinklerCommandDTO {
    private String caller;
    private String callerUsername;
    private String command;
    private String timestamp;
}
